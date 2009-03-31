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
  
  public void shutdown() {
    JAsioHost.shutdownAndUnloadDriver();
  }
  
  public void bufferSwitch(
      int[][] inputInt, int[][] outputInt, 
      float[][] inputFloat, float[][] outputFloat, 
      double[][] inputDouble, double[][] outputDouble) {

    for (int i = sampleIndex; i < sampleIndex+bufferSize; i++) {
      outputInt[0][i] = (int) (Math.sin(2 * Math.PI * i * 440.0 / sampleRate) * (double) Integer.MAX_VALUE);
    }
    System.arraycopy(outputInt[0], 0, outputInt[1], 0, bufferSize);
    sampleIndex += bufferSize;
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
    //host.openControlPanel();
    host.start();
    try {
      Thread.sleep(1000);
    } catch (Exception e) {
      // ???
    }
    JAsioHost.shutdownAndUnloadDriver();
  }

}
