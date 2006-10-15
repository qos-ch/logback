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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

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

// Based on 310985 revision 310985 as attested by http://tinyurl.com/8njps
// see also http://tinyurl.com/c2rp5

/**
 * A JoranConfiguratorBase lays most of the groundwork for concrete
 * configurators derived from it. Concrete configurators only need to implement
 * the {@link #addInstanceRules} method.
 * <p>
 * A JoranConfiguratorBase instance should not be used more than once to
 * configure a Context.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
abstract public class JoranConfiguratorBase extends GenericConfigurator {
  

  final public void xdoConfigure(final InputSource inputSource) {
    // This line is needed here because there is logging from inside this
    // method.
    buildInterpreter();

    ExecutionContext ec = interpreter.getExecutionContext();

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
      saxParser.parse(inputSource, interpreter);
    } catch (IOException ie) {
      final String errMsg = "I/O error occurred while parsing xml file";
      ec.addError(errMsg, ie);
    } catch (Exception ex) {
      final String errMsg = "Problem parsing XML document. See previously reported errors. Abandoning all further processing.";
      addError(errMsg, ex);
      return;
    } finally {
      // detachListAppender(repository);
    }
  }

  public List getErrorList() {
    return null;
  }

  @Override
  protected void addInstanceRules(RuleStore rs) {
    rs.addRule(new Pattern("configuration/substitutionProperty"),
        new SubstitutionPropertyAction());
    rs.addRule(new Pattern("configuration/repositoryProperty"),
        new RepositoryPropertyAction());
    rs.addRule(new Pattern("configuration/conversionRule"),
        new ConversionRuleAction());

    rs.addRule(new Pattern("configuration/appender"), new AppenderAction());
    rs.addRule(new Pattern("configuration/appender/appender-ref"),
        new AppenderRefAction());
    rs.addRule(new Pattern("configuration/newRule"), new NewRuleAction());
    rs.addRule(new Pattern("*/param"), new ParamAction());
  }

  @Override
  protected void addImpliciutRules(Interpreter interpreter) {
    // The following line adds the capability to parse nested components
    NestedComponentIA nestedIA = new NestedComponentIA();
    nestedIA.setContext(context);
    interpreter.addImplicitAction(nestedIA);

    NestedSimplePropertyIA nestedSimpleIA = new NestedSimplePropertyIA();
    nestedIA.setContext(context);
    interpreter.addImplicitAction(nestedSimpleIA);
  }

  @Override
  protected void buildInterpreter() {
    super.buildInterpreter();
    Map<String, Object> omap = interpreter.getExecutionContext().getObjectMap();
    omap.put(ActionConst.APPENDER_BAG, new HashMap());
    omap.put(ActionConst.FILTER_CHAIN_BAG, new HashMap());
  }

  public ExecutionContext getExecutionContext() {
    return interpreter.getExecutionContext();
  }
}
