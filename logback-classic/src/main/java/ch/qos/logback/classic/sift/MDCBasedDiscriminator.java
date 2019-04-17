/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;
import ch.qos.logback.core.util.OptionHelper;

import java.util.Map;

/**
 * MDCBasedDiscriminator essentially returns the value mapped to an MDC key. If
 * the said value is null, then a default value is returned.
 * <p/>
 * <p>Both Key and the DefaultValue are user specified properties.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class MDCBasedDiscriminator extends AbstractDiscriminator<ILoggingEvent> {

    private String key;
    private String defaultValue;

    /**
     * Return the value associated with an MDC entry designated by the Key
     * property. If that value is null, then return the value assigned to the
     * DefaultValue property.
     */
    public String getDiscriminatingValue(ILoggingEvent event) {
        // http://jira.qos.ch/browse/LBCLASSIC-213
        Map<String, String> mdcMap = event.getMDCPropertyMap();
        if (mdcMap == null) {
            return defaultValue;
        }
        String mdcValue = mdcMap.get(key);
        if (mdcValue == null) {
            return defaultValue;
        } else {
            return mdcValue;
        }
    }

    @Override
    public void start() {
        int errors = 0;
        if (OptionHelper.isNullOrEmpty(key)) {
            errors++;
            addError("The \"Key\" property must be set");
        }
        if (OptionHelper.isNullOrEmpty(defaultValue)) {
            errors++;
            addError("The \"DefaultValue\" property must be set");
        }
        if (errors == 0) {
            started = true;
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return
     * @see #setDefaultValue(String)
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * The default MDC value in case the MDC is not set for
     * {@link #setKey(String) mdcKey}.
     * <p/>
     * <p> For example, if {@link #setKey(String) Key} is set to the value
     * "someKey", and the MDC is not set for "someKey", then this appender will
     * use the default value, which you can set with the help of this method.
     *
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
