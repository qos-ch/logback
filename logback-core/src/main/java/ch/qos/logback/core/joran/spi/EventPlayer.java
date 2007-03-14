/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import java.util.List;

import ch.qos.logback.core.joran.event.BodyEvent;
import ch.qos.logback.core.joran.event.EndEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;

public class EventPlayer {

  final Interpreter interpreter;
  List<SaxEvent> eventList;
  int currentIndex;

  public EventPlayer(Interpreter interpreter) {
    this.interpreter = interpreter; 
  }
  
  public void play(List<SaxEvent> seList) {
    eventList = seList;
    SaxEvent se;
    for(currentIndex = 0; currentIndex < eventList.size(); currentIndex++) {
      se = eventList.get(currentIndex);
      
      if(se instanceof StartEvent) {
        interpreter.startElement((StartEvent) se);
        // invoke fireInPlay after startElement processing
        interpreter.getExecutionContext().fireInPlay(se);
      }
      if(se instanceof BodyEvent) {
        // invoke fireInPlay before  characters processing
        interpreter.getExecutionContext().fireInPlay(se);
        interpreter.characters((BodyEvent) se);
      }
      if(se instanceof EndEvent) {
        // invoke fireInPlay before endElement processing
        interpreter.getExecutionContext().fireInPlay(se);
        interpreter.endElement((EndEvent) se);
      }
    
    }
  }
  
  public void addEvents(List<SaxEvent> eventList) {
    this.eventList.addAll(currentIndex+2, eventList);
  }
}
