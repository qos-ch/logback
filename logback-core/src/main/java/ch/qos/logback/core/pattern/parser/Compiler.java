/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.pattern.parser;

import java.util.Map;

import ch.qos.logback.core.pattern.CompositeConverter;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.util.OptionHelper;


class Compiler extends ContextAwareBase {

  Converter head;
  Converter tail;
  final Node top;
  final Map converterMap;
  
  Compiler(final Node top, final Map converterMap) {
    this.top = top;
    this.converterMap = converterMap;
  }

  Converter compile() {
    head = tail = null;
    for (Node n = top; n != null; n = n.next) {
      switch (n.type) {
      case Node.LITERAL:
        addToList(new LiteralConverter((String) n.getValue()));
        break;
      case Node.COMPOSITE:
        CompositeNode cn = (CompositeNode) n;
        CompositeConverter compositeConverter = new CompositeConverter();
        compositeConverter.setFormattingInfo(cn.getFormatInfo());
        Compiler childCompiler = new Compiler(cn.getChildNode(), converterMap);
        childCompiler.setContext(context);
        Converter childConverter = childCompiler.compile();
        compositeConverter.setChildConverter(childConverter);
        addToList(compositeConverter);
        break;
      case Node.KEYWORD:
        KeywordNode kn = (KeywordNode) n;
        DynamicConverter dynaConverter = createConverter(kn);
        if (dynaConverter != null) {
          dynaConverter.setFormattingInfo(kn.getFormatInfo());
          dynaConverter.setOptionList(kn.getOptions());
          addToList(dynaConverter);
        } else {
          // if the appropriate dynaconverter cannot be found, then replace
          // it with a dummy LiteralConverter indicating an error.
          Converter errConveter = new LiteralConverter("%PARSER_ERROR_"
              + kn.getValue());
          addStatus(new ErrorStatus("["+kn.getValue()+"] is not a valid conversion word", this));
          addToList(errConveter);
        }

      }
    }
    return head;
  }

  private void addToList(Converter c) {
    if (head == null) {
      head = tail = c;
    } else {
      tail.setNext(c);
      tail = c;
    }
  }

  
  /**
   * Attempt to create a converter using the information found in 'converterMap'.
   * @param kn
   * @return
   */
  DynamicConverter createConverter(KeywordNode kn) {
    String keyword = (String) kn.getValue();
    String converterClassStr = (String) converterMap.get(keyword);

    // FIXME: Better error handling
    if (converterClassStr != null) {
      try {
        return (DynamicConverter) OptionHelper.instantiateByClassName(
            converterClassStr, DynamicConverter.class, context);
      } catch (Exception e) {
        return null;
      }
    } else {
      return null;
    }
  }

//  public void setStatusManager(StatusManager statusManager) {
//    this.statusManager = statusManager;
//  }
//  
//  void addStatus(Status status) {
//    if(statusManager != null) {
//      statusManager.add(status);
//    }
//  }
}