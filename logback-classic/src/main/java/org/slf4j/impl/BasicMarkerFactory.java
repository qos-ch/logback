/* 
 * Copyright (c) 2004-2005 SLF4J.ORG
 * Copyright (c) 2004-2005 QOS.ch
 *
 * All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, and/or sell copies of  the Software, and to permit persons
 * to whom  the Software is furnished  to do so, provided  that the above
 * copyright notice(s) and this permission notice appear in all copies of
 * the  Software and  that both  the above  copyright notice(s)  and this
 * permission notice appear in supporting documentation.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR  A PARTICULAR PURPOSE AND NONINFRINGEMENT
 * OF  THIRD PARTY  RIGHTS. IN  NO EVENT  SHALL THE  COPYRIGHT  HOLDER OR
 * HOLDERS  INCLUDED IN  THIS  NOTICE BE  LIABLE  FOR ANY  CLAIM, OR  ANY
 * SPECIAL INDIRECT  OR CONSEQUENTIAL DAMAGES, OR  ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS  OF USE, DATA OR PROFITS, WHETHER  IN AN ACTION OF
 * CONTRACT, NEGLIGENCE  OR OTHER TORTIOUS  ACTION, ARISING OUT OF  OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * Except as  contained in  this notice, the  name of a  copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use
 * or other dealings in this Software without prior written authorization
 * of the copyright holder.
 *
 */

package org.slf4j.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;

/**
 * An almost trivial implementation of the {@link IMarkerFactory}
 * interface which creates {@link BasicMarker} instances.
 * 
 * <p>Simple logging systems can conform to the SLF4J API by binding
 * {@link org.slf4j.MarkerFactory} with an instance of this class.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class BasicMarkerFactory implements IMarkerFactory {

  Map<String, Marker> markerMap = new HashMap<String, Marker>();
  
  /**
   * Regular users should <em>not</em> create
   * <code>BasicMarkerFactory</code> instances. <code>Marker</code>
   * instances can be obtained using the static {@link
   * org.slf4j.MarkerFactory#getMarker} method.
   */
  public BasicMarkerFactory() {
  }

  /**
   * Manufacture a {@link BasicMarker} instance by name. If the instance has been 
   * created earlier, return the previously created instance. 
   * 
   * @param name the name of the marker to be created
   * @return a Marker instance
   */
  public synchronized Marker getMarker(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Marker name cannot be null");
    }

    Marker marker = (Marker) markerMap.get(name);
    if (marker == null) {
      marker = new BasicMarker(name, this);
      markerMap.put(name, marker);
    }
    return marker;
  }
  
  /**
   * Does the name marked already exist?
   */
  public synchronized boolean exists(String name) {
    if (name == null) {
      return false;
    }
    return markerMap.containsKey(name);
  }
  
}
