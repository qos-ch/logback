/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2012, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.subst;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.PropertyContainer;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class Compiler {


  final Node node;
  final PropertyContainer propertyContainer0;

  public Compiler(Node node, PropertyContainer propertyContainer0) {
    this.node = node;
    this.propertyContainer0 = propertyContainer0;
  }


  String compile() {
    StringBuilder stringBuilder = new StringBuilder();
    compileNode(node, stringBuilder);
    return stringBuilder.toString();

  }

  void compileNode(Node inputNode, StringBuilder stringBuilder) {
    Node n = inputNode;
    while (n != null) {
      switch (n.type) {
        case LITERAL:
          handleLiteral(n, stringBuilder);
          break;
        case VARIABLE:
          handleVariable(n, stringBuilder);
          break;
      }
      n = n.next;
    }
  }

  private void handleVariable(Node n, StringBuilder stringBuilder) {
    StringBuilder keyBuffer = new StringBuilder();
    Node payload = (Node) n.payload;
    compileNode(payload, keyBuffer);
    String key = keyBuffer.toString();
    String value = lookupKey(key);
    if (value != null) {
      stringBuilder.append(value);
      return;
    }

    if(n.defaultPart == null) {
      stringBuilder.append(key + CoreConstants.UNDEFINED_PROPERTY_SUFFIX);
      return;
    }

    Node defaultPart = (Node) n.defaultPart;
    StringBuilder defaultPartBuffer = new StringBuilder();
    compileNode(defaultPart, defaultPartBuffer);
    String defaultVal = defaultPartBuffer.toString();
    stringBuilder.append(defaultVal);
  }

  private String lookupKey(String key) {
    String value = propertyContainer0.getProperty(key);
    return value;
  }


  private void handleLiteral(Node n, StringBuilder stringBuilder) {
    stringBuilder.append((String) n.payload);
  }

}
