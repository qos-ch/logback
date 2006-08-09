/*
 * Copyright (c) 2004-2005 SLF4J.ORG
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
package org.slf4j;

import org.slf4j.impl.StaticMarkerBinder;
import org.slf4j.impl.Util;

/**
 * MarkerFactory is a utility class producing {@link Marker} instances as
 * appropriate for the logging system currently in use.
 * 
 * <p>
 * This class is essentially implemented as a wrapper around an
 * {@link IMarkerFactory} instance bound at compile time.
 * 
 * <p>
 * Please note that all methods in this class are static.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class MarkerFactory {
  static IMarkerFactory markerFactory;

  private MarkerFactory() {
  }

  static {
    try {
      markerFactory = StaticMarkerBinder.SINGLETON.getMarkerFactory();
    } catch (Exception e) {
      // we should never get here
      Util.reportFailure("Could not instantiate instance of class ["
          + StaticMarkerBinder.SINGLETON.getMarkerFactoryClassStr() + "]", e);
    }
  }

  /**
   * Return a Marker instance as specified by the name parameter using the
   * previously bound {@link IMarkerFactory}instance.
   * 
   * @param name
   *          The name of the {@link Marker} object to return.
   * @return marker
   */
  public static Marker getMarker(String name) {
    return markerFactory.getMarker(name);
  }

  /**
   * Return the {@link IMarkerFactory}instance in use.
   * 
   * <p>The IMarkerFactory instance is usually bound with this class at 
   * compile time.
   * 
   * @return the IMarkerFactory instance in use
   */
  public static IMarkerFactory getIMarkerFactory() {
    return markerFactory;
  }
}