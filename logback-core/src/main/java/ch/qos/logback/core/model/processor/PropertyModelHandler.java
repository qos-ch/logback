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
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

public class PropertyModelHandler extends ModelHandlerBase<PropertyModel> {

    static String INVALID_ATTRIBUTES = "In <property> element, either the \"file\" attribute alone, or "
                    + "the \"resource\" element alone, or both the \"name\" and \"value\" attributes must be set.";

    PropertyModelHandler(Context context, InterpretationContext interpretationContext) {
        super(context, interpretationContext);
    }

    public void handle(PropertyModel propertyMopel) {

        Scope scope = ActionUtil.stringToScope(propertyMopel.getScopeStr());

        if (checkFileAttributeSanity(propertyMopel)) {
            String file = propertyMopel.getFile();
            file = interpretationContext.subst(file);
            try {
                FileInputStream istream = new FileInputStream(file);
                loadAndSetProperties(interpretationContext, istream, scope);
            } catch (FileNotFoundException e) {
                addError("Could not find properties file [" + file + "].");
            } catch (IOException e1) {
                addError("Could not read properties file [" + file + "].", e1);
            }
        } else if (checkResourceAttributeSanity(propertyMopel)) {
            String resource = propertyMopel.getResource();
            resource = interpretationContext.subst(resource);
            URL resourceURL = Loader.getResourceBySelfClassLoader(resource);
            if (resourceURL == null) {
                addError("Could not find resource [" + resource + "].");
            } else {
                try {
                    InputStream istream = resourceURL.openStream();
                    loadAndSetProperties(interpretationContext, istream, scope);
                } catch (IOException e) {
                    addError("Could not read resource file [" + resource + "].", e);
                }
            }
        } else if (checkValueNameAttributesSanity(propertyMopel)) {
            String value = RegularEscapeUtil.basicEscape(propertyMopel.getValue());
            // now remove both leading and trailing spaces
            value = value.trim();
            value = interpretationContext.subst(value);
            ActionUtil.setProperty(interpretationContext, propertyMopel.getName(), value, scope);

        } else {
            addError(INVALID_ATTRIBUTES);
        }
    }

    void loadAndSetProperties(InterpretationContext interpretationContext, InputStream istream, Scope scope) throws IOException {
        Properties props = new Properties();
        props.load(istream);
        istream.close();
        ActionUtil.setProperties(interpretationContext, props, scope);
    }

    boolean checkFileAttributeSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();

        return !(OptionHelper.isEmpty(file)) && (OptionHelper.isEmpty(name) && OptionHelper.isEmpty(value) && OptionHelper.isEmpty(resource));
    }

    boolean checkResourceAttributeSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();

        return !(OptionHelper.isEmpty(resource)) && (OptionHelper.isEmpty(name) && OptionHelper.isEmpty(value) && OptionHelper.isEmpty(file));
    }

    boolean checkValueNameAttributesSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();
        return (!(OptionHelper.isEmpty(name) || OptionHelper.isEmpty(value)) && (OptionHelper.isEmpty(file) && OptionHelper.isEmpty(resource)));
    }

}
