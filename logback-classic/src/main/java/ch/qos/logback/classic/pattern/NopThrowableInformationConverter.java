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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;



/**
 * Always returns an empty string.
 * <p>
 * This converter is useful to pretend that the converter chain for
 * PatternLayout actually handles exceptions, when in fact it does not.
 * By adding %nopex to the conversion pattern, the user can bypass
 * the automatic addition of %ex conversion pattern for patterns 
 * which do not contain a converter handling exceptions.
 * 
 * <p>Users can ignore the existence of this converter, unless they
 * want to suppress the automatic printing of exceptions by 
 * {@link PatternLayout}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class NopThrowableInformationConverter extends ThrowableHandlingConverter {

  public String convert(ILoggingEvent event) {
    return CoreConstants.EMPTY_STRING;
  }
 
}
