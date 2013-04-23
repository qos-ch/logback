/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

package ch.qos.logback.core;

import java.util.HashSet;
import java.util.Set;

import ch.qos.logback.core.spi.LifeCycle;

/**
 * An object that manages a collection of components that implement the
 * {@link LifeCycle} interface.  Each component that is added to the manager
 * is started if necessary, and is stopped when the manager is reset.
 *
 * @author Carl Harris
 */
public class LifeCycleManager {

  private final Set<LifeCycle> components = new HashSet<LifeCycle>();
  
  /**
   * Adds a component to this manager.  
   * <p>
   * The component is started if necessary. 
   * @param component the component whose life cycle is to be managed
   */
  public void addComponent(LifeCycle component) {
    if (!component.isStarted()) {
      component.start();
    }
    components.add(component);
  }
  
  /**
   * Resets this manager.
   * <p>
   * All registered components are stopped and removed from the manager.
   */
  public void reset() {
    for (LifeCycle component : components) {
      if (component.isStarted()) {
        component.stop();
      }
    }
    components.clear();
  }
 
}
