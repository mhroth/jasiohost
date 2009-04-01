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
  ASIOSTInt16MSB(0, JavaNativeType.SHORT),
  ASIOSTInt24MSB(1, JavaNativeType.INTEGER), // used for 18, 20 bits as well
  ASIOSTInt32MSB(2, JavaNativeType.INTEGER),
  ASIOSTFloat32MSB(3, JavaNativeType.FLOAT), // IEEE 754 32 bit float
  ASIOSTFloat64MSB(4, JavaNativeType.DOUBLE), // IEEE 754 64 bit double float
  ASIOSTInt32MSB16(8, JavaNativeType.INTEGER),
  ASIOSTInt32MSB18(9, JavaNativeType.INTEGER),
  ASIOSTInt32MSB20(10 ,JavaNativeType.INTEGER),
  ASIOSTInt32MSB24(11, JavaNativeType.INTEGER),
  ASIOSTInt16LSB(16, JavaNativeType.SHORT),
  ASIOSTInt24LSB(17, JavaNativeType.INTEGER),
  ASIOSTInt32LSB(18, JavaNativeType.INTEGER),
  ASIOSTFloat32LSB(19, JavaNativeType.FLOAT), // IEEE 754 32 bit float, as found on Intel x86 architecture
  ASIOSTFloat64LSB(20, JavaNativeType.DOUBLE), // IEEE 754 64 bit double float, as found on Intel x86 architecture
  ASIOSTInt32LSB16(24, JavaNativeType.INTEGER),
  ASIOSTInt32LSB18(25, JavaNativeType.INTEGER),
  ASIOSTInt32LSB20(26, JavaNativeType.INTEGER),
  ASIOSTInt32LSB24(27, JavaNativeType.INTEGER),
  ASIOSTDSDInt8LSB1(32, JavaNativeType.BYTE),
  ASIOSTDSDInt8MSB1(33, JavaNativeType.BYTE),
  ASIOSTDSDInt8NER8(40, JavaNativeType.BYTE);
  
  private int nativeEnum; // the native enum representing this type
  private JavaNativeType nativeType; // the java type representing this sample type
  
  private AsioSampleType(int nativeEnum, JavaNativeType nativeType) {
    this.nativeEnum = nativeEnum;
    this.nativeType = nativeType;
  }
  
  public static AsioSampleType getSampleType(int nativeEnum) {
    for (AsioSampleType sampleType : values()) {
      if (sampleType.nativeEnum == nativeEnum) {
        return sampleType;
      }
    }
    return null;
  }
  
  /**
   * Returns the Java number type which represents this sample type.
   */
  public JavaNativeType getJavaNativeType() {
    return nativeType;
  }
  
  public enum JavaNativeType {
    BYTE,
    SHORT,
    INTEGER,
    FLOAT,
    DOUBLE;
  }
  
}
