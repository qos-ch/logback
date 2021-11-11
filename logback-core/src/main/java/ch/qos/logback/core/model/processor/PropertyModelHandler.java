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
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

public class PropertyModelHandler extends ModelHandlerBase {

    public static final String INVALID_ATTRIBUTES = "In <property> element, either the \"file\" attribute alone, or "
                    + "the \"resource\" element alone, or both the \"name\" and \"value\" attributes must be set.";

    public PropertyModelHandler(final Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
        return new PropertyModelHandler(context);
    }

    @Override
    protected Class<PropertyModel> getSupportedModelClass() {
        return PropertyModel.class;
    }

    @Override
    public void handle(final InterpretationContext interpretationContext, final Model model) {

        final PropertyModel propertyModel = (PropertyModel) model;

        final Scope scope = ActionUtil.stringToScope(propertyModel.getScopeStr());

        if (checkFileAttributeSanity(propertyModel)) {
            String file = propertyModel.getFile();
            file = interpretationContext.subst(file);
            try {
                final FileInputStream istream = new FileInputStream(file);
                loadAndSetProperties(interpretationContext, istream, scope);
            } catch (final FileNotFoundException e) {
                addError("Could not find properties file [" + file + "].");
            } catch (final IOException e1) {
                addError("Could not read properties file [" + file + "].", e1);
            }
        } else if (checkResourceAttributeSanity(propertyModel)) {
            String resource = propertyModel.getResource();
            resource = interpretationContext.subst(resource);
            final URL resourceURL = Loader.getResourceBySelfClassLoader(resource);
            if (resourceURL == null) {
                addError("Could not find resource [" + resource + "].");
            } else {
                try {
                    final InputStream istream = resourceURL.openStream();
                    loadAndSetProperties(interpretationContext, istream, scope);
                } catch (final IOException e) {
                    addError("Could not read resource file [" + resource + "].", e);
                }
            }
        } else if (checkValueNameAttributesSanity(propertyModel)) {
            String value = RegularEscapeUtil.basicEscape(propertyModel.getValue());
            // now remove both leading and trailing spaces
            value = value.trim();
            value = interpretationContext.subst(value);
            ActionUtil.setProperty(interpretationContext, propertyModel.getName(), value, scope);

        } else {
            addError(INVALID_ATTRIBUTES);
        }
    }

    void loadAndSetProperties(final InterpretationContext interpretationContext, final InputStream istream, final Scope scope) throws IOException {
        final Properties props = new Properties();
        props.load(istream);
        istream.close();
        ActionUtil.setProperties(interpretationContext, props, scope);
    }

    boolean checkFileAttributeSanity(final PropertyModel propertyModel) {
        final String file = propertyModel.getFile();
        final String name = propertyModel.getName();
        final String value = propertyModel.getValue();
        final String resource = propertyModel.getResource();

        return !OptionHelper.isNullOrEmpty(file) && OptionHelper.isNullOrEmpty(name) && OptionHelper.isNullOrEmpty(value)
                        && OptionHelper.isNullOrEmpty(resource);
    }

    boolean checkResourceAttributeSanity(final PropertyModel propertyModel) {
        final String file = propertyModel.getFile();
        final String name = propertyModel.getName();
        final String value = propertyModel.getValue();
        final String resource = propertyModel.getResource();

        return !OptionHelper.isNullOrEmpty(resource) && OptionHelper.isNullOrEmpty(name) && OptionHelper.isNullOrEmpty(value)
                        && OptionHelper.isNullOrEmpty(file);
    }

    boolean checkValueNameAttributesSanity(final PropertyModel propertyModel) {
        final String file = propertyModel.getFile();
        final String name = propertyModel.getName();
        final String value = propertyModel.getValue();
        final String resource = propertyModel.getResource();
        return !OptionHelper.isNullOrEmpty(name) && !OptionHelper.isNullOrEmpty(value) && OptionHelper.isNullOrEmpty(file)
                        && OptionHelper.isNullOrEmpty(resource);
    }

}
