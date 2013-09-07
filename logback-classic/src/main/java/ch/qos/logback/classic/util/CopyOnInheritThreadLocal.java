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
package ch.qos.logback.classic.util;

import java.util.HashMap;

/**
 * This class extends InheritableThreadLocal so that children threads get a copy
 * of the parent's hashmap.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class CopyOnInheritThreadLocal extends
    InheritableThreadLocal<HashMap<String, String>> {

  /**
   * Child threads should get a copy of the parent's hashmap.
   */
  @Override
  protected HashMap<String, String> childValue(
      HashMap<String, String> parentValue) {
    if (parentValue == null) {
      return null;
    } else {
      return new HashMap<String, String>(parentValue);
    }
  }

}
