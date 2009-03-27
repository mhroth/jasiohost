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

import java.util.ArrayList;
import java.util.List;

public class AsioDriver {
  
  private AsioDriverState state;
  private List<AsioDriverListener> listeners;
  
  protected AsioDriver() {
    state = AsioDriverState.LOADED;
    listeners = new ArrayList<AsioDriverListener>();
  }
  
  static {
    System.loadLibrary("jasiohost");
  }
  
  /*
   * Normally JAsioHost shuts down the driver. But it is put into finalize() in order to properly
   * shut down the driver in case the JVM is otherwise shut down.
   * (non-Javadoc)
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    try {
      shutdown();
    } finally {
      super.finalize();
    }
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
  public AsioDriverInfo init() {
    if (!AsioDriverState.LOADED.equals(state)) {
      throw new IllegalStateException("AsioDriver must be in AsioDriverState.LOADED state in order " +
      		"to be initialised. The current state is: " + state.toString());
    }
    state = AsioDriverState.INITIALIZED;
    return ASIOInit();
  }
  private static native AsioDriverInfo ASIOInit();
  
  /**
   * ASIOExit
   */
  public void exit() {
    if (!AsioDriverState.INITIALIZED.equals(state)) {
      throw new IllegalStateException();
    }
    ASIOExit();
    state = AsioDriverState.LOADED;
  }
  private static native void ASIOExit();
  
  /**
   * Open the native control panel, allowing the user to adjust the ASIO settings. A control panel
   * may not be provided by all drivers on all platforms.
   */
  public void openControlPanel() {
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
    return ASIOGetChannels(true);
  }
  
