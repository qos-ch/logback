/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.sift;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.spi.DefaultClass;
import ch.qos.logback.core.sift.Discriminator;
import ch.qos.logback.core.sift.SiftingAppenderBase;
import org.slf4j.Marker;

/**
 * This appender can contains other appenders which it can build dynamically
 * depending on MDC values. The built appender is specified as part of a
 * configuration file.
 * 
 * <p>See the logback manual for further details.
 * 
 * 
 * @author Ceki Gulcu
 */
public class SiftingAppender extends SiftingAppenderBase<ILoggingEvent> {

  @Override
  protected long getTimestamp(ILoggingEvent event) {
    return event.getTimeStamp();
  }
  

  @Override
  @DefaultClass(MDCBasedDiscriminator.class)
  public void setDiscriminator(Discriminator<ILoggingEvent> discriminator) {
    super.setDiscriminator(discriminator);
  }

  protected boolean eventMarksEndOfLife(ILoggingEvent event) {
    Marker marker = event.getMarker();
    if(marker == null)
      return false;

    return marker.contains(ClassicConstants.FINALIZE_SESSION_MARKER);
  }
}
