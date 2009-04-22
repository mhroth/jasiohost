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

import java.util.ArrayList;
import java.util.List;

public class JAsioHost {

  private static AsioDriver asioDriver;

  private JAsioHost() {
    // no instance of this class allowed
  }

  static {
    System.loadLibrary("jasiohost");
  }

  /**
   * The designated ASIO driver is loaded and returned in the INITIALIZED state. If a driver is
   * already loaded and it is the named driver, then that driver object is returned. If the named
   * driver is different from the one currently loaded, then the currently loaded driver is shut
   * down and a new one is instantiated.
   * 
   * @param driverName  The name of the driver to load, as returned by <code>getDriverNames</code>.
   * @return The named AsioDriver object.
   */
  public static AsioDriver getAsioDriver(String driverName) {
    if (driverName == null) {
      throw new NullPointerException("The driver name cannot be null.");
    }
    if (JAsioHost.getCurrentDriverName().equals(driverName)) {
      return asioDriver;
    } else {
      if (!getDriverNames().contains(driverName)) {
        throw new IllegalArgumentException(
            "The given driver name does not exist in the system registry. "
                + "Check getDriverNames().");
      }

      JAsioHost.shutdownAndUnloadDriver();
      boolean driverLoaded = loadDriver(driverName);
      if (!driverLoaded) {
        throw new AsioException("The driver was not successfully loaded into memory. No information" +
        		"as to why is available.");
      }
      asioDriver = new AsioDriver();
      return asioDriver;
    }
  }
  
  /**
   * Returns the currently loaded <code>AsioDriver</code>.
   * @return  The current <code>AsioDriver</code> object. May be <code>null</code> 
   * if no driver is loaded.
   */
  public static AsioDriver getCurrentDriver() {
    return asioDriver;
  }

  /**
   * Loads the named driver into memory.
   * @param driverName  The ASIO driver to load.
   * @return  True if the driver was successfully loaded. False otherwise.
   */
  private static native boolean loadDriver(String driverName);

  private static native void removeCurrentDriver();

  /**
   * A list of all (maximum 32) ASIO drivers registered with the system is returned.
   */
  public static List<String> getDriverNames() {
    String[] driverNames = new String[32];
    int numNames = getDriverNames(driverNames);
    List<String> nameList = new ArrayList<String>(numNames);
    for (int i = 0; i < numNames; i++) {
      nameList.add(driverNames[i]);
    }
    return nameList;
  }
  private static native int getDriverNames(String[] driverNames);

  public static native String getCurrentDriverName();

  public static native int getCurrentDriverIndex();

  /**
   * Shutdown the currently loaded ASIO driver, regardless of what state it is in. Unload it from
   * memory. If no driver is loaded, then this method has no effect.
   */
  public static void shutdownAndUnloadDriver() {
    if (asioDriver != null) {
      asioDriver.returnToState(AsioDriverState.LOADED);
      asioDriver = null;
      JAsioHost.removeCurrentDriver();
    }
  }
}
