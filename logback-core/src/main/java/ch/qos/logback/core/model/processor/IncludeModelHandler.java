package ch.qos.logback.core.model.processor;

import static ch.qos.logback.core.joran.JoranConstants.INCLUDED_TAG;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Stack;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.IncludeModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

public class IncludeModelHandler extends ModelHandlerBase {

    private boolean optional;
    private String attributeInUse;

    public IncludeModelHandler(final Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
        return new IncludeModelHandler(context);
    }

    @Override
    protected Class<IncludeModel> getSupportedModelClass() {
        return IncludeModel.class;
    }

    @Override
    public void handle(final InterpretationContext intercon, final Model model) throws ModelHandlerException {
        final IncludeModel includeModel = (IncludeModel) model;
        final SaxEventRecorder localRecorder = new SaxEventRecorder(context, includeModel.getElementPath()) {
            @Override
            public boolean shouldIgnoreForElementPath(final String tagName) {
                return JoranConstants.INCLUDED_TAG.equalsIgnoreCase(tagName);
            }
        };

        optional = OptionHelper.toBoolean(includeModel.getOptional(), false);

        if (!checkAttributes(includeModel)) {
            return;
        }

        final InputStream in = getInputStream(intercon, includeModel);
        try {
            if (in != null) {

                parseAndRecord(in, localRecorder);
                // remove the <included> tag from the beginning and </included> from the end
                trimHeadAndTail(localRecorder, INCLUDED_TAG);

                final SaxEventInterpreter localInterpreter = intercon.getSaxEventInterpreter().duplicate(includeModel.getElementPath());
                // add models
                localInterpreter.getEventPlayer().play(localRecorder.saxEventList);
                transferModelStack(includeModel, localInterpreter);
            }
        } catch (final JoranException e) {
            addError("Error while parsing  " + attributeInUse, e);
        } finally {
            close(in);
        }
    }

    private void transferModelStack(final IncludeModel includeModel, final SaxEventInterpreter subInterpreter) {
        final Stack<Model> copy = subInterpreter.getInterpretationContext().getCopyOfModelStack();
        for (final Model m : copy) {
            includeModel.addSubModel(m);
        }
    }

    private void close(final InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (final IOException e) {
            }
        }
    }

    private boolean checkAttributes(final IncludeModel includeModel) {
        int count = 0;

        if (!OptionHelper.isNullOrEmpty(includeModel.getFile())) {
            count++;
        }
        if (!OptionHelper.isNullOrEmpty(includeModel.getUrl())) {
            count++;
        }
        if (!OptionHelper.isNullOrEmpty(includeModel.getResource())) {
            count++;
        }

        if (count == 0) {
            addError("One of \"path\", \"resource\" or \"url\" attributes must be set.");
            return false;
        }
        if (count > 1) {
            addError("Only one of \"file\", \"url\" or \"resource\" attributes should be set.");
            return false;
        }
        if (count == 1) {
            return true;
        }
        throw new IllegalStateException("Count value [" + count + "] is not expected");
    }

    private InputStream getInputStream(final InterpretationContext ec, final IncludeModel includeModel) {
        final URL inputURL = getInputURL(ec, includeModel);
        if (inputURL == null) {
            return null;
        }

        ConfigurationWatchListUtil.addToWatchList(context, inputURL);
        return openURL(inputURL);
    }

    private URL getInputURL(final InterpretationContext ec, final IncludeModel includeModel) {
        final String fileAttribute = includeModel.getFile();
        final String urlAttribute = includeModel.getUrl();
        final String resourceAttribute = includeModel.getResource();

        if (!OptionHelper.isNullOrEmpty(fileAttribute)) {
            attributeInUse = ec.subst(fileAttribute);
            return filePathAsURL(attributeInUse);
        }

        if (!OptionHelper.isNullOrEmpty(urlAttribute)) {
            attributeInUse = ec.subst(urlAttribute);
            return attributeToURL(attributeInUse);
        }

        if (!OptionHelper.isNullOrEmpty(resourceAttribute)) {
            attributeInUse = ec.subst(resourceAttribute);
            return resourceAsURL(attributeInUse);
        }
        // given previous checkAttributes() check we cannot reach this line
        throw new IllegalStateException("A URL stream should have been returned");
    }

    private InputStream openURL(final URL url) {
        try {
            return url.openStream();
        } catch (final IOException e) {
            optionalWarning("Failed to open [" + url.toString() + "]");
            return null;
        }
    }

    private URL attributeToURL(final String urlAttribute) {
        try {
            return new URL(urlAttribute);
        } catch (final MalformedURLException mue) {
            final String errMsg = "URL [" + urlAttribute + "] is not well formed.";
            addError(errMsg, mue);
            return null;
        }
    }

    private URL resourceAsURL(final String resourceAttribute) {
        final URL url = Loader.getResourceBySelfClassLoader(resourceAttribute);
        if (url == null) {
            optionalWarning("Could not find resource corresponding to [" + resourceAttribute + "]");
            return null;
        }
        return url;
    }

    private void optionalWarning(final String msg) {
        if (!optional) {
            addWarn(msg);
        }
    }

    URL filePathAsURL(final String path) {
        final URI uri = new File(path).toURI();
        try {
            return uri.toURL();
        } catch (final MalformedURLException e) {
            // impossible to get here
            e.printStackTrace();
            return null;
        }
    }

    private void parseAndRecord(final InputStream inputSource, final SaxEventRecorder recorder) throws JoranException {
        recorder.setContext(context);
        recorder.recordEvents(inputSource);
    }

    private void trimHeadAndTail(final SaxEventRecorder recorder, final String tagName) {
        // Let's remove the two events with the specified tag before
        // adding the events to the player.

        final List<SaxEvent> saxEventList = recorder.saxEventList;

        if (saxEventList.size() == 0) {
            return;
        }

        final SaxEvent first = saxEventList.get(0);
        if (first != null && first.qName.equalsIgnoreCase(tagName)) {
            saxEventList.remove(0);
        }

        final SaxEvent last = saxEventList.get(recorder.saxEventList.size() - 1);
        if (last != null && last.qName.equalsIgnoreCase(tagName)) {
            saxEventList.remove(recorder.saxEventList.size() - 1);
        }
    }

}
