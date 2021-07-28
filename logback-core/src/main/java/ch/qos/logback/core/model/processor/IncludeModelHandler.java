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

	public IncludeModelHandler(Context context) {
		super(context);
	}

	static public ModelHandlerBase makeInstance(Context context, InterpretationContext ic) {
		return new IncludeModelHandler(context);
	}	

	@Override
	protected Class<IncludeModel> getSupportedModelClass() {
		return IncludeModel.class;
	}

	@Override
	public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		IncludeModel includeModel = (IncludeModel) model;
		SaxEventRecorder localRecorder = new SaxEventRecorder(context, includeModel.getElementPath()) {
			@Override
			public boolean shouldIgnoreForElementPath(String tagName) {
				return JoranConstants.INCLUDED_TAG.equalsIgnoreCase(tagName);
			}
		};

		this.optional = OptionHelper.toBoolean(includeModel.getOptional(), false);

		if (!checkAttributes(includeModel)) {
			return;
		}

		InputStream in = getInputStream(intercon, includeModel);
		try {
			if (in != null) {

				parseAndRecord(in, localRecorder);
				// remove the <included> tag from the beginning and </included> from the end
				trimHeadAndTail(localRecorder, INCLUDED_TAG);

				SaxEventInterpreter localInterpreter = intercon.getSaxEventInterpreter()
						.duplicate(includeModel.getElementPath());
				// add models
				localInterpreter.getEventPlayer().play(localRecorder.saxEventList);
				transferModelStack(includeModel, localInterpreter);
			}
		} catch (JoranException e) {
			addError("Error while parsing  " + attributeInUse, e);
		} finally {
			close(in);
		}
	}

	private void transferModelStack(IncludeModel includeModel, SaxEventInterpreter subInterpreter) {
		Stack<Model> copy = subInterpreter.getInterpretationContext().getCopyOfModelStack();
		for (Model m : copy) {
			includeModel.addSubModel(m);
		}
	}

	private void close(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
	}

	private boolean checkAttributes(IncludeModel includeModel) {
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
		} else if (count > 1) {
			addError("Only one of \"file\", \"url\" or \"resource\" attributes should be set.");
			return false;
		} else if (count == 1) {
			return true;
		}
		throw new IllegalStateException("Count value [" + count + "] is not expected");
	}

	private InputStream getInputStream(InterpretationContext ec, IncludeModel includeModel) {
		URL inputURL = getInputURL(ec, includeModel);
		if (inputURL == null)
			return null;

		ConfigurationWatchListUtil.addToWatchList(context, inputURL);
		return openURL(inputURL);
	}

	private URL getInputURL(InterpretationContext ec, IncludeModel includeModel) {
		String fileAttribute = includeModel.getFile();
		String urlAttribute = includeModel.getUrl();
		String resourceAttribute = includeModel.getResource();

		if (!OptionHelper.isNullOrEmpty(fileAttribute)) {
			this.attributeInUse = ec.subst(fileAttribute);
			return filePathAsURL(attributeInUse);
		}

		if (!OptionHelper.isNullOrEmpty(urlAttribute)) {
			this.attributeInUse = ec.subst(urlAttribute);
			return attributeToURL(attributeInUse);
		}

		if (!OptionHelper.isNullOrEmpty(resourceAttribute)) {
			this.attributeInUse = ec.subst(resourceAttribute);
			return resourceAsURL(attributeInUse);
		}
		// given previous checkAttributes() check we cannot reach this line
		throw new IllegalStateException("A URL stream should have been returned");
	}

	private InputStream openURL(URL url) {
		try {
			return url.openStream();
		} catch (IOException e) {
			optionalWarning("Failed to open [" + url.toString() + "]");
			return null;
		}
	}

	private URL attributeToURL(String urlAttribute) {
		try {
			return new URL(urlAttribute);
		} catch (MalformedURLException mue) {
			String errMsg = "URL [" + urlAttribute + "] is not well formed.";
			addError(errMsg, mue);
			return null;
		}
	}

	private URL resourceAsURL(String resourceAttribute) {
		URL url = Loader.getResourceBySelfClassLoader(resourceAttribute);
		if (url == null) {
			optionalWarning("Could not find resource corresponding to [" + resourceAttribute + "]");
			return null;
		} else
			return url;
	}

	private void optionalWarning(String msg) {
		if (!optional) {
			addWarn(msg);
		}
	}

	URL filePathAsURL(String path) {
		URI uri = new File(path).toURI();
		try {
			return uri.toURL();
		} catch (MalformedURLException e) {
			// impossible to get here
			e.printStackTrace();
			return null;
		}
	}

	private void parseAndRecord(InputStream inputSource, SaxEventRecorder recorder) throws JoranException {
		recorder.setContext(context);
		recorder.recordEvents(inputSource);
	}

	private void trimHeadAndTail(SaxEventRecorder recorder, String tagName) {
		// Let's remove the two events with the specified tag before
		// adding the events to the player.

		List<SaxEvent> saxEventList = recorder.saxEventList;

		if (saxEventList.size() == 0) {
			return;
		}

		SaxEvent first = saxEventList.get(0);
		if (first != null && first.qName.equalsIgnoreCase(tagName)) {
			saxEventList.remove(0);
		}

		SaxEvent last = saxEventList.get(recorder.saxEventList.size() - 1);
		if (last != null && last.qName.equalsIgnoreCase(tagName)) {
			saxEventList.remove(recorder.saxEventList.size() - 1);
		}
	}

}
