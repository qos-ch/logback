/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.gaffer

import ch.qos.logback.core.Appender
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.spi.LifeCycle
import ch.qos.logback.core.spi.ContextAware

/**
 * @author Ceki G&uuml;c&uuml;
 */

@Mixin(ContextAwareBase)
class ComponentDelegate {

  final Object component;

  ComponentDelegate(Object component) {
    this.component = component;
  }

  String getLabel() { "component" }

  String getLabelFistLetterInUpperCase() { getLabel()[0].toUpperCase() + getLabel().substring(1) }

  void methodMissing(String name, def args) {
    
    if (component.hasProperty(name)) {
      String subComponentName
      Class clazz
      Closure closure
      (subComponentName, clazz, closure) = analyzeArgs(args)
      if (clazz != null) {
        Object subComponent = clazz.newInstance()
        if (subComponentName && subComponent.hasProperty(name)) {
          subComponent.name = subComponentName;
        }
        if (subComponent instanceof ContextAware) {
          subComponent.context = context;
        }
        if (closure) {
          ComponentDelegate subDelegate = new ComponentDelegate(subComponent)
          subDelegate.context = context
          closure.delegate = subDelegate
          closure.resolveStrategy = Closure.DELEGATE_FIRST
          closure()
        }
        if (subComponent instanceof LifeCycle) {
          subComponent.start();
        }
        component."${name}" = subComponent;
      }
    } else {
      addError("${getLabelFistLetterInUpperCase()} ${getComponentName()} of type [${component.getClass().canonicalName}] has no [${name}] property ")
    }
  }

  def analyzeArgs(Object[] args) {
    String name;
    Class clazz;
    Closure closure;

    if (args.size() > 3) {
      addError("At most 3 arguments allowed but you passed $args")
      return [name, clazz, closure]
    }

    if (args[-1] instanceof Closure) {
      closure = args[-1]
      args -= args[-1]
    }

    if (args.size() == 1) {
      clazz = parseClassArgument(args[0])
    }

    if (args.size() == 2) {
      name = parseNameArgument(args[0])
      clazz = parseClassArgument(args[1])
    }

    return [name, clazz, closure]
  }

  Class parseClassArgument(arg) {
    if (arg instanceof Class) {
      return arg
    } else if (arg instanceof String) {
      return Class.forName(arg)
    } else {
      addError("Unexpected argument type ${arg.getClass().canonicalName}")
      return null;
    }
  }

  String parseNameArgument(arg) {
    if (arg instanceof String) {
      return arg
    } else {
      addError("With 2 or 3 arguments, the first argument must be the component name, i.e of type string")
      return null;
    }
  }

  String getComponentName() {
    if (component.hasProperty("name"))
      return "[${component.name}]"
    else
      return ""

  }

  void propertyMissing(String name, def value) {
    name = camelCasify(name);
    if (component.hasProperty(name)) {
      //println "-- component has property $name"
      component."${name}" = value;
    } else {
      // println "-- component does not have property [$name]"
      addError("${getLabelFistLetterInUpperCase()} ${getComponentName()} of type [${component.getClass().canonicalName}] has no [${name}] property ")
    }
  }

  String camelCasify(String name) {
    if(name == null || name.length() == 0)
      return name;

    String firstLetter = new String(name.getAt(0)).toLowerCase();
    if(name.length() == 1)
      return firstLetter
    else
      return firstLetter + name.substring(1);
  }
}