/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.joran;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.xml.sax.InputSource;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.EventPlayer;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;
import ch.qos.logback.core.spi.ContextAwareBase;

public abstract class GenericConfigurator extends ContextAwareBase {

  protected Interpreter interpreter;

  final public void doConfigure(URL url) throws JoranException {
    try {
      informContextOfURLUsedForConfiguration(url);
      URLConnection urlConnection = url.openConnection();
      // per http://jira.qos.ch/browse/LBCORE-105
      // per http://jira.qos.ch/browse/LBCORE-127
      urlConnection.setUseCaches(false);
      
      InputStream in = urlConnection.getInputStream();
      doConfigure(in);
      in.close();
    } catch (IOException ioe) {
      String errMsg = "Could not open URL [" + url + "].";
      addError(errMsg, ioe);
      throw new JoranException(errMsg, ioe);
    }
  }

  final public void doConfigure(String filename) throws JoranException {
    doConfigure(new File(filename));
  }

  final public void doConfigure(File file) throws JoranException {
    FileInputStream fis = null;
    try {
      informContextOfURLUsedForConfiguration(file.toURI().toURL());
      fis = new FileInputStream(file);
      doConfigure(fis);
    } catch (IOException ioe) {
      String errMsg = "Could not open [" + file.getName() + "].";
      addError(errMsg, ioe);
      throw new JoranException(errMsg, ioe);
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (java.io.IOException ioe) {
          String errMsg = "Could not close [" + file.getName() + "].";
          addError(errMsg, ioe);
          throw new JoranException(errMsg, ioe);
        }
      }
    }
  }

  protected void informContextOfURLUsedForConfiguration(URL url) {
    getContext().putObject(CoreConstants.URL_OF_LAST_CONFIGURATION_VIA_JORAN, url);
  }
  
  final public void doConfigure(InputStream inputStream) throws JoranException {
    doConfigure(new InputSource(inputStream));
  }

  abstract protected void addInstanceRules(RuleStore rs);

  abstract protected void addImplicitRules(Interpreter interpreter);

  protected void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {
    
  }
  
  protected Pattern initialPattern() {
    return new Pattern();
  }
  
  protected void buildInterpreter() {
    RuleStore rs = new SimpleRuleStore(context);
    addInstanceRules(rs);
    this.interpreter = new Interpreter(context, rs, initialPattern());
    InterpretationContext ec = interpreter.getInterpretationContext();
    ec.setContext(context);
    addImplicitRules(interpreter);
    addDefaultNestedComponentRegistryRules(ec.getDefaultNestedComponentRegistry());
  }

  final public void doConfigure(final InputSource inputSource)
      throws JoranException {
    SaxEventRecorder recorder = new SaxEventRecorder();
    recorder.setContext(context);
    recorder.recordEvents(inputSource);
    buildInterpreter();
    // disallow simultaneous configurations of the same context
    synchronized (context.getConfigurationLock()) {
      interpreter.play(recorder.saxEventList);
    }
  }

  public void doConfigure(final List<SaxEvent> eventList)
      throws JoranException {
    buildInterpreter();
    EventPlayer player = new EventPlayer(interpreter);
    player.play(eventList);
  }
}
