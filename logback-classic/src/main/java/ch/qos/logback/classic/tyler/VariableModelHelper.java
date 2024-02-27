/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.tyler;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.model.ModelConstants;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.util.PropertyModelHandlerHelper;
import ch.qos.logback.core.model.util.VariableSubstitutionsHelper;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class VariableModelHelper extends ContextAwareBase  {

    TylerConfiguratorBase tylerConfiguratorBase;
    VariableSubstitutionsHelper variableSubstitutionsHelper;

    VariableModelHelper(Context context, TylerConfiguratorBase tylerConfiguratorBase) {
        super( tylerConfiguratorBase);
        this.context = context;
        this.tylerConfiguratorBase = tylerConfiguratorBase;
        this.variableSubstitutionsHelper = new VariableSubstitutionsHelper(context);
    }

    void updateProperties(PropertyModel propertyModel) {

        ActionUtil.Scope scope = ActionUtil.stringToScope(propertyModel.getScopeStr());
        if (PropertyModelHandlerHelper.checkFileAttributeSanity(propertyModel)) {
            String file = propertyModel.getFile();
            file = tylerConfiguratorBase.subst(file);
            try (FileInputStream istream = new FileInputStream(file)) {
                loadAndSetProperties(istream, scope);
            } catch (FileNotFoundException e) {
                addError("Could not find properties file [" + file + "].");
            } catch (IOException |IllegalArgumentException e1) { // IllegalArgumentException is thrown in case the file
                // is badly malformed, i.e a binary.
                addError("Could not read properties file [" + file + "].", e1);
            }
        } else if (PropertyModelHandlerHelper.checkResourceAttributeSanity(propertyModel)) {
            String resource = propertyModel.getResource();
            resource = tylerConfiguratorBase.subst(resource);
            URL resourceURL = Loader.getResourceBySelfClassLoader(resource);
            if (resourceURL == null) {
                addError("Could not find resource [" + resource + "].");
            } else {
                try ( InputStream istream = resourceURL.openStream();) {
                    loadAndSetProperties(istream, scope);
                } catch (IOException e) {
                    addError("Could not read resource file [" + resource + "].", e);
                }
            }
        } else if (PropertyModelHandlerHelper.checkValueNameAttributesSanity(propertyModel)) {
            // earlier versions performed Java '\' escapes for '\\' '\t' etc. Howevver, there is no
            // need to do this. See RegularEscapeUtil.__UNUSED__basicEscape
            String value = propertyModel.getValue();

            // now remove both leading and trailing spaces
            value = value.trim();
            value = tylerConfiguratorBase.subst(value);
            setProperty(propertyModel.getName(), value, scope);

        } else {
            addError(ModelConstants.INVALID_ATTRIBUTES);
        }
    }

    void loadAndSetProperties(InputStream istream, ActionUtil.Scope scope) throws IOException {
        Properties props = new Properties();
        props.load(istream);
        setProperties(props, scope);
    }


    public void setProperties(Properties props, ActionUtil.Scope scope) {
        switch (scope) {
        case LOCAL:
            variableSubstitutionsHelper.addSubstitutionProperties(props);
            break;
        case CONTEXT:
            ContextUtil cu = new ContextUtil(getContext());
            cu.addProperties(props);
            break;
        case SYSTEM:
            OptionHelper.setSystemProperties(this, props);
        }
    }

    public void setProperty(String key, String value, ActionUtil.Scope scope) {
        switch (scope) {
        case LOCAL:
            variableSubstitutionsHelper.addSubstitutionProperty(key, value);
            break;
        case CONTEXT:
            getContext().putProperty(key, value);
            break;
        case SYSTEM:
            OptionHelper.setSystemProperty(this, key, value);
        }
    }
}
