package ch.qos.logback.core.model.processor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ModelConstants;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.util.PropertyModelUtil;
import ch.qos.logback.core.util.Loader;

public class PropertyModelHandler extends ModelHandlerBase {

    public PropertyModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new PropertyModelHandler(context);
    }

    @Override
    protected Class<PropertyModel> getSupportedModelClass() {
        return PropertyModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext interpretationContext, Model model) {

        PropertyModel propertyModel = (PropertyModel) model;

        Scope scope = ActionUtil.stringToScope(propertyModel.getScopeStr());

        if (PropertyModelUtil.checkFileAttributeSanity(propertyModel)) {
            String file = propertyModel.getFile();
            file = interpretationContext.subst(file);
            try (FileInputStream istream = new FileInputStream(file)) {
                loadAndSetProperties(interpretationContext, istream, scope);
            } catch (FileNotFoundException e) {
                addError("Could not find properties file [" + file + "].");
            } catch (IOException|IllegalArgumentException e1) { // IllegalArgumentException is thrown in case the file
                                                                // is badly malformed, i.e a binary.
                addError("Could not read properties file [" + file + "].", e1);
            }
        } else if (PropertyModelUtil.checkResourceAttributeSanity(propertyModel)) {
            String resource = propertyModel.getResource();
            resource = interpretationContext.subst(resource);
            URL resourceURL = Loader.getResourceBySelfClassLoader(resource);
            if (resourceURL == null) {
                addError("Could not find resource [" + resource + "].");
            } else {
                try ( InputStream istream = resourceURL.openStream();) {
                    loadAndSetProperties(interpretationContext, istream, scope);
                } catch (IOException e) {
                    addError("Could not read resource file [" + resource + "].", e);
                }
            }
        } else if (PropertyModelUtil.checkValueNameAttributesSanity(propertyModel)) {
            // earlier versions performed Java '\' escapes for '\\' '\t' etc. Howevver, there is no
            // need to do this. See RegularEscapeUtil.__UNUSED__basicEscape
            String value = propertyModel.getValue();

            // now remove both leading and trailing spaces
            value = value.trim();
            value = interpretationContext.subst(value);
            ActionUtil.setProperty(interpretationContext, propertyModel.getName(), value, scope);

        } else {
            addError(ModelConstants.INVALID_ATTRIBUTES);
        }
    }

    void loadAndSetProperties(ModelInterpretationContext mic, InputStream istream, Scope scope) throws IOException {
        Properties props = new Properties();
        props.load(istream);
        PropertyModelUtil.setProperties(mic, props, scope);
    }

}
