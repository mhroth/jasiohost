/*
 *  Copyright 2009 Martin Roth (mhroth@gmail.com)
 * 
 *  This file is part of JAsioHost.
 *
 *  JVstHost is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JVstHost is distributed in the hope that it will be useful,
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

public class ExampleHost implements AsioDriverListener {

  private AsioDriver asioDriver;
  private Set<AsioChannelInfo> activeChannels;
  private int sampleIndex;
  private int bufferSize;
  private double sampleRate;
  
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
    asioDriver.createBuffers(activeChannels, bufferSize);
    asioDriver.start();
  }
  
  public void bufferSwitch(
      int[][] inputInt, int[][] outputInt, 
      float[][] inputFloat, float[][] outputFloat, 
      double[][] inputDouble, double[][] outputDouble) {

    for (int i = 0; i < bufferSize; i++, sampleIndex++) {
      outputInt[0][i] = (int) (Math.sin(2 * Math.PI * sampleIndex * 440.0 / sampleRate) * (double) Integer.MAX_VALUE);
    }
    System.arraycopy(outputInt[0], 0, outputInt[1], 0, bufferSize);
  }

  public void latenciesChanged(int inputLatency, int outputLatency) {
    // TODO Auto-generated method stub

  }

  public void resetRequest() {
    // TODO Auto-generated method stub

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
      Thread.sleep(5000);
    } catch (Exception e) {
      // ???
    }
    host.start();
    try {
      Thread.sleep(2000);
    } catch (Exception e) {
      // ???
    }
    JAsioHost.shutdownAndUnloadDriver();
  }

}
