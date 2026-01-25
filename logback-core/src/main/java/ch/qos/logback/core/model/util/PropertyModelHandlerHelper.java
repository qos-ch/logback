/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.model.util;

import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.model.ModelConstants;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Given a {@link PropertyModel} offers methods to inject properties into a {@link PropertyContainer}.
 *
 * @since 1.5.1
 */
public class PropertyModelHandlerHelper extends ContextAwareBase {

    public static final String HANDLE_PROPERTY_MODEL_METHOD_NAME = "handlePropertyModel";

    public PropertyModelHandlerHelper(ContextAware declaredOrigin) {
        super(declaredOrigin);
    }

    /**
     * Given a {@link PropertyModel} inject relevant properties into the given {@link ContextAwarePropertyContainer}
     * parameter.
     *
     * @param capcm
     * @param nameStr
     * @param valueStr
     * @param fileStr
     * @param resourceStr
     * @param scopeStr
     *
     */
    public void handlePropertyModel(ContextAwarePropertyContainer capcm, String nameStr, String valueStr,
            String fileStr, String resourceStr, String scopeStr) {
        PropertyModel propertyModel = new PropertyModel();
        propertyModel.setName(nameStr);
        propertyModel.setValue(valueStr);
        propertyModel.setFile(fileStr);
        propertyModel.setResource(resourceStr);

        propertyModel.setScopeStr(scopeStr);

        handlePropertyModel(capcm, propertyModel);
    }

    /**
     * Given a {@link PropertyModel} inject relevant properties into the given {@link ContextAwarePropertyContainer}
     * parameter.
     *
     * @param capc
     * @param propertyModel
     */
    public void handlePropertyModel(ContextAwarePropertyContainer capc, PropertyModel propertyModel) {
        ActionUtil.Scope scope = ActionUtil.stringToScope(propertyModel.getScopeStr());

        if (checkFileAttributeSanity(propertyModel)) {
            String file = propertyModel.getFile();
            file = capc.subst(file);
            try (FileInputStream istream = new FileInputStream(file)) {
                PropertyModelHandlerHelper.loadAndSetProperties(capc, istream, scope);
            } catch (FileNotFoundException e) {
                addError("Could not find properties file [" + file + "].");
            } catch (IOException | IllegalArgumentException e1) { // IllegalArgumentException is thrown in case the file
                // is badly malformed, i.e a binary.
                addError("Could not read properties file [" + file + "].", e1);
            }
        } else if (checkResourceAttributeSanity(propertyModel)) {
            String resource = propertyModel.getResource();
            resource = capc.subst(resource);
            URL resourceURL = Loader.getResourceBySelfClassLoader(resource);
            if (resourceURL == null) {
                addError("Could not find resource [" + resource + "].");
            } else {
                try (InputStream istream = resourceURL.openStream();) {
                    PropertyModelHandlerHelper.loadAndSetProperties(capc, istream, scope);
                } catch (IOException e) {
                    addError("Could not read resource file [" + resource + "].", e);
                }
            }
        } else if (checkValueNameAttributesSanity(propertyModel)) {
            // earlier versions performed Java '\' escapes for '\\' '\t' etc. Howevver, there is no
            // need to do this. See RegularEscapeUtil.__UNUSED__basicEscape
            String value = propertyModel.getValue();

            // now remove both leading and trailing spaces
            value = value.trim();
            value = capc.subst(value);
            ActionUtil.setProperty(capc, propertyModel.getName(), value, scope);

        } else {
            addError(ModelConstants.INVALID_ATTRIBUTES);
        }
    }

    public static boolean checkFileAttributeSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();

        return !(OptionHelper.isNullOrEmptyOrAllSpaces(file)) && (OptionHelper.isNullOrEmptyOrAllSpaces(name)
                && OptionHelper.isNullOrEmptyOrAllSpaces(value) && OptionHelper.isNullOrEmptyOrAllSpaces(resource));
    }

    public static boolean checkResourceAttributeSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();

        return !(OptionHelper.isNullOrEmptyOrAllSpaces(resource)) && (OptionHelper.isNullOrEmptyOrAllSpaces(name)
                && OptionHelper.isNullOrEmptyOrAllSpaces(value) && OptionHelper.isNullOrEmptyOrAllSpaces(file));
    }

    public static boolean checkValueNameAttributesSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();
        return (!(OptionHelper.isNullOrEmptyOrAllSpaces(name) || OptionHelper.isNullOrEmptyOrAllSpaces(value)) && (
                OptionHelper.isNullOrEmptyOrAllSpaces(file) && OptionHelper.isNullOrEmptyOrAllSpaces(resource)));
    }

    /**
     * Add all the properties found in the argument named 'props' to an InterpretationContext.
     */
    static public void setProperty(ContextAwarePropertyContainer capc, String key, String value,
            ActionUtil.Scope scope) {
        switch (scope) {
        case LOCAL:
            capc.addSubstitutionProperty(key, value);
            break;
        case CONTEXT:
            capc.getContext().putProperty(key, value);
            break;
        case SYSTEM:
            OptionHelper.setSystemProperty(capc, key, value);
        }
    }

    /**
     * Add all the properties found in the argument named 'props' to an InterpretationContext.
     */
    static public void setProperties(ContextAwarePropertyContainer capc, Properties props, ActionUtil.Scope scope) {
        switch (scope) {
        case LOCAL:
            capc.addSubstitutionProperties(props);
            break;
        case CONTEXT:
            ContextUtil cu = new ContextUtil(capc.getContext());
            cu.addProperties(props);
            break;
        case SYSTEM:
            OptionHelper.setSystemProperties(capc, props);
        }
    }

    static public void loadAndSetProperties(ContextAwarePropertyContainer capc, InputStream istream,
            ActionUtil.Scope scope) throws IOException {
        Properties props = new Properties();
        props.load(istream);
        PropertyModelHandlerHelper.setProperties(capc, props, scope);
    }
}
