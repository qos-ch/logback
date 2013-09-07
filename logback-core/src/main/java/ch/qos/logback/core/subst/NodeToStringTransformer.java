/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Compiles a previously parsed Node chain into a String.
 *
 * @author Ceki G&uuml;c&uuml;
 */
public class NodeToStringTransformer {

  final Node node;
  final PropertyContainer propertyContainer0;
  final PropertyContainer propertyContainer1;

  public NodeToStringTransformer(Node node, PropertyContainer propertyContainer0, PropertyContainer propertyContainer1) {
    this.node = node;
    this.propertyContainer0 = propertyContainer0;
    this.propertyContainer1 = propertyContainer1;
  }

  public NodeToStringTransformer(Node node, PropertyContainer propertyContainer0) {
    this(node, propertyContainer0, null);
  }

  public static String substituteVariable(String input, PropertyContainer pc0, PropertyContainer pc1) throws ScanException {
    Tokenizer tokenizer = new Tokenizer(input);
    Parser parser = new Parser(tokenizer.tokenize());
    Node node = parser.parse();
    NodeToStringTransformer nodeToStringTransformer = new NodeToStringTransformer(node, pc0, pc1);
    return nodeToStringTransformer.transform();
  }

  public String transform() {
    StringBuilder stringBuilder = new StringBuilder();
    compileNode(node, stringBuilder);
    return stringBuilder.toString();
  }

  private void compileNode(Node inputNode, StringBuilder stringBuilder) {
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

    if (n.defaultPart == null) {
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
    if (value != null)
      return value;

    if (propertyContainer1 != null) {
      value = propertyContainer1.getProperty(key);
      if (value != null)
        return value;
    }

    value = OptionHelper.getSystemProperty(key, null);
    if (value != null)
      return value;

    value = OptionHelper.getEnv(key);
    if (value != null) {
      return value;
    }

    return null;
  }


  private void handleLiteral(Node n, StringBuilder stringBuilder) {
    stringBuilder.append((String) n.payload);
  }

}
