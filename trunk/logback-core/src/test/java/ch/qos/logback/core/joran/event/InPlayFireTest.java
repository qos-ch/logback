/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.event;

import java.util.HashMap;

import junit.framework.TestCase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.TrivialConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.Constants;

public class InPlayFireTest extends TestCase {

  Context context = new ContextBase();
  HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();

  public void testBasic() throws JoranException {
    ListenAction listenAction = new ListenAction();
    
    rulesMap.put(new Pattern("fire"), listenAction);
    TrivialConfigurator gc = new TrivialConfigurator(rulesMap);

    gc.setContext(context);
    gc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/fire1.xml");
    
    //for(SaxEvent se: listenAction.getSeList()) {
    //  System.out.println(se);
    //}
    assertEquals(5, listenAction.getSeList().size());
    assertTrue(listenAction.getSeList().get(0) instanceof StartEvent);
    assertTrue(listenAction.getSeList().get(1) instanceof StartEvent);
    assertTrue(listenAction.getSeList().get(2) instanceof BodyEvent);
    assertTrue(listenAction.getSeList().get(3) instanceof EndEvent);
  }

  public void testReplay() throws JoranException {
    ListenAction listenAction = new ListenAction();
    
    rulesMap.put(new Pattern("fire"), listenAction);
    TrivialConfigurator gc = new TrivialConfigurator(rulesMap);

    gc.setContext(context);
    gc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/fire1.xml");
    
//    for(SaxEvent se: listenAction.getSeList()) {
//      System.out.println(se);
//    }
    assertEquals(5, listenAction.getSeList().size());
    assertTrue(listenAction.getSeList().get(0) instanceof StartEvent);
    assertTrue(listenAction.getSeList().get(1) instanceof StartEvent);
    assertTrue(listenAction.getSeList().get(2) instanceof BodyEvent);
    assertTrue(listenAction.getSeList().get(3) instanceof EndEvent);
  }
  
  
  
}
