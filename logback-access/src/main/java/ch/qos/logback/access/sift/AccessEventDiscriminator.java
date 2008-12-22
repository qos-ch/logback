/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.access.sift;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.sift.Discriminator;
import ch.qos.logback.core.spi.ContextAwareBase;

public class AccessEventDiscriminator extends ContextAwareBase implements
    Discriminator<AccessEvent> {

  boolean started = false;

  /**
   * Allowed field names are: COOKIE, REQUEST_ATTRIBUTE, SESSION_ATTRIBUTE,
   * REMOTE_ADDRESS, LOCAL_PORT,REQUEST_URI
   * 
   * <p> The first three field names require a key attribute.
   */
  public enum FieldName {
    COOKIE, REQUEST_ATTRIBUTE, SESSION_ATTRIBUTE, REMOTE_ADDRESS, LOCAL_PORT, REQUEST_URI
  }

  String defaultValue;
  String key;
  FieldName fieldName;
  String optionalKey;

  public String getDiscriminatingValue(AccessEvent acccessEvent) {
    String rawValue = getRawDiscriminatingValue(acccessEvent);
    if (rawValue == null || rawValue.length() == 0) {
      return defaultValue;
    } else {
      return rawValue;
    }
  }

  public String getRawDiscriminatingValue(AccessEvent acccessEvent) {
    switch (fieldName) {
    case COOKIE:
      return acccessEvent.getCookie(optionalKey);
    case LOCAL_PORT:
      return String.valueOf(acccessEvent.getLocalPort());
    case REQUEST_ATTRIBUTE:
      return acccessEvent.getAttribute(optionalKey);
    case SESSION_ATTRIBUTE:
      return getSessionAttribute(acccessEvent);
    case REMOTE_ADDRESS:
      return acccessEvent.getRemoteAddr();
    case REQUEST_URI:
      return getRequestURI(acccessEvent);
    default:
      return null;
    }
  }

  private String getRequestURI(AccessEvent acccessEvent) {
    String uri = acccessEvent.getRequestURI();
    if (uri != null && uri.length() >= 1 && uri.charAt(0) == '/') {
      return uri.substring(1);
    } else {
      return uri;
    }
  }

  private String getSessionAttribute(AccessEvent acccessEvent) {
    HttpServletRequest req = acccessEvent.getRequest();
    if (req != null) {
      HttpSession session = req.getSession(false);
      if (session != null) {
        Object v = session.getAttribute(optionalKey);
        if (v != null) {
          return v.toString();
        }
      }
    }
    return null;
  }

  public boolean isStarted() {
    return started;
  }

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
      if (optionalKey == null) {
        addError("\"OptionalKey\" property is mandatory for field name "+fieldName.toString());
        errorCount++;
      }
    }

    if (errorCount == 0) {
      started = true;
    }
  }

  public void stop() {
    started = false;
  }

  public void setFieldName(FieldName fieldName) {
    this.fieldName = fieldName;
  }

  public FieldName getFieldName() {
    return fieldName;
  }

  public String getOptionalKey() {
    return optionalKey;
  }

  public void setOptionalKey(String optionalKey) {
    this.optionalKey = optionalKey;
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
