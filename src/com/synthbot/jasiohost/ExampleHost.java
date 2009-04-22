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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The ExampleHost is a very simple example of how to load and initialise an ASIO driver. It loads
 * the first driver referenced by the system. The control panel is opened for 5 seconds for the user
 * to make any adjustments to the settings. Then a 440Hz tone (sine) is played on the first two
 * outputs for 2 seconds.
 */
public class ExampleHost implements AsioDriverListener {

  private AsioDriver asioDriver;
  private Set<AsioChannelInfo> activeChannels;
  private int sampleIndex;
  private int bufferSize;
  private double sampleRate;
  private float[] output;

  public ExampleHost() {
    List<String> driverNameList = JAsioHost.getDriverNames();
    asioDriver = JAsioHost.getAsioDriver(driverNameList.get(0));
    activeChannels = new HashSet<AsioChannelInfo>();
    activeChannels.add(asioDriver.getChannelInfoOutput(0));
    activeChannels.add(asioDriver.getChannelInfoOutput(1));
    asioDriver.addAsioDriverListener(this);
    sampleIndex = 0;
  }

  public void openControlPanel() {
    asioDriver.openControlPanel();
  }

  public void start() {
    bufferSize = asioDriver.getBufferPreferredSize();
    sampleRate = asioDriver.getSampleRate();
    output = new float[bufferSize];
    asioDriver.createBuffers(activeChannels);
    asioDriver.start();
  }
  
  public void bufferSwitch(long systemTime, long samplePosition, Set<AsioChannelInfo> channels) {
    for (int i = 0; i < bufferSize; i++, sampleIndex++) {
      output[i] = (float) Math.sin(2 * Math.PI * sampleIndex * 440.0 / sampleRate);
    }
    for (AsioChannelInfo channelInfo : channels) {
      channelInfo.write(output);
    }
  }
  
  public void bufferSizeChanged(int bufferSize) {
    // TODO Auto-generated method stub
  }

  public void latenciesChanged(int inputLatency, int outputLatency) {
    // TODO Auto-generated method stub
  }

  public void resetRequest() {
    /*
     * This thread will attempt to shut down the ASIO driver. However, it will
     * block on the AsioDriver object at least until the current method has returned.
     */
	new Thread() {
      @Override
      public void run() {
    	 asioDriver.returnToState(AsioDriverState.INITIALIZED);
      }
    }.start();
  }

  public void resyncRequest() {
    // TODO Auto-generated method stub
  }

  public void sampleRateDidChange(double sampleRate) {
    // TODO Auto-generated method stub
  }

  public static void main(String[] args) {
    ExampleHost host = new ExampleHost();
    host.openControlPanel();
    try {
      Thread.sleep(1000);
    } catch (Exception e) {
      // ???
    }
    host.start();
    try {
      Thread.sleep(4000);
    } catch (Exception e) {
      // ???
    }
    JAsioHost.shutdownAndUnloadDriver();
  }

}
