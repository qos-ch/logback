/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.action;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

public class IncludeAction extends Action {

  private static final String INCLUDED_TAG = "included";
  private static final String FILE_ATTR = "file";
  private static final String URL_ATTR = "url";
  private static final String RESOURCE_ATTR = "resource";
  private SaxEventRecorder recorder = new SaxEventRecorder();;

  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes)
      throws ActionException {

    String fileAttribute = attributes.getValue(FILE_ATTR);
    String urlAttribute = attributes.getValue(URL_ATTR);
    String resourceAttribute = attributes.getValue(RESOURCE_ATTR);
    String attributeInUse = null;

    if(!checkAttributes(fileAttribute, urlAttribute, resourceAttribute)) {
      return;
    }

    InputStream in = null;

    if (!OptionHelper.isEmpty(fileAttribute)) {
      attributeInUse = ec.subst(fileAttribute);
      in = getInputStreamByFilePath(attributeInUse);
    }

    if (!OptionHelper.isEmpty(urlAttribute)) {
      attributeInUse = ec.subst(urlAttribute);
      in = getInputStreamByUrl(attributeInUse);
    }

    if (!OptionHelper.isEmpty(resourceAttribute)) {
      attributeInUse = ec.subst(resourceAttribute);
      in = getInputStreamByResource(attributeInUse);
    }

    try {
      if (in != null) {
        parseAndRecord(in);
        in.close();
      }
    } catch (JoranException e) {
      addError("Error while parsing  " + attributeInUse, e);
    } catch (IOException e) {
      // called if in.close did not work
    }

    if (recorder.saxEventList.size() == 0) {
      return;
    }

    // Let's remove the two <included> events before
    // adding the events to the player.
    SaxEvent first = recorder.saxEventList.get(0);
    if (first != null && first.qName.equalsIgnoreCase(INCLUDED_TAG)) {
      recorder.saxEventList.remove(0);
    }

    SaxEvent last = recorder.saxEventList.get(recorder.saxEventList.size() - 1);
    if (last != null && last.qName.equalsIgnoreCase(INCLUDED_TAG)) {
      recorder.saxEventList.remove(recorder.saxEventList.size() - 1);
    }

    ec.getJoranInterpreter().addEvents(recorder.saxEventList);
  }

  private boolean checkAttributes(String fileAttribute,
      String urlAttribute, String resourceAttribute) {
    int count = 0;

    if (!OptionHelper.isEmpty(fileAttribute)) {
      count++;
    }
    if (!OptionHelper.isEmpty(urlAttribute)) {
      count++;
    }
    if (!OptionHelper.isEmpty(resourceAttribute)) {
      count++;
    }

    if (count == 0) {
      addError("One of \"path\", \"resource\" or \"url\" attributes must be set.");
      return false;
    } else if (count > 1) {
      addError("Only one of \"file\", \"url\" or \"resource\" attributes should be set.");
      return false;
    } else if (count == 1) {
      return true;
    }
    throw new IllegalStateException("Count value ["+count+"] is not expected");
  }

  private InputStream getInputStreamByFilePath(String pathToFile) {
    try {
      return new FileInputStream(pathToFile);
    } catch (IOException ioe) {
      String errMsg = "File [" + pathToFile + "] does not exist.";
      addError(errMsg, ioe);
      return null;
    }
  }

  private InputStream getInputStreamByUrl(String urlAttribute) {
    URL url;
    try {
      url = new URL(urlAttribute);
    } catch (MalformedURLException mue) {
      String errMsg = "URL [" + urlAttribute + "] is not well formed.";
      addError(errMsg, mue);
      return null;
    }
    return openURL(url);
  }

  InputStream openURL(URL url) {
    try {
      return url.openStream();
    } catch (IOException e) {
      String errMsg = "Failed to open [" + url.toString() + "]";
      addError(errMsg, e);
      return null;
    }
  }

  private InputStream getInputStreamByResource(String resourceAttribute) {
    URL url = Loader.getResourceByTCL(resourceAttribute);
    if (url == null) {
      String errMsg = "Could not find resource corresponding to ["
          + resourceAttribute + "]";
      addError(errMsg);
      return null;
    }
    return openURL(url);
  }

  private void parseAndRecord(InputStream inputSource) throws JoranException {
    recorder.setContext(context);
    recorder.recordEvents(inputSource);
  }

  @Override
  public void end(InterpretationContext ec, String name) throws ActionException {
    // do nothing
  }

}
