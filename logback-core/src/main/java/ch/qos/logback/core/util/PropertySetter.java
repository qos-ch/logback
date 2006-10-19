/*
 * Copyright 1999,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Contributors:  Georg Lundesgaard
package ch.qos.logback.core.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;

import java.lang.reflect.*;

import java.util.*;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * General purpose Object property setter. Clients repeatedly invokes
 * {@link #setProperty setProperty(name,value)} in order to invoke setters on
 * the Object specified in the constructor. This class relies on the JavaBeans
 * {@link Introspector} to analyze the given Object Class using reflection.
 * 
 * <p>
 * Usage:
 * 
 * <pre>
 * PropertySetter ps = new PropertySetter(anObject);
 * ps.set(&quot;name&quot;, &quot;Joe&quot;);
 * ps.set(&quot;age&quot;, &quot;32&quot;);
 * ps.set(&quot;isMale&quot;, &quot;true&quot;);
 * </pre>
 * 
 * will cause the invocations anObject.setName("Joe"), anObject.setAge(32), and
 * setMale(true) if such methods exist with those signatures. Otherwise an
 * {@link IntrospectionException} are thrown.
 * 
 * @author Anders Kristensen
 * @author Ceki Gulcu
 */
public class PropertySetter extends ContextAwareBase {
  public static final int NOT_FOUND = 0;
  public static final int AS_COMPONENT = 1;
  public static final int AS_PROPERTY = 2;
  public static final int AS_COLLECTION = 3;

  protected Object obj;
  protected Class objClass;
  protected PropertyDescriptor[] propertyDescriptors;
  protected MethodDescriptor[] methodDescriptors;

  /**
   * Create a new PropertySetter for the specified Object. This is done in
   * preparation for invoking {@link #setProperty} one or more times.
   * 
   * @param obj
   *          the object for which to set properties
   */
  public PropertySetter(Object obj) {
    this.obj = obj;
    this.objClass = obj.getClass();
  }

  /**
   * Uses JavaBeans {@link Introspector} to computer setters of object to be
   * configured.
   */
  protected void introspect() {
    try {
      BeanInfo bi = Introspector.getBeanInfo(obj.getClass());
      propertyDescriptors = bi.getPropertyDescriptors();
      methodDescriptors = bi.getMethodDescriptors();
    } catch (IntrospectionException ex) {
      addError("Failed to introspect " + obj + ": " + ex.getMessage());
      propertyDescriptors = new PropertyDescriptor[0];
      methodDescriptors = new MethodDescriptor[0];
    }
  }

