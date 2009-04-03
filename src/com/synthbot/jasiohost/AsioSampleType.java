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

/**
 * This class enumerates the different types of sample formats supported by ASIO.
 * The API makes a distinction between Big-Endian (denoted by <code>MSB</code>) and 
 * Little-Endian (<code>LSB</code>) encodings, which are meaningless in Java, as all 
 * numbers are Big-Endian encoded. <code>AsioSampleType</code>s are exposed for informative 
 * purposes, however they are most useful for determining the Java native type to use 
 * (<code>getJavaNativeType()</code>, and the number of bits per sample. The endianness of 
 * the sample is resolved in the native library.
 */
public enum AsioSampleType {
  
  /**
   * 16-bit data word
   */
  ASIOSTInt16MSB(0, JavaNativeType.SHORT),
  
  /**
   * This is the packed 24 bit format. 2 data words will spawn consecutive 6 bytes in memory.
   * (Used for 18 and 20 bits as well, if they use this packed format). In Java it is represented
   * the same as any of the <code>ASIOSTInt32MSB16</code> formats.
   */
  ASIOSTInt24MSB(1, JavaNativeType.INTEGER),
  
  /**
   * 32-bit data word.
   */
  ASIOSTInt32MSB(2, JavaNativeType.INTEGER),
  
  /**
   * IEEE 754 32 bit float, as found on Intel x86 architecture.
   */
  ASIOSTFloat32MSB(3, JavaNativeType.FLOAT),
  
  /**
   * IEEE 754 64 bit double float, as found on Intel x86 architecture
   */
  ASIOSTFloat64MSB(4, JavaNativeType.DOUBLE),
  
  /**
   * Sample data fills the least significant 16 bits, the other bits are sign extended.
   */
  ASIOSTInt32MSB16(8, JavaNativeType.INTEGER),
  
  /**
   * Sample data fills the least significant 18 bits, the other bits are sign extended.
   */
  ASIOSTInt32MSB18(9, JavaNativeType.INTEGER),
  
  /**
   * Sample data fills the least significant 20 bits, the other bits are sign extended.
   */
  ASIOSTInt32MSB20(10 ,JavaNativeType.INTEGER),
  
  /**
   * Sample data fills the least significant 24 bits, the other bits are sign extended.
   */
  ASIOSTInt32MSB24(11, JavaNativeType.INTEGER),
  ASIOSTInt16LSB(16, JavaNativeType.SHORT),
  ASIOSTInt24LSB(17, JavaNativeType.INTEGER),
  ASIOSTInt32LSB(18, JavaNativeType.INTEGER),
  ASIOSTFloat32LSB(19, JavaNativeType.FLOAT),
  ASIOSTFloat64LSB(20, JavaNativeType.DOUBLE),
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
