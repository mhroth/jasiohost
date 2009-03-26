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

public enum AsioSampleType {
  ASIOSTInt16MSB(0),
  ASIOSTInt24MSB(1), // used for 20 bits as well
  ASIOSTInt32MSB(2),
  ASIOSTFloat32MSB(3), // IEEE 754 32 bit float
  ASIOSTFloat64MSB(4), // IEEE 754 64 bit double float
  ASIOSTInt32MSB16(8),
  ASIOSTInt32MSB18(9),
  ASIOSTInt32MSB20(10),
  ASIOSTInt32MSB24(11),
  ASIOSTInt16LSB(16),
  ASIOSTInt24LSB(17),
  ASIOSTInt32LSB(18),
  ASIOSTFloat32LSB(19), // IEEE 754 32 bit float, as found on Intel x86 architecture
  ASIOSTFloat64LSB(20), // IEEE 754 64 bit double float, as found on Intel x86 architecture
  ASIOSTInt32LSB16(24),
  ASIOSTInt32LSB18(25),
  ASIOSTInt32LSB20(26),
  ASIOSTInt32LSB24(27),
  ASIOSTDSDInt8LSB1(32),
  ASIOSTDSDInt8MSB1(33),
  ASIOSTDSDInt8NER8(40);
  
  private int nativeEnum;
  
  private AsioSampleType(int nativeEnum) {
    this.nativeEnum = nativeEnum;
  }
  
  public static AsioSampleType getSampleType(int nativeEnum) {
    for (AsioSampleType sampleType : values()) {
      if (sampleType.nativeEnum == nativeEnum) {
        return sampleType;
      }
    }
    return null;
  }
  
}