  /**
   * Set the properties for the object that match the <code>prefix</code>
   * passed as parameter.
   */
  public void setProperties(Properties properties, String prefix) {
    int len = prefix.length();

    for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();

      // handle only properties that start with the desired frefix.
      if (key.startsWith(prefix)) {
        // ignore key if it contains dots after the prefix
        if (key.indexOf('.', len + 1) > 0) {
          // System.err.println("----------Ignoring---["+key
          // +"], prefix=["+prefix+"].");
          continue;
        }

        String value = OptionHelper.findAndSubst(key, properties);

        key = key.substring(len);

        if ("layout".equals(key) && obj instanceof Appender) {
          continue;
        }

        setProperty(key, value);
      }
    }
  }

  /**
   * Set a property on this PropertySetter's Object. If successful, this method
   * will invoke a setter method on the underlying Object. The setter is the one
   * for the specified property name and the value is determined partly from the
   * setter argument type and partly from the value specified in the call to
   * this method.
   * 
   * <p>
   * If the setter expects a String no conversion is necessary. If it expects an
   * int, then an attempt is made to convert 'value' to an int using new
   * Integer(value). If the setter expects a boolean, the conversion is by new
   * Boolean(value).
   * 
   * @param name
   *          name of the property
   * @param value
   *          String value of the property
   */
  public void setProperty(String name, String value) {
    if (value == null) {
      return;
    }

    name = Introspector.decapitalize(name);

    PropertyDescriptor prop = getPropertyDescriptor(name);

    // LogLog.debug("---------Key: "+name+", type="+prop.getPropertyType());
    if (prop == null) {
      addWarn("No such property [" + name + "] in " + objClass.getName() + ".");
    } else {
      try {
        setProperty(prop, name, value);
      } catch (PropertySetterException ex) {
        addWarn("Failed to set property [" + name + "] to value \"" + value
            + "\". ", ex);
      }
    }
  }

  /**
   * Set the named property given a {@link PropertyDescriptor}.
   * 
   * @param prop
   *          A PropertyDescriptor describing the characteristics of the
   *          property to set.
   * @param name
   *          The named of the property to set.
   * @param value
   *          The value of the property.
   */
  public void setProperty(PropertyDescriptor prop, String name, String value)
      throws PropertySetterException {
    Method setter = prop.getWriteMethod();

    if (setter == null) {
      throw new PropertySetterException("No setter for property [" + name
          + "].");
    }

    Class[] paramTypes = setter.getParameterTypes();

    if (paramTypes.length != 1) {
      throw new PropertySetterException("#params for setter != 1");
    }

    Object arg;

    try {
      arg = convertArg(value, paramTypes[0]);
    } catch (Throwable t) {
      throw new PropertySetterException("Conversion to type [" + paramTypes[0]
          + "] failed. ", t);
    }

    if (arg == null) {
      throw new PropertySetterException("Conversion to type [" + paramTypes[0]
          + "] failed.");
    }

    // getLogger().debug("Setting property [{}] to [{}].", name, arg);

    try {
      setter.invoke(obj, new Object[] { arg });
    } catch (Exception ex) {
      throw new PropertySetterException(ex);
    }
  }

  public int canContainComponent(String name) {
    String cName = capitalizeFirstLetter(name);

    Method method = getMethod("add" + cName);

    if (method != null) {
      // getLogger().debug(
      // "Found add {} method in class {}", cName, objClass.getName());

      return AS_COLLECTION;
    }

    String dName = Introspector.decapitalize(name);

    PropertyDescriptor propertyDescriptor = getPropertyDescriptor(dName);

    if (propertyDescriptor != null) {
      Method setterMethod = propertyDescriptor.getWriteMethod();

      if (setterMethod != null) {
        // getLogger().debug(
        // "Found setter method for property [{}] in class {}", name,
        // objClass.getName());
        Class[] classArray = setterMethod.getParameterTypes();
        if (classArray.length != 1) {
          return NOT_FOUND;
        } else {
          Class clazz = classArray[0];
          Package p = clazz.getPackage();
          if (clazz.isPrimitive()) {
            return AS_PROPERTY;
          } else if ("java.lang".equals(p.getName())) {
            return AS_PROPERTY;
          } else {
            return AS_COMPONENT;
          }
        }
      }
    }

    // we have failed
    return NOT_FOUND;
  }

  public Class getObjClass() {
    return objClass;
  }

  @SuppressWarnings("unchecked")
  public void addComponent(String name, Object childComponent) {
    Class ccc = childComponent.getClass();
    name = capitalizeFirstLetter(name);

    Method method = getMethod("add" + name);

    // first let us use the addXXX method
    if (method != null) {
      Class[] params = method.getParameterTypes();

      if (params.length == 1) {
        if (params[0].isAssignableFrom(childComponent.getClass())) {
          try {
            method.invoke(this.obj, new Object[] { childComponent });
          } catch (Exception e) {
            addError("Could not invoke method " + method.getName()
                + " in class " + obj.getClass().getName()
                + " with parameter of type " + ccc.getName(), e);
          }
        } else {
          addError("A \"" + ccc.getName()
              + "\" object is not assignable to a \"" + params[0].getName()
              + "\" variable.");
          addError("The class \"" + params[0].getName() + "\" was loaded by ");
          addError("[" + params[0].getClassLoader()
              + "] whereas object of type ");
          addError("\"" + ccc.getName() + "\" was loaded by ["
              + ccc.getClassLoader() + "].");
        }
      }
    } else {
      addError("Could not find method [" + "add" + name + "] in class ["
          + objClass.getName() + "].");
    }
  }

  public void setComponent(String name, Object childComponent) {
    PropertyDescriptor propertyDescriptor = getPropertyDescriptor(name);

    if (propertyDescriptor == null) {
      addWarn("Could not find PropertyDescriptor for [" + name + "] in "
          + objClass.getName());

      return;
    }

    Method setter = propertyDescriptor.getWriteMethod();

    if (setter == null) {
      addWarn("Not setter method for property [" + name + "] in "
          + obj.getClass().getName());

      return;
    }

    Class[] paramTypes = setter.getParameterTypes();

    if (paramTypes.length != 1) {
      addError("Wrong number of parameters in setter method for property ["
          + name + "] in " + obj.getClass().getName());

      return;
    }

    try {
      setter.invoke(obj, new Object[] { childComponent });
      // getLogger().debug(
      // "Set child component of type [{}] for [{}].", objClass.getName(),
      // childComponent.getClass().getName());
    } catch (Exception e) {
      addError("Could not set component " + obj + " for parent component "
          + obj, e);
    }
  }

  String capitalizeFirstLetter(String name) {
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

  /**
   * Convert <code>val</code> a String parameter to an object of a given type.
   */
  protected Object convertArg(String val, Class type) {
    if (val == null) {
      return null;
    }

    String v = val.trim();

    if (String.class.isAssignableFrom(type)) {
      return val;
    } else if (Integer.TYPE.isAssignableFrom(type)) {
      return new Integer(v);
    } else if (Long.TYPE.isAssignableFrom(type)) {
      return new Long(v);
    } else if (Float.TYPE.isAssignableFrom(type)) {
      return new Float(v);
    } else if (Double.TYPE.isAssignableFrom(type)) {
      return new Double(v);
    } else if (Boolean.TYPE.isAssignableFrom(type)) {
      if ("true".equalsIgnoreCase(v)) {
        return Boolean.TRUE;
      } else if ("false".equalsIgnoreCase(v)) {
        return Boolean.FALSE;
      }
    }

    return null;
  }

  protected Method getMethod(String methodName) {
    if (methodDescriptors == null) {
      introspect();
    }

    for (int i = 0; i < methodDescriptors.length; i++) {
      if (methodName.equals(methodDescriptors[i].getName())) {
        return methodDescriptors[i].getMethod();
      }
    }

    return null;
  }

  protected PropertyDescriptor getPropertyDescriptor(String name) {
    if (propertyDescriptors == null) {
      introspect();
    }

    for (int i = 0; i < propertyDescriptors.length; i++) {
      if (name.equals(propertyDescriptors[i].getName())) {
        return propertyDescriptors[i];
      }
    }

    return null;
  }
}
