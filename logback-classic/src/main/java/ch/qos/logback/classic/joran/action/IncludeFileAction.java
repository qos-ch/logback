/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.joran.action;

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

public class IncludeFileAction extends Action {

  private static final String INCLUDED_TAG = "included";
  private static final String FILE_ATTR = "file";
  private static final String URL_ATTR = "url";
  private SaxEventRecorder recorder = new SaxEventRecorder();;

  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes)
      throws ActionException {

    String attFile = attributes.getValue(FILE_ATTR);
    String attUrl = attributes.getValue(URL_ATTR);
    // if both are null, report error and do nothing.
    if (attFile == null && attUrl == null) {
      addError("One of path and URL attribute must be set.");
      return;
    }

    String pathToFile = null;
    if (attFile != null) {
      if (attFile.startsWith("$")) {
        pathToFile = ec.subst(attFile);
      } else {
        pathToFile = attFile;
      }
    }

    URL urlToFile = null;
    String tmpUrl;
    if (attUrl != null) {
      if (attUrl.startsWith("$")) {
        tmpUrl = ec.subst(attUrl);
      } else {
        tmpUrl = attUrl;
      }
      try {
        urlToFile = new URL(tmpUrl);
      } catch (MalformedURLException mue) {
        String errMsg = "URL [" + tmpUrl + "] is not well formed.";
        addError(errMsg, mue);
        return;
      }
    }

    // we know now that either pathToFile or urlToFile
    // is not null and correctly formed (in case of urlToFile).

    try {
      InputStream in = getInputStream(pathToFile, urlToFile);
      if (in != null) {
        parseAndRecord(in);
        in.close();
      }
    } catch (JoranException e) {
      addError("Error while parsing file " + pathToFile + e);
    } catch (IOException e) {
      // called if in.close did not work
    }

    if (recorder.saxEventList.size() == 0) {
      return;
    }

    //Let's remove the two <included> events before
    //adding the events to the player.
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

  private InputStream getInputStream(String pathToFile, URL urlToFile) {
    if (pathToFile != null) {
      try {
        return new FileInputStream(pathToFile);
      } catch (IOException ioe) {
        String errMsg = "File [" + pathToFile + "] does not exist.";
        addError(errMsg, ioe);
        return null;
      }
    } else {
      try {
        return urlToFile.openStream();
      } catch (IOException e) {
        String errMsg = "URL [" + urlToFile.toString() + "] does not exist.";
        addError(errMsg, e);
        return null;
      }
    }
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
