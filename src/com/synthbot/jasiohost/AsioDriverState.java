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

public enum AsioDriverState {
  /**
   * The driver is loaded into memory.
   */
  LOADED,
  
  /**
   * The driver it initialised and is ready to accept commands.
   */
  INITIALIZED,
  
  /**
   * The audio buffers have been created and configured.
   */
  PREPARED,
  
  /**
   * The driver is running and making calls to <code>bufferSwitch()</code>.
   */
  RUNNING;
  
  public boolean atLeastInState(AsioDriverState minimumState) {
    if (minimumState == null) {
      return false;
    } else {
      return minimumState.ordinal() <= this.ordinal();      
    }
  }
}
