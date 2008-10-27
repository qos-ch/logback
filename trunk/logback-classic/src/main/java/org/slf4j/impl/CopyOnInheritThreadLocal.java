package org.slf4j.impl;

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
    HashMap<String, String> hm = new HashMap<String, String>(parentValue);
    return hm;
  }

}
