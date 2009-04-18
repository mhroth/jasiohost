/*
 *  Copyright 2009 Martin Roth (mhroth@gmail.com)
 * 
 *  This file is part of JAsioHost.
 *
 *  JAsioHost is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JAsioHost is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JAsioHost.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.synthbot.jasiohost;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * All methods may throw an <code>AsioException</code>, which is a <code>RuntimeException</code> 
 * (i.e., it is not required to be caught). The error message will have some information about
 * what has gone wrong. Generally, it is not possible to recover from such errors, except for
 * shutting down the driver.
 * Note that all methods can throw an <code>IllegalStateException</code> if the driver is not
 * in the appropriate state for that method to be called. For instance, if <code>start()</code>
 * is called before <code>createBuffers</code>, an <code>IllegalStateException</code> will be
 * thrown.
 */
public class AsioDriver {
  
  private volatile AsioDriverState state;
  private final List<AsioDriverListener> listeners;
  private Set<AsioChannelInfo> activeChannels;
  private final AsioChannelInfo[] inputChannels;
  private final AsioChannelInfo[] outputChannels;
  private final AsioDriverInfo driverInfo;
  
  protected AsioDriver() {
    state = AsioDriverState.LOADED;
    listeners = new ArrayList<AsioDriverListener>();
    driverInfo = init(); // initialise the driver
    
    inputChannels = new AsioChannelInfo[ASIOGetChannels(true)];
    for (int i = 0; i < inputChannels.length; i++) {
      inputChannels[i] = ASIOGetChannelInfo(i, true);
    }
    outputChannels = new AsioChannelInfo[ASIOGetChannels(false)];
    for (int i = 0; i < outputChannels.length; i++) {
      outputChannels[i] = ASIOGetChannelInfo(i, false);
    }
  }
  
  static {
    System.loadLibrary("jasiohost");
  }
  
  /*
   * Normally JAsioHost shuts down the driver. But it is put into finalize() in order to properly
   * shut down the driver in case the JVM is otherwise stopped.
   * (non-Javadoc)
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    try {
      returnToState(AsioDriverState.LOADED);
      JAsioHost.shutdownAndUnloadDriver();
    } finally {
      super.finalize();
    }
  }
  
  /**
   * Returns the name of the driver.
   */
  public String getDriverName() {
    return driverInfo.getDriverName();
  }
  
  /**
   * Returns the version of the driver.
   */
  public int getDriverVersion() {
    return driverInfo.getDriverVersion();
  }
  
  /**
   * Returns the version of ASIO which this driver uses (currently 1 or 2).
   */
  public int getAsioVersion() {
    return driverInfo.getAsioVersion();
  }
  
  /**
   * Returns the current state of the ASIO driver.
   */
  public AsioDriverState getState() {
    return state;
  }
  
  /**
   * ASIOInit
   * @return  An AsioDriverInfo object is returned with name and version information
   * about the initialised driver.
   */
  public synchronized AsioDriverInfo init() {
    if (!AsioDriverState.LOADED.equals(state)) {
      throw new IllegalStateException("AsioDriver must be in AsioDriverState.LOADED state in order " +
          "to be initialised. The current state is: " + state.toString());
    }
    state = AsioDriverState.INITIALIZED;
    return ASIOInit();
  }
  private native AsioDriverInfo ASIOInit();
  
  /**
   * ASIOExit
   */
  public synchronized void exit() {
    if (!AsioDriverState.INITIALIZED.equals(state)) {
    	throw new IllegalStateException("AsioDriver must be in AsioDriverState.INITIALIZED state " +
            "in order to be initialised. The current state is: " + state.toString());
    }
    ASIOExit();
    state = AsioDriverState.LOADED;
  }
  private native void ASIOExit();
  
