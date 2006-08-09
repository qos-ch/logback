/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import org.slf4j.Marker;

public class Util {

  static public boolean match(Marker marker, Marker[] markerArray) {
    if(markerArray == null) {
      throw new IllegalArgumentException("markerArray should not be null");
    }
    
    //System.out.println("event marker="+marker);
    
    final int size = markerArray.length;
    for(int i = 0; i < size; i++) {
      //System.out.println("other:"+markerArray[i]);
      
      if(marker.contains(markerArray[i])) {
        return true;
      }
    }
    return false;
  }
}
