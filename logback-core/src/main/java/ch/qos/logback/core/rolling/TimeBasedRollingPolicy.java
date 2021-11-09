/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.rolling;

import static ch.qos.logback.core.CoreConstants.UNBOUNDED_TOTAL_SIZE_CAP;
import static ch.qos.logback.core.CoreConstants.UNBOUND_HISTORY;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.Compressor;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RenameUtil;
import ch.qos.logback.core.util.FileSize;

/**
 * <code>TimeBasedRollingPolicy</code> is both easy to configure and quite
 * powerful. It allows the roll over to be made based on time. It is possible to
 * specify that the roll over occur once per day, per week or per month.
 *
 * <p>For more information, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#TimeBasedRollingPolicy
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class TimeBasedRollingPolicy<E> extends RollingPolicyBase implements TriggeringPolicy<E> {
	static final String FNP_NOT_SET = "The FileNamePattern option must be set before using TimeBasedRollingPolicy. ";
	// WCS: without compression suffix
	FileNamePattern fileNamePatternWithoutCompSuffix;

	private Compressor compressor;
	private final RenameUtil renameUtil = new RenameUtil();
	Future<?> compressionFuture;
	Future<?> cleanUpFuture;

	private int maxHistory = UNBOUND_HISTORY;
	protected FileSize totalSizeCap = new FileSize(UNBOUNDED_TOTAL_SIZE_CAP);

	private ArchiveRemover archiveRemover;

	TimeBasedFileNamingAndTriggeringPolicy<E> timeBasedFileNamingAndTriggeringPolicy;

	boolean cleanHistoryOnStart = false;

	@Override
	public void start() {
		// set the LR for our utility object
		renameUtil.setContext(context);

		// find out period from the filename pattern
		if (fileNamePatternStr == null) {
			addWarn(FNP_NOT_SET);
			addWarn(CoreConstants.SEE_FNP_NOT_SET);
			throw new IllegalStateException(FNP_NOT_SET + CoreConstants.SEE_FNP_NOT_SET);
		}
		fileNamePattern = new FileNamePattern(fileNamePatternStr, context);
		determineCompressionMode();

		compressor = new Compressor(compressionMode);
		compressor.setContext(context);

		// wcs : without compression suffix
		fileNamePatternWithoutCompSuffix = new FileNamePattern(Compressor.computeFileNameStrWithoutCompSuffix(fileNamePatternStr, compressionMode), context);

		addInfo("Will use the pattern " + fileNamePatternWithoutCompSuffix + " for the active file");

		if (compressionMode == CompressionMode.ZIP) {
			final String zipEntryFileNamePatternStr = transformFileNamePattern2ZipEntry(fileNamePatternStr);
			zipEntryFileNamePattern = new FileNamePattern(zipEntryFileNamePatternStr, context);
		}

		if (timeBasedFileNamingAndTriggeringPolicy == null) {
			timeBasedFileNamingAndTriggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy<>();
		}
		timeBasedFileNamingAndTriggeringPolicy.setContext(context);
		timeBasedFileNamingAndTriggeringPolicy.setTimeBasedRollingPolicy(this);
		timeBasedFileNamingAndTriggeringPolicy.start();

		if (!timeBasedFileNamingAndTriggeringPolicy.isStarted()) {
			addWarn("Subcomponent did not start. TimeBasedRollingPolicy will not start.");
			return;
		}

		// the maxHistory property is given to TimeBasedRollingPolicy instead of to
		// the TimeBasedFileNamingAndTriggeringPolicy. This makes it more convenient
		// for the user at the cost of inconsistency here.
		if (maxHistory != UNBOUND_HISTORY) {
			archiveRemover = timeBasedFileNamingAndTriggeringPolicy.getArchiveRemover();
			archiveRemover.setMaxHistory(maxHistory);
			archiveRemover.setTotalSizeCap(totalSizeCap.getSize());
			if (cleanHistoryOnStart) {
				addInfo("Cleaning on start up");
				final Date now = new Date(timeBasedFileNamingAndTriggeringPolicy.getCurrentTime());
				cleanUpFuture = archiveRemover.cleanAsynchronously(now);
			}
		} else if (!isUnboundedTotalSizeCap()) {
			addWarn("'maxHistory' is not set, ignoring 'totalSizeCap' option with value ["+totalSizeCap+"]");
		}

		super.start();
	}

	protected boolean isUnboundedTotalSizeCap() {
		return totalSizeCap.getSize() == UNBOUNDED_TOTAL_SIZE_CAP;
	}

	@Override
	public void stop() {
		if (!isStarted()) {
			return;
		}
		waitForAsynchronousJobToStop(compressionFuture, "compression");
		waitForAsynchronousJobToStop(cleanUpFuture, "clean-up");
		super.stop();
	}

	private void waitForAsynchronousJobToStop(final Future<?> aFuture, final String jobDescription) {
		if (aFuture != null) {
			try {
				aFuture.get(CoreConstants.SECONDS_TO_WAIT_FOR_COMPRESSION_JOBS, TimeUnit.SECONDS);
			} catch (final TimeoutException e) {
				addError("Timeout while waiting for " + jobDescription + " job to finish", e);
			} catch (final Exception e) {
				addError("Unexpected exception while waiting for " + jobDescription + " job to finish", e);
			}
		}
	}

	private String transformFileNamePattern2ZipEntry(final String fileNamePatternStr) {
		final String slashified = FileFilterUtil.slashify(fileNamePatternStr);
		return FileFilterUtil.afterLastSlash(slashified);
	}

	public void setTimeBasedFileNamingAndTriggeringPolicy(final TimeBasedFileNamingAndTriggeringPolicy<E> timeBasedTriggering) {
		this.timeBasedFileNamingAndTriggeringPolicy = timeBasedTriggering;
	}

	public TimeBasedFileNamingAndTriggeringPolicy<E> getTimeBasedFileNamingAndTriggeringPolicy() {
		return timeBasedFileNamingAndTriggeringPolicy;
	}

	@Override
	public void rollover() throws RolloverFailure {

		// when rollover is called the elapsed period's file has
		// been already closed. This is a working assumption of this method.

		final String elapsedPeriodsFileName = timeBasedFileNamingAndTriggeringPolicy.getElapsedPeriodsFileName();

		final String elapsedPeriodStem = FileFilterUtil.afterLastSlash(elapsedPeriodsFileName);

		if (compressionMode == CompressionMode.NONE) {
			if (getParentsRawFileProperty() != null) {
				renameUtil.rename(getParentsRawFileProperty(), elapsedPeriodsFileName);
			} // else { nothing to do if CompressionMode == NONE and parentsRawFileProperty == null }
		} else if (getParentsRawFileProperty() == null) {
			compressionFuture = compressor.asyncCompress(elapsedPeriodsFileName, elapsedPeriodsFileName, elapsedPeriodStem);
		} else {
			compressionFuture = renameRawAndAsyncCompress(elapsedPeriodsFileName, elapsedPeriodStem);
		}

		if (archiveRemover != null) {
			final Date now = new Date(timeBasedFileNamingAndTriggeringPolicy.getCurrentTime());
			this.cleanUpFuture = archiveRemover.cleanAsynchronously(now);
		}
	}

	Future<?> renameRawAndAsyncCompress(final String nameOfCompressedFile, final String innerEntryName) throws RolloverFailure {
		final String parentsRawFile = getParentsRawFileProperty();
		final String tmpTarget = nameOfCompressedFile + System.nanoTime() + ".tmp";
		renameUtil.rename(parentsRawFile, tmpTarget);
		return compressor.asyncCompress(tmpTarget, nameOfCompressedFile, innerEntryName);
	}

	/**
	 *
	 * The active log file is determined by the value of the parent's filename
	 * option. However, in case the file name is left blank, then, the active log
	 * file equals the file name for the current period as computed by the
	 * <b>FileNamePattern</b> option.
	 *
	 * <p>The RollingPolicy must know whether it is responsible for changing the
	 * name of the active file or not. If the active file name is set by the user
	 * via the configuration file, then the RollingPolicy must let it like it is.
	 * If the user does not specify an active file name, then the RollingPolicy
	 * generates one.
	 *
	 * <p> To be sure that the file name used by the parent class has been
	 * generated by the RollingPolicy and not specified by the user, we keep track
	 * of the last generated name object and compare its reference to the parent
	 * file name. If they match, then the RollingPolicy knows it's responsible for
	 * the change of the file name.
	 *
	 */
	@Override
	public String getActiveFileName() {
		final String parentsRawFileProperty = getParentsRawFileProperty();
		if (parentsRawFileProperty != null) {
			return parentsRawFileProperty;
		}
		return timeBasedFileNamingAndTriggeringPolicy.getCurrentPeriodsFileNameWithoutCompressionSuffix();
	}

	@Override
	public boolean isTriggeringEvent(final File activeFile, final E event) {
		return timeBasedFileNamingAndTriggeringPolicy.isTriggeringEvent(activeFile, event);
	}

	/**
	 * Get the number of archive files to keep.
	 *
	 * @return number of archive files to keep
	 */
	public int getMaxHistory() {
		return maxHistory;
	}

	/**
	 * Set the maximum number of archive files to keep.
	 *
	 * @param maxHistory
	 *                number of archive files to keep
	 */
	public void setMaxHistory(final int maxHistory) {
		this.maxHistory = maxHistory;
	}

	public boolean isCleanHistoryOnStart() {
		return cleanHistoryOnStart;
	}

	/**
	 * Should archive removal be attempted on application start up? Default is false.
	 * @since 1.0.1
	 * @param cleanHistoryOnStart
	 */
	public void setCleanHistoryOnStart(final boolean cleanHistoryOnStart) {
		this.cleanHistoryOnStart = cleanHistoryOnStart;
	}

	@Override
	public String toString() {
		return "c.q.l.core.rolling.TimeBasedRollingPolicy@"+hashCode();
	}

	public void setTotalSizeCap(final FileSize totalSizeCap) {
		addInfo("setting totalSizeCap to "+totalSizeCap.toString());
		this.totalSizeCap = totalSizeCap;
	}
}