  /**
   * Open the native control panel, allowing the user to adjust the ASIO settings. A control panel
   * may not be provided by all drivers on all platforms.
   */
  public synchronized void openControlPanel() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    ASIOControlPanel();
  }
  private static native void ASIOControlPanel();
  
  /**
   * Returns the number of available input channels. -1 is returned if there is an error.
   * @return  The number of available input channels.
   */
  public int getNumChannelsInput() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return inputChannels.length;
  }
  
  /**
   * Returns the number of available output channels. -1 is return if there is an error.
   * @return  The number of available output channels.
   */
  public int getNumChannelsOutput() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return outputChannels.length;
  }
  private static native int ASIOGetChannels(boolean isInput);

  /**
   * Returns the current sample rate to which the host is set.
   * @return  The current sample rate.
   */
  public synchronized double getSampleRate() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetSampleRate();
  }
  private static native double ASIOGetSampleRate();
  
  /**
   * Inquires of the hardware if a specific available sample rate is available.
   * @param sampleRate  The sample rate in question.
   * @return  True if the sample rate is supported. False otherwise.
   */
  public synchronized boolean canSampleRate(double sampleRate) {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOCanSampleRate(sampleRate);
  }
  private static native boolean ASIOCanSampleRate(double sampleRate);
  
  /**
   * Returns the minimum supported buffer size.
   */
  public synchronized int getBufferMinSize() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetBufferSize(0);
  }
  
  /**
   * Returns the maximum supported buffer size.
   */
  public synchronized int getBufferMaxSize() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetBufferSize(1);
  }
  
  /**
   * Returns the preferred buffer size. The host should attempt to use this buffer size.
   */
  public synchronized int getBufferPreferredSize() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetBufferSize(2);
  }
  
  public synchronized int getBufferGranularity() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetBufferSize(3);
  }
  
  private static native int ASIOGetBufferSize(int index);
  
  /**
   * Note: As <code>getLatencyInput()</code> will also have to include the audio buffer size of the 
   * <code>createBuffers()</code> call, the application should call this function after the buffer creation. 
   * In the case that the call occurs beforehand the driver should assume preferred buffer size. 
   * @return  The input latency in samples.
   */
  public synchronized int getLatencyInput() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetLatencies(true);
  }
  
  /**
   * Note: As <code>getLatencyOutput()</code> will also have to include the audio buffer size of the 
   * <code>createBuffers()</code> call, the application should call this function after the buffer creation. 
   * In the case that the call occurs beforehand the driver should assume preferred buffer size.
   * @return  The output latency in samples.
   */
  public synchronized int getLatencyOutput() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetLatencies(false);
  }
  private static native int ASIOGetLatencies(boolean isInput);
  
  /**
   * Get information about an input channel.
   * @param index  The input channel index to get information about.
   * @return An <code>AsioChannelInfo</code> object representing the requested input channel.
   * @throws IndexOutOfBoundsException  Thrown if the requested channel index is out of bounds. The
   * channel does not exist.
   */
  public AsioChannelInfo getChannelInfoInput(int index) {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    if (index < 0 || index >= inputChannels.length) {
      throw new IndexOutOfBoundsException("The input index must be in [0," + 
          Integer.toString(inputChannels.length) + "): " + Integer.toString(index));
    }
    return inputChannels[index];
  }
  
  /**
   * Get information about an output channel.
   * @param index  The output channel index to get information about.
   * @return An <code>AsioChannelInfo</code> object representing the requested output channel.
   * @throws IndexOutOfBoundsException  Thrown if the requested channel index is out of bounds. The
   * channel does not exist.
   */
  public AsioChannelInfo getChannelInfoOutput(int index) {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    if (index < 0 || index >= outputChannels.length) {
      throw new IndexOutOfBoundsException("The output index must be in [0," + 
          Integer.toString(outputChannels.length) + "): " + Integer.toString(index));
    }
    return outputChannels[index];
  }
  private static native AsioChannelInfo ASIOGetChannelInfo(int index, boolean isInput);
  
  /**
   * Creates audio buffers for the set of desinated channels. The buffer size is that as returned by
   * <code>getBufferPreferredSize()</code>.
   * @param channelsToInit  A <code>Set</code> of <code>AsioChannelInfo</code> objects designating the
   * input and output channels to initialise and create audio buffers for.
   */
  public synchronized void createBuffers(Set<AsioChannelInfo> channelsToInit) {
    if (!AsioDriverState.INITIALIZED.equals(state)) {
      throw new IllegalStateException("The ASIO driver must be in the INITIALIZED state in order to createBuffers().");
    }
	  if (channelsToInit == null) {
	    throw new NullPointerException("The set of channels to initialise may not be null.");
	  }
	  if (channelsToInit.contains(null)) {
	    throw new IllegalArgumentException("The set of channels to initialise may not contain a null value.");
	  }
	  if (channelsToInit.size() == 0) {
      throw new IllegalArgumentException("The set of channels to initialise may not be empty.");
	  }
    
    // make a defensive copy of the the channel initialisation set
    activeChannels = new HashSet<AsioChannelInfo>(channelsToInit.size());
    activeChannels.addAll(channelsToInit);
    
	  ASIOCreateBuffers(activeChannels.toArray(new AsioChannelInfo[0]), getBufferPreferredSize());
	  
	  state = AsioDriverState.PREPARED;
  }
  private static native void ASIOCreateBuffers(AsioChannelInfo[] channelsToInit, int bufferSize);
  
  /**
   * 
   */
  public synchronized void disposeBuffers() {
    if (!AsioDriverState.PREPARED.equals(state)) {
      throw new IllegalStateException();
    }
    for (AsioChannelInfo channelInfo : activeChannels) {
      channelInfo.setByteBuffers(null, null); // clear the ByteBuffer references
    }
    activeChannels = null;
    ASIODisposeBuffers();
    state = AsioDriverState.INITIALIZED;
  }
  private static native void ASIODisposeBuffers();
  
  /**
   * Start the driver. Input buffers are presented and output buffers consumed via calls to 
   * <code>bufferSwitch()</code>. Puts the <code>AsioDriver</code> into the <code>RUNNING</code> state.
   */
  public synchronized void start() {
    if (!AsioDriverState.PREPARED.equals(state)) {
      throw new IllegalStateException();
    }
    ASIOStart();
    state = AsioDriverState.RUNNING;
  }
  private static native void ASIOStart();
  
  /**
   * Stop the driver. Calls to <code>bufferSwitch()</code> will cease. The <code>AsioDriver</code> 
   * returns to the <code>PREPARED</code> state.
   */
  public synchronized void stop() {
    if (!AsioDriverState.RUNNING.equals(state)) {
      throw new IllegalStateException();
    }
    ASIOStop();
    state = AsioDriverState.PREPARED;
  }
  private static native void ASIOStop();
  
  /**
   * Return the driver to a given state. If the target state is ahead of the current state, or if
   * the target state is equal to the current state, then this method has no effect.
   * @param targetState  The state to which the driver should return.
   */
  public synchronized void returnToState(AsioDriverState targetState) {
    if (targetState == null) {
      throw new NullPointerException("Target state may not be null.");
    }
    if (targetState.ordinal() < state.ordinal()) {
      switch (state) {
      	case RUNNING: {
      		stop();
      		if (state.equals(targetState)) {
            break;
      		}
      		// allow fall-throughs
      	}
      	case PREPARED: {
      		disposeBuffers();
      		if (state.equals(targetState)) {
            break;
        	}
      	}
      	case INITIALIZED: {
      		exit();
        }
      }
    }
  }
  
  /**
   * Add a new <code>AsioDriverListener</code>. Listeners can only be updated while the driver
   * is in the LOADED or INITIALIZED state.
   * @param listener
   */
  public void addAsioDriverListener(AsioDriverListener listener) {
    if (state.ordinal() < AsioDriverState.PREPARED.ordinal()) {
      synchronized(listeners) {
        if (!listeners.contains(listener)) {
          listeners.add(listener);
        }        
      }
    } else {
      throw new IllegalStateException("AsioDriverListeners can only be updated while the " +
      		"AsioDriver is in the LOADED or INITIALIZED state.");
    }
  }
  
  /**
   * Unregister an <code>AsioDriverListener</code>. Listeners can only be updated while the driver
   * is in the LOADED or INITIALIZED state.
   * @param listener
   */
  public void removeAsioDriverListener(AsioDriverListener listener) {
    if (state.ordinal() < AsioDriverState.PREPARED.ordinal()) {
      synchronized(listeners) {
        listeners.remove(listener);
      }
    } else {
      throw new IllegalStateException("AsioDriverListeners can only be updated while the " +
      		"AsioDriver is in the LOADED or INITIALIZED state.");
    }
  }
  
  
  /*
   * Callbacks
   */
  
  @SuppressWarnings("unused")
  private void fireSampleRateDidChange(double sampleRate) {
    for (AsioDriverListener listener : listeners) {
      listener.sampleRateDidChange(sampleRate);
    }
  }
  
  @SuppressWarnings("unused")
  private void fireResetRequest() {
    for (AsioDriverListener listener : listeners) {
      listener.resetRequest();
    }
  }
  
  @SuppressWarnings("unused")
  private void fireResyncRequest() {
    for (AsioDriverListener listener : listeners) {
      listener.resyncRequest();
    }
  }
  
  @SuppressWarnings("unused")
  private void fireBufferSizeChanged(int bufferSize) {
    for (AsioDriverListener listener : listeners) {
      listener.bufferSizeChanged(bufferSize);
    }
  }
  
  @SuppressWarnings("unused")
  private void fireLatenciesChanged(int inputLatency, int outputLatency) {
    for (AsioDriverListener listener : listeners) {
      listener.latenciesChanged(inputLatency, outputLatency);
    }
  }
  
  @SuppressWarnings("unused")
  private void fireBufferSwitch(long systemTime, long samplePosition, int bufferIndex) {
    for (AsioChannelInfo channelInfo : activeChannels) {
      channelInfo.setBufferIndex(bufferIndex);
    }
    for (AsioDriverListener listener : listeners) {
      listener.bufferSwitch(systemTime, samplePosition, activeChannels);
    }
  }
}
