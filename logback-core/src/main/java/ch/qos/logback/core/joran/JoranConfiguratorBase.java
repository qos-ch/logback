/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
 
package ch.qos.logback.core.joran;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.action.AppenderAction;
import ch.qos.logback.core.joran.action.AppenderRefAction;
import ch.qos.logback.core.joran.action.ConversionRuleAction;
import ch.qos.logback.core.joran.action.NestedComponentIA;
import ch.qos.logback.core.joran.action.NestedSimplePropertyIA;
import ch.qos.logback.core.joran.action.NewRuleAction;
import ch.qos.logback.core.joran.action.ParamAction;
import ch.qos.logback.core.joran.action.RepositoryPropertyAction;
import ch.qos.logback.core.joran.action.SubstitutionPropertyAction;
import ch.qos.logback.core.joran.spi.ExecutionContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;
import ch.qos.logback.core.spi.ContextAwareBase;


// Based on 310985 revision  310985 as attested by http://tinyurl.com/8njps
// see also http://tinyurl.com/c2rp5

/**
 * A JoranConfiguratorBase lays most of the groundwork for concrete 
 * configurators derived from it. Concrete configurators only need to
 * implement the {@link #addInstanceRules} method.
 * <p>
 * A JoranConfiguratorBase instance should not be used more than once to
 * configure a Context.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
abstract public class JoranConfiguratorBase extends ContextAwareBase {
  Interpreter joranInterpreter;
  boolean listAppnderAttached = false;

  
  final public void doConfigure(URL url) {
    try {
      InputStream in = url.openStream();
      doConfigure(in);
      in.close();
    } catch (IOException ioe) {
      String errMsg = "Could not open URL [" + url + "].";
      addError(errMsg, ioe);
    }
  }
  
  final public void doConfigure(String filename) {
    doConfigure(new File(filename));
  }

  final public void doConfigure(File file) {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      doConfigure(fis);
    } catch (IOException ioe) {
      String errMsg = "Could not open [" + file.getName() + "].";
      addError(errMsg, ioe);
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (java.io.IOException ioe) {
          addError(
            "Could not close [" + file.getName() + "].", ioe);
        }
      }
    }
  }

  final public void doConfigure(InputStream inputStream) {
    doConfigure(new InputSource(inputStream));
  }

  final public void doConfigure(final InputSource inputSource) {
    // This line is needed here because there is logging from inside this method.
    selfInitialize(this.context);
    
    ExecutionContext ec = joranInterpreter.getExecutionContext();


    SAXParser saxParser = null;
    try {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setNamespaceAware(true);
        saxParser = spf.newSAXParser();
    } catch (Exception pce) {
      String errMsg = "Parser configuration error occured";
      ec.addError(errMsg, pce);
      return;
    }
    
    try {
      // attachListAppender(context);
      saxParser.parse(inputSource, joranInterpreter);
    } catch(IOException ie) {
      final String errMsg = "I/O error occurred while parsing xml file";
      ec.addError(errMsg, ie);
    } catch (Exception ex) {
      final String errMsg = "Problem parsing XML document. See previously reported errors. Abandoning all further processing.";
      addError(errMsg, ex);
      return;
    } finally {
      //detachListAppender(repository);
    }    
  }

  public List getErrorList() {
    return null;
  }

  abstract public void addInstanceRules(RuleStore rs);
  
  protected void selfInitialize(Context context) {
    RuleStore rs = new SimpleRuleStore(context);
    addInstanceRules(rs);
   
    rs.addRule(
      new Pattern("configuration/substitutionProperty"),
      new SubstitutionPropertyAction());
    rs.addRule(
      new Pattern("configuration/repositoryProperty"),
      new RepositoryPropertyAction());
    rs.addRule(
        new Pattern("configuration/conversionRule"),
        new ConversionRuleAction());
  
    rs.addRule(
      new Pattern("configuration/appender"), new AppenderAction());
    rs.addRule(new Pattern("configuration/appender/appender-ref"), 
        new AppenderRefAction());
    rs.addRule(
      new Pattern("configuration/newRule"), new NewRuleAction());
    rs.addRule(new Pattern("*/param"), new ParamAction());
    
    joranInterpreter = new Interpreter(rs);
    ExecutionContext ec = joranInterpreter.getExecutionContext();
    ec.setContext(context);
    
    // The following line adds the capability to parse nested components
    NestedComponentIA nestedIA = new NestedComponentIA();
    nestedIA.setContext(context);
    joranInterpreter.addImplicitAction(nestedIA);

    NestedSimplePropertyIA nestedSimpleIA = new NestedSimplePropertyIA();
    nestedIA.setContext(context);
    joranInterpreter.addImplicitAction(nestedSimpleIA);

    Map<String, Object> omap = ec.getObjectMap();
    omap.put(ActionConst.APPENDER_BAG, new HashMap());
    omap.put(ActionConst.FILTER_CHAIN_BAG, new HashMap());
  }

  public ExecutionContext getExecutionContext() {
    return joranInterpreter.getExecutionContext();
  }
}
