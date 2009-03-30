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

public interface AsioDriverListener {

  public void sampleRateDidChange(double sampleRate);

  public void resetRequest();
  
  public void resyncRequest();
  
  public void latenciesChanged(int inputLatency, int outputLatency);

  /**
   * @param input  The input buffer is filled with new data from the driver.
   * @param output  The output buffer should be filled with data to be sent to the driver.
   */
  public void bufferSwitch(int[][] inputInt, int[][] outputInt,
		                   float[][] inputFloat, float[][] outputFloat,
		                   double[][] inputDouble, double[][] outputDouble);
}
