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
        interpreter.getInterpretationContext().fireInPlay(se);
      }
      if(se instanceof BodyEvent) {
        // invoke fireInPlay before  characters processing
        interpreter.getInterpretationContext().fireInPlay(se);
        interpreter.characters((BodyEvent) se);
      }
      if(se instanceof EndEvent) {
        // invoke fireInPlay before endElement processing
        interpreter.getInterpretationContext().fireInPlay(se);
        interpreter.endElement((EndEvent) se);
      }
    
    }
  }
  
  public void addEventsDynamically(List<SaxEvent> eventList) {
    this.eventList.addAll(currentIndex+2, eventList);
  }
}
