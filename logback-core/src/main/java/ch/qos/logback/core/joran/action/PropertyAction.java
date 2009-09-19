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
package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.util.Properties;

/**
 * This class serves as a base for other actions, which similar to the ANT
 * <property> task which add/set properties of a given object.
 * 
 * This action sets new substitution properties in the logging context by name,
 * value pair, or adds all the properties passed in "file" or "resource"
 * attribute.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class PropertyAction extends Action {

  static final String RESOURCE_ATTRIBUTE = "resource";

  static String INVALID_ATTRIBUTES = "In <property> element, either the \"file\" attribute alone, or "
      + "the \"resource\" element alone, or both the \"name\" and \"value\" attributes must be set.";

  /**
   * Add all the properties found in the argument named 'props' to an
   * InterpretationContext.
   */
  public void setProperties(InterpretationContext ec, Properties props) {
    ec.addSubstitutionProperties(props);
  }

  public void setProperty(InterpretationContext ec, String key, String value) {
    ec.addSubstitutionProperty(key, value);
  }

  /**
   * Set a new property for the execution context by name, value pair, or adds
   * all the properties found in the given file.
   * 
   */
  public void begin(InterpretationContext ec, String localName,
      Attributes attributes) {

    if ("substitutionProperty".equals(localName)) {
      addWarn("[substitutionProperty] element has been deprecated. Plase use the [property] element instead.");
    }

    String name = attributes.getValue(NAME_ATTRIBUTE);
    String value = attributes.getValue(VALUE_ATTRIBUTE);

    if (checkFileAttributeSanity(attributes)) {
      String file = attributes.getValue(FILE_ATTRIBUTE);
      file = ec.subst(file);
      try {
        FileInputStream istream = new FileInputStream(file);
        loadAndSetProperties(ec, istream);
      } catch (IOException e) {
        addError("Could not read properties file [" + file + "].", e);
      }
    } else if (checkResourceAttributeSanity(attributes)) {
      String resource = attributes.getValue(RESOURCE_ATTRIBUTE);
      resource = ec.subst(resource);
      URL resourceURL = Loader.getResourceBySelfClassLoader(resource);
      if (resourceURL == null) {
        addError("Could not find resource [" + resource + "].");
      } else {
        try {
          InputStream istream = resourceURL.openStream();
          loadAndSetProperties(ec, istream);
        } catch (IOException e) {
          addError("Could not read resource file [" + resource + "].", e);
        }
      }
    } else if (checkValueNameAttributesSanity(attributes)) {
      value = RegularEscapeUtil.basicEscape(value);
      // now remove both leading and trailing spaces
      value = value.trim();
      value = ec.subst(value);
      setProperty(ec, name, value);

    } else {
      addError(INVALID_ATTRIBUTES);
    }
  }

  void loadAndSetProperties(InterpretationContext ec, InputStream istream)
      throws IOException {
    Properties props = new Properties();
    props.load(istream);
    istream.close();
    setProperties(ec, props);
  }

  boolean checkFileAttributeSanity(Attributes attributes) {
    String file = attributes.getValue(FILE_ATTRIBUTE);
    String name = attributes.getValue(NAME_ATTRIBUTE);
    String value = attributes.getValue(VALUE_ATTRIBUTE);
    String resource = attributes.getValue(RESOURCE_ATTRIBUTE);

    return !(OptionHelper.isEmpty(file))
        && (OptionHelper.isEmpty(name) && OptionHelper.isEmpty(value) && OptionHelper
            .isEmpty(resource));
  }

  boolean checkResourceAttributeSanity(Attributes attributes) {
    String file = attributes.getValue(FILE_ATTRIBUTE);
    String name = attributes.getValue(NAME_ATTRIBUTE);
    String value = attributes.getValue(VALUE_ATTRIBUTE);
    String resource = attributes.getValue(RESOURCE_ATTRIBUTE);

    return !(OptionHelper.isEmpty(resource))
        && (OptionHelper.isEmpty(name) && OptionHelper.isEmpty(value) && OptionHelper
            .isEmpty(file));
  }

  boolean checkValueNameAttributesSanity(Attributes attributes) {
    String file = attributes.getValue(FILE_ATTRIBUTE);
    String name = attributes.getValue(NAME_ATTRIBUTE);
    String value = attributes.getValue(VALUE_ATTRIBUTE);
    String resource = attributes.getValue(RESOURCE_ATTRIBUTE);

    return (!(OptionHelper.isEmpty(name) || OptionHelper.isEmpty(value)) && (OptionHelper
        .isEmpty(file) && OptionHelper.isEmpty(resource)));
  }

  public void end(InterpretationContext ec, String name) {
  }

  public void finish(InterpretationContext ec) {
  }
}
