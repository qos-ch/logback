package ch.qos.logback.classic.joran;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.StatusUtil;

public class ReconfigureOnChangeTask extends ContextAwareBase implements Runnable {

	public static final String DETECTED_CHANGE_IN_CONFIGURATION_FILES = "Detected change in configuration files.";
	static final String RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION = "Re-registering previous fallback configuration once more as a fallback configuration point";
	static final String FALLING_BACK_TO_SAFE_CONFIGURATION = "Given previous errors, falling back to previously registered safe configuration.";



	long birthdate = System.currentTimeMillis();
	List<ReconfigureOnChangeTaskListener> listeners;


	void addListener(final ReconfigureOnChangeTaskListener listener) {
		if(listeners==null) {
			listeners = new ArrayList<>();
		}
		listeners.add(listener);
	}

	@Override
	public void run() {
		fireEnteredRunMethod();

		final ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(context);
		if (configurationWatchList == null) {
			addWarn("Empty ConfigurationWatchList in context");
			return;
		}

		final List<File> filesToWatch = configurationWatchList.getCopyOfFileWatchList();
		if (filesToWatch == null || filesToWatch.isEmpty()) {
			addInfo("Empty watch file list. Disabling ");
			return;
		}

		if (!configurationWatchList.changeDetected()) {
			return;
		}
		System.out.println("fireChangeDetected");
		fireChangeDetected();
		final URL mainConfigurationURL = configurationWatchList.getMainURL();

		addInfo(DETECTED_CHANGE_IN_CONFIGURATION_FILES);
		addInfo(CoreConstants.RESET_MSG_PREFIX + "named [" + context.getName() + "]");

		final LoggerContext lc = (LoggerContext) context;
		if (mainConfigurationURL.toString().endsWith("xml")) {
			performXMLConfiguration(lc, mainConfigurationURL);
		} else if (mainConfigurationURL.toString().endsWith("groovy")) {
			addError("Groovy configuration disabled due to Java 9 compilation issues.");
		}
		fireDoneReconfiguring();
	}

	private void fireEnteredRunMethod() {
		if(listeners == null) {
			return;
		}

		for(final ReconfigureOnChangeTaskListener listener: listeners) {
			listener.enteredRunMethod();
		}
	}

	private void fireChangeDetected() {
		if(listeners == null) {
			return;
		}

		for(final ReconfigureOnChangeTaskListener listener: listeners) {
			listener.changeDetected();
		}
	}


	private void fireDoneReconfiguring() {
		if(listeners == null) {
			return;
		}

		for(final ReconfigureOnChangeTaskListener listener: listeners) {
			listener.doneReconfiguring();
		}
	}

	private void performXMLConfiguration(final LoggerContext lc, final URL mainConfigurationURL) {
		final JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(context);
		final StatusUtil statusUtil = new StatusUtil(context);
		final List<SaxEvent> eventList = jc.recallSafeConfiguration();

		final URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(context);
		lc.reset();
		final long threshold = System.currentTimeMillis();
		try {
			jc.doConfigure(mainConfigurationURL);
			if (statusUtil.hasXMLParsingErrors(threshold)) {
				fallbackConfiguration(lc, eventList, mainURL);
			}
		} catch (final JoranException e) {
			fallbackConfiguration(lc, eventList, mainURL);
		}
	}

	private List<SaxEvent> removeIncludeEvents(final List<SaxEvent> unsanitizedEventList) {
		final List<SaxEvent> sanitizedEvents = new ArrayList<>();
		if (unsanitizedEventList == null) {
			return sanitizedEvents;
		}

		for (final SaxEvent e : unsanitizedEventList) {
			if (!"include".equalsIgnoreCase(e.getLocalName())) {
				sanitizedEvents.add(e);
			}

		}
		return sanitizedEvents;
	}

	private void fallbackConfiguration(final LoggerContext lc, final List<SaxEvent> eventList, final URL mainURL) {
		// failsafe events are used only in case of errors. Therefore, we must *not*
		// invoke file inclusion since the included files may be the cause of the error.

		final List<SaxEvent> failsafeEvents = removeIncludeEvents(eventList);
		final JoranConfigurator joranConfigurator = new JoranConfigurator();
		joranConfigurator.setContext(context);
		final ConfigurationWatchList oldCWL = ConfigurationWatchListUtil.getConfigurationWatchList(context);
		final ConfigurationWatchList newCWL = oldCWL.buildClone();

		if (failsafeEvents == null || failsafeEvents.isEmpty()) {
			addWarn("No previous configuration to fall back on.");
		} else {
			addWarn(FALLING_BACK_TO_SAFE_CONFIGURATION);
			try {
				lc.reset();
				ConfigurationWatchListUtil.registerConfigurationWatchList(context, newCWL);
				joranConfigurator.playEventsAndProcessModel(failsafeEvents);
				addInfo(RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);
				joranConfigurator.registerSafeConfiguration(eventList);

				addInfo("after registerSafeConfiguration: " + eventList);
			} catch (final JoranException e) {
				addError("Unexpected exception thrown by a configuration considered safe.", e);
			}
		}
	}

	@Override
	public String toString() {
		return "ReconfigureOnChangeTask(born:" + birthdate + ")";
	}
}
