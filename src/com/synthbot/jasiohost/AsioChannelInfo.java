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

public class AsioChannelInfo {

  private final int index;
  private final boolean isInput;
  private final boolean isActive;
  private final int channelGroup;
  private final AsioSampleType sampleType;
  private final String name;
  
  protected AsioChannelInfo(int index, boolean isInput, boolean isActive, int channelGroup, AsioSampleType sampleType, String name) {
    this.index = index;
    this.isInput = isInput;
    this.isActive = isActive;
    this.channelGroup = channelGroup;
    this.sampleType = sampleType;
    this.name = name;
  }
  
  public int getChannelIndex() {
    return index;
  }
  
  public boolean isInput() {
    return isInput;
  }
  
  public boolean isActive() {
    return isActive;
  }
  
  public int getChannelGroup() {
    return channelGroup;
  }
  
  public AsioSampleType getSampleType() {
    return sampleType;
  }
  
  public String getChannelName() {
    return name;
  }
  
}
