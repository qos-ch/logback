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
package ch.qos.logback.access.sift;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;

/**
 * 
 * AccessEventDiscriminator's job is to return the value of a designated field
 * in an {@link IAccessEvent} instance.
 * 
 * <p>The field is specified via the {@link FieldName} property.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class AccessEventDiscriminator extends AbstractDiscriminator<IAccessEvent> {

    /**
     * At present time the followed fields can be designated: COOKIE,
     * REQUEST_ATTRIBUTE, SESSION_ATTRIBUTE, REMOTE_ADDRESS,
     * LOCAL_PORT,REQUEST_URI
     * 
     * <p> The first three fields require an additional key. For the
     * SESSION_ATTRIBUTE field, the additional key named "id" has special meaning
     * as it is mapped to the session id of the current http request.
     */
    public enum FieldName {
        COOKIE, REQUEST_ATTRIBUTE, SESSION_ATTRIBUTE, REMOTE_ADDRESS, LOCAL_PORT, REQUEST_URI
    }

    String defaultValue;
    String key;
    FieldName fieldName;
    String additionalKey;

    @Override
    public String getDiscriminatingValue(IAccessEvent acccessEvent) {
        String rawValue = getRawDiscriminatingValue(acccessEvent);
        if (rawValue == null || rawValue.length() == 0) {
            return defaultValue;
        } else {
            return rawValue;
        }
    }

    public String getRawDiscriminatingValue(IAccessEvent acccessEvent) {
        switch (fieldName) {
        case COOKIE:
            // tested
            return acccessEvent.getCookie(additionalKey);
        case LOCAL_PORT:
            return String.valueOf(acccessEvent.getLocalPort());
        case REQUEST_ATTRIBUTE:
            // tested
            return getRequestAttribute(acccessEvent);
        case SESSION_ATTRIBUTE:
            return getSessionAttribute(acccessEvent);
        case REMOTE_ADDRESS:
            return acccessEvent.getRemoteAddr();
        case REQUEST_URI:
            // tested
            return getRequestURI(acccessEvent);
        default:
            return null;
        }
    }

    private String getRequestAttribute(IAccessEvent acccessEvent) {
        String attr = acccessEvent.getAttribute(additionalKey);
        if (IAccessEvent.NA.equals(attr)) {
            return null;
        } else {
            return attr;
        }
    }

    private String getRequestURI(IAccessEvent acccessEvent) {
        String uri = acccessEvent.getRequestURI();
        if (uri != null && uri.length() >= 1 && uri.charAt(0) == '/') {
            return uri.substring(1);
        } else {
            return uri;
        }
    }

    private String getSessionAttribute(IAccessEvent acccessEvent) {
        HttpServletRequest req = acccessEvent.getRequest();
        if (req != null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                if ("id".equalsIgnoreCase(additionalKey)) {
                    return session.getId();
                } else {
                    Object v = session.getAttribute(additionalKey);
                    if (v != null) {
                        return v.toString();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void start() {

        int errorCount = 0;

        if (defaultValue == null) {
            addError("\"DefaultValue\" property must be set.");
        }
        if (fieldName == null) {
            addError("\"FieldName\" property must be set.");
            errorCount++;
        }

        switch (fieldName) {
        case SESSION_ATTRIBUTE:
        case REQUEST_ATTRIBUTE:
        case COOKIE:
            if (additionalKey == null) {
                addError("\"OptionalKey\" property is mandatory for field name " + fieldName.toString());
                errorCount++;
            }
        default:    
        }

        if (errorCount == 0) {
            started = true;
        }
    }

    public void setFieldName(FieldName fieldName) {
        this.fieldName = fieldName;
    }

    public FieldName getFieldName() {
        return fieldName;
    }

    public String getAdditionalKey() {
        return additionalKey;
    }

    public void setAdditionalKey(String additionalKey) {
        this.additionalKey = additionalKey;
    }

    /**
     * @see #setDefaultValue(String)
     * @return
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * The default value returned by this discriminator in case it cannot compute
     * the discriminating value from the access event.
     * 
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
