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
  private SaxEventRecorder recorder;
  
  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes)
      throws ActionException {

    String attribute = attributes.getValue(FILE_ATTR);
    if (attribute == null) {
      addError("Path to configuration file to include is not set.");
      return;
    }
    
    String pathToFile;
    
    if (attribute.startsWith("$")) {
      pathToFile = ec.subst(attribute);
    } else {
      pathToFile = attribute;
    }

    try {
      InputStream in = new FileInputStream(pathToFile);
      parseAndRecord(in);
      in.close();
    } catch (IOException ioe) {
      String errMsg = "File [" + pathToFile + "] does not exist.";
      addError(errMsg, ioe);
    } catch (JoranException e) {
      addError("Error while parsing file " + pathToFile + e);
    }
    
    if (recorder.saxEventList.size() == 0) {
      return;
    }
    
    SaxEvent first = recorder.saxEventList.get(0);
    if (first != null && first.qName.equalsIgnoreCase(INCLUDED_TAG)) {
      recorder.saxEventList.remove(0);
    }

    SaxEvent last = recorder.saxEventList.get(recorder.saxEventList.size()-1);
    if (last != null && last.qName.equalsIgnoreCase(INCLUDED_TAG)) {
      recorder.saxEventList.remove(recorder.saxEventList.size()-1);
    }
    
    ec.getJoranInterpreter().addEvents(recorder.saxEventList);
  }
  
  private void parseAndRecord(InputStream inputSource) throws JoranException {
    recorder = new SaxEventRecorder();
    recorder.setContext(context);
    recorder.recordEvents(inputSource);
  }

  @Override
  public void end(InterpretationContext ec, String name) throws ActionException {
    // do nothing
  }

}
