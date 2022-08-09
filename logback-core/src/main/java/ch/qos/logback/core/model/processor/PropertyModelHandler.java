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
import ch.qos.logback.core.model.ModelUtil;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

public class PropertyModelHandler extends ModelHandlerBase {

    public static final String INVALID_ATTRIBUTES = "In <property> element, either the \"file\" attribute alone, or "
            + "the \"resource\" element alone, or both the \"name\" and \"value\" attributes must be set.";

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

        if (checkFileAttributeSanity(propertyModel)) {
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
        } else if (checkResourceAttributeSanity(propertyModel)) {
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
        } else if (checkValueNameAttributesSanity(propertyModel)) {
            // earlier versions performed Java '\' escapes for '\\' '\t' etc. Howevver, there is no
            // need to do this. See RegularEscapeUtil.__UNUSED__basicEscape
            String value = propertyModel.getValue();

            // now remove both leading and trailing spaces
            value = value.trim();
            value = interpretationContext.subst(value);
            ActionUtil.setProperty(interpretationContext, propertyModel.getName(), value, scope);

        } else {
            addError(INVALID_ATTRIBUTES);
        }
    }

    void loadAndSetProperties(ModelInterpretationContext mic, InputStream istream, Scope scope) throws IOException {
        Properties props = new Properties();
        props.load(istream);
        ModelUtil.setProperties(mic, props, scope);
    }

    boolean checkFileAttributeSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();

        return !(OptionHelper.isNullOrEmpty(file)) && (OptionHelper.isNullOrEmpty(name)
                && OptionHelper.isNullOrEmpty(value) && OptionHelper.isNullOrEmpty(resource));
    }

    boolean checkResourceAttributeSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();

        return !(OptionHelper.isNullOrEmpty(resource)) && (OptionHelper.isNullOrEmpty(name)
                && OptionHelper.isNullOrEmpty(value) && OptionHelper.isNullOrEmpty(file));
    }

    boolean checkValueNameAttributesSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();
        return (!(OptionHelper.isNullOrEmpty(name) || OptionHelper.isNullOrEmpty(value))
                && (OptionHelper.isNullOrEmpty(file) && OptionHelper.isNullOrEmpty(resource)));
    }

}