  /**
   * Returns the number of available output channels. -1 is return if there is an error.
   * @return  The number of available output channels.
   */
  public int getNumChannelsOutput() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetChannels(false);
  }
  public static native int ASIOGetChannels(boolean isInput);

  /**
   * Returns the current sample rate to which the host is set.
   * @return  The current sample rate.
   */
  public double getSampleRate() {
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
  public boolean canSampleRate(double sampleRate) {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOCanSampleRate(sampleRate);
  }
  private static native boolean ASIOCanSampleRate(double sampleRate);
  
  public int getBufferMinSize() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetBufferSize(0);
  }
  
  public int getBufferMaxSize() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetBufferSize(1);
  }
  
  public int getBufferPreferredSize() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetBufferSize(2);
  }
  
  public int getBufferGranularity() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetBufferSize(3);
  }
  
  private static native int ASIOGetBufferSize(int index);
  
  /**
   * Note: As ASIOGetLatencies() will also have to include the audio buffer size of the 
   * ASIOCreateBuffers() call, the application has to call this function after the buffer creation. 
   * In the case that the call occurs beforehand the driver should assume preferred buffer size. 
   * @return
   */
  public int getLatencyInput() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetLatencies(true);
  }
  
  public int getLatencyOutput() {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    return ASIOGetLatencies(false);
  }
  
  private static native int ASIOGetLatencies(boolean isInput);
  
  /**
   * 
   * @param index
   * @return
   * @throws IndexOutOfBoundsException  Thrown if the requested channel index is out of bounds. The
   * channel does not exist.
   */
  public AsioChannelInfo getChannelInfoInput(int index) {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    if (index < 0 || index >= getNumChannelsInput()) {
      throw new IndexOutOfBoundsException();
    }
    return ASIOGetChannelInfo(index, true);
  }
  
  /**
   * 
   * @param index
   * @return
   * @throws IndexOutOfBoundsException  Thrown if the requested channel index is out of bounds. The
   * channel does not exist.
   */
  public AsioChannelInfo getChannelInfoOutput(int index) {
    if (!state.atLeastInState(AsioDriverState.INITIALIZED)) {
      throw new IllegalStateException();
    }
    if (index < 0 || index >= getNumChannelsOutput()) {
      throw new IndexOutOfBoundsException();
    }
    AsioChannelInfo channelInfo = ASIOGetChannelInfo(index, false);
    switch (channelInfo.getSampleType()) {
      case ASIOSTInt16MSB:
      case ASIOSTInt24MSB:
      case ASIOSTInt16LSB:
      case ASIOSTInt24LSB:
      case ASIOSTDSDInt8LSB1:
      case ASIOSTDSDInt8MSB1:
      case ASIOSTDSDInt8NER8: {
        System.err.println("WARNING: JAsioHost does not support this sample type at the moment. " +
        		"Undefined behaviour will result if the driver is start()ed.");
      }
    }
    return channelInfo;
  }
  private static native AsioChannelInfo ASIOGetChannelInfo(int index, boolean isInput);
  
  /**
   * 
   * @param numInputs
   * @param numOutputs
   * @param bufferSize
   */
  public void createBuffers(int numInputs, int numOutputs, int bufferSize) {
    if (!AsioDriverState.INITIALIZED.equals(state)) {
      throw new IllegalStateException();
    }
    if (numInputs < 0 || numInputs >= getNumChannelsInput()) {
      throw new IllegalArgumentException();
    }
    if (numOutputs < 0 || numOutputs >= getNumChannelsOutput()) {
      throw new IllegalArgumentException();
    }
    if ((bufferSize - getBufferMinSize()) % this.getBufferGranularity() != 0) {
      throw new IllegalArgumentException();
    }
    /* ???
    if (bufferSize != this.getBufferPreferredSize()) {
      throw new IllegalArgumentException();
    }
    */
    ASIOCreateBuffers(numInputs, numOutputs, bufferSize);
    state = AsioDriverState.PREPARED;
  }
  private static native void ASIOCreateBuffers(int numInputs, int numOutputs, int bufferSize);
  
  /**
   * 
   */
  public void disposeBuffers() {
    if (!AsioDriverState.PREPARED.equals(state)) {
      throw new IllegalStateException();
    }
    ASIODisposeBuffers();
    state = AsioDriverState.INITIALIZED;
  }
  private static native void ASIODisposeBuffers();
  
  /**
   * 
   */
  public void start() {
    if (!AsioDriverState.PREPARED.equals(state)) {
      throw new IllegalStateException();
    }
    ASIOStart();
    state = AsioDriverState.RUNNING;
  }
  private static native void ASIOStart();
  
  /**
   * 
   */
  public void stop() {
    if (!AsioDriverState.RUNNING.equals(state)) {
      throw new IllegalStateException();
    }
    ASIOStop();
    state = AsioDriverState.PREPARED;
  }
  private static native void ASIOStop();
  
  /**
   * Shut down the driver, regardless of what state it is in. Return it to the LOADED state.
   */
  protected void shutdown() {
    switch (state) {
      case RUNNING: {
        stop();
        // allow fall-throughs
      }
      case PREPARED: {
        disposeBuffers();
      }
      case INITIALIZED: {
        exit();
      }
    }
  }
  
  
  /*
   * Callbacks
   */
  
  // TODO: note the synchronization issues in using the list
  public void addAsioDriverListener(AsioDriverListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }
  
  public void removeAsioDriverListener(AsioDriverListener listener) {
    listeners.remove(listener);
  }
  
  @SuppressWarnings("unused")
  private void fireSampleRateDidChange(double sampleRate) {
    System.out.println("sampleRateDidChange @ " + Long.toString(System.currentTimeMillis()));
    for (AsioDriverListener listener : listeners) {
      listener.sampleRateDidChange(sampleRate);
    }
  }
  
  @SuppressWarnings("unused")
  private void fireResetRequest() {
    System.out.println("resetRequest @ " + Long.toString(System.currentTimeMillis()));
    for (AsioDriverListener listener : listeners) {
      listener.resetRequest();
    }
  }
  
  @SuppressWarnings("unused")
  private void fireResyncRequest() {
    System.out.println("resyncRequest @ " + Long.toString(System.currentTimeMillis()));
    for (AsioDriverListener listener : listeners) {
      listener.resyncRequest();
    }
  }
  
  @SuppressWarnings("unused")
  private void fireLatenciesChanged(int inputLatency, int outputLatency) {
    System.out.println("latenciesChanged @ " + Long.toString(System.currentTimeMillis()));
    for (AsioDriverListener listener : listeners) {
      listener.latenciesChanged(inputLatency, outputLatency);
    }
  }
  
  @SuppressWarnings("unused")
  private void fireBufferSwitch(int[][] inputs, int[][] outputs) {
    System.out.println("bufferSwitch @ " + Long.toString(System.currentTimeMillis()));
    for (AsioDriverListener listener : listeners) {
      listener.bufferSwitch(inputs, outputs);
    }
  }
  
  @SuppressWarnings("unused")
  private void fireBufferSwitch(float[][] inputs, float[][] outputs) {
    System.out.println("bufferSwitch @ " + Long.toString(System.currentTimeMillis()));
    for (AsioDriverListener listener : listeners) {
      listener.bufferSwitch(inputs, outputs);
    }
  }
  
  @SuppressWarnings("unused")
  private void fireBufferSwitch(double[][] inputs, double[][] outputs) {
    System.out.println("bufferSwitch @ " + Long.toString(System.currentTimeMillis()));
    for (AsioDriverListener listener : listeners) {
      listener.bufferSwitch(inputs, outputs);
    }
  }

}
