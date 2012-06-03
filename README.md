Welcome to the JAsioHost wiki!

# Getting Started
Place `JAsioHost.jar` into your classpath and `jasiohost.dll` into `C:\WINDOWS\system32`. The basic design pattern for using `JAsioHost` is as follows. `static` methods in `AsioDriver` are used to collect information about the available drivers. `getDriver` is called in order to load and instantiate a given driver. The `AsioDriver` can then be queried for channel state information. Audio buffers are created using @createBuffers@, before `start` is called. Callbacks are made from the driver to registered `AsioDriverListener` objects in order to submit input and retrieve output.

```Java
// get a list of available ASIO drivers
List<String> driverNameList = AsioDriver.getDriverNames();

// load the names ASIO driver
AsioDriver asioDriver = AsioDriver.getDriver(driverNameList.get(0));

// create a Set of AsioChannels, defining which input and output channels will be used
Set<AsioChannel> activeChannels = new HashSet<AsioChannel>();

// configure the ASIO driver to use the given channels
activeChannels.add(asioDriver.getChannelOutput(0));
activeChannels.add(asioDriver.getChannelOutput(1));

// create the audio buffers and prepare the driver to run
asioDriver.createBuffers();

// start the driver
asioDriver.start();
try {
  Thread.sleep(1000);
} catch (InterruptedException ie) {
  ie.printStackTrace(System.err);
}

// tear everything down
AsioDriver.shutdownAndUnloadDriver();
```

Note that you can only load one ASIO driver at time. This is a limitation of the original API (AFAIK).


## Note on Compilation
If you are brave enough to try to compile the native component, please note the following helpful tips.

* `./common/asiodrvr.cpp` is not necessary. Rename or remove it.
* `./common/dllentry.cpp` is not necessary. Rename or remove it.
* Line 219 of `./common/combase.h`, `#if WINVER < 0x0501`, should be replaced with `#if 0`. See [here](http://osdir.com/ml/audio.portaudio.devel/2006-09/msg00058.html) for more information.


## A Note about API Translation
The `JAsioHost` API does not strictly reflect the original C++ API written by Steinberg. There are two reasons for this. The first is that some elements of the original API do not translate well due to language semantics. The second is that I felt that some things could be improved or simplified.

An example of the former is that the native API refers to an audio buffer by using a `void` pointer. Java does not allow arrays to be referenced opaquely. `JAsioHost` therefore encapsulates an audio buffer by using a `ByteBuffer`, which exposes the raw bytes of the native buffer, but also allows other types to be read and written with relative ease.

An example of the latter is the absence of the `ASIOBufferInfo` structure in Java. I simply added a reference to the audio buffer to the active `AsioChannel` objects. As the audio buffer conceptually belongs to a channel, this seemed to make sense, and also remove a superfluous class.


# License
JAsioHost is released under the [Lesser Gnu Public License](http://www.gnu.org/licenses/lgpl.html) (LGPL). Basically, the library stays open source, but you can use if for whatever you want, including closed source applications. You must publicly credit the use of this library.


## Contact
My name is Martin Roth. I am the author of JAsioHost. Please feel free to contact me with any questions or comments at [mhroth+jasiohost@gmail.com](mailto:mhroth+jasiohost@gmail.com).