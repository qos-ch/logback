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

import static ch.qos.logback.core.CoreConstants.DAILY_DATE_PATTERN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.SpacePadder;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import ch.qos.logback.core.rolling.testUtil.ScaffoldingForRollingTests;
import ch.qos.logback.core.testUtil.StatusChecker;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.FixedRateInvocationGate;
import ch.qos.logback.core.util.StatusPrinter;

public class TimeBasedRollingWithArchiveRemoval_Test extends ScaffoldingForRollingTests {
	String MONTHLY_DATE_PATTERN = "yyyy-MM";
	String MONTHLY_CRONOLOG_DATE_PATTERN = "yyyy/MM";
	final String DAILY_CRONOLOG_DATE_PATTERN = "yyyy/MM/dd";

	RollingFileAppender<Object> rfa = new RollingFileAppender<>();
	TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<>();

	// by default tbfnatp is an instance of DefaultTimeBasedFileNamingAndTriggeringPolicy
	TimeBasedFileNamingAndTriggeringPolicy<Object> tbfnatp = new DefaultTimeBasedFileNamingAndTriggeringPolicy<>();

	StatusChecker checker = new StatusChecker(context);

	static long MILLIS_IN_MINUTE = 60 * 1000;
	static long MILLIS_IN_HOUR = 60 * MILLIS_IN_MINUTE;
	static long MILLIS_IN_DAY = 24 * MILLIS_IN_HOUR;
	static long MILLIS_IN_MONTH = (long) (365.242199 / 12 * MILLIS_IN_DAY);
	static int MONTHS_IN_YEAR = 12;

	public static final String DAILY_HOUR_PATTERN = "yyyy-MM-dd-HH";

	// Wed Mar 23 23:07:05 CET 2016
	static final long WED_2016_03_23_T_230705_CET = 1458770825333L;
	static final long THU_2016_03_17_T_230330_CET = 1458252210975L;

	int slashCount = 0;
	int ticksPerPeriod = 216;

	ConfigParameters cp; // initialized in setup
	FixedRateInvocationGate fixedRateInvocationGate = new FixedRateInvocationGate(ticksPerPeriod / 2);

	@Override
	@Before
	public void setUp() {
		super.setUp();
		cp = new ConfigParameters(currentTime);
	}

	private int computeSlashCount(final String datePattern) {
		if (datePattern == null) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < datePattern.length(); i++) {
			final char c = datePattern.charAt(i);
			if (c == '/') {
				count++;
			}
		}
		return count;
	}

	// test that the number of files at the end of the test is same as the expected number taking into account end dates
	// near the beginning of a new year. This test has been run in a loop with start date varying over a two years
	// with success.
	@Test
	public void monthlyRolloverOverManyPeriods() {
		slashCount = computeSlashCount(MONTHLY_CRONOLOG_DATE_PATTERN);
		final int maxHistory = 2;
		final int simulatedNumberOfPeriods = 30;
		final String fileNamePattern = randomOutputDir + "/%d{" + MONTHLY_CRONOLOG_DATE_PATTERN + "}/clean.txt.zip";

		cp.maxHistory(maxHistory).fileNamePattern(fileNamePattern).simulatedNumberOfPeriods(simulatedNumberOfPeriods).periodDurationInMillis(MILLIS_IN_MONTH);

		final long startTime = currentTime;
		final long endTime = logOverMultiplePeriods(cp);
		System.out.println("randomOutputDir:" + randomOutputDir);
		System.out.println("start:" + startTime + ", end=" + endTime);
		final int differenceInMonths = RollingCalendar.diffInMonths(startTime, endTime);
		System.out.println("differenceInMonths:" + differenceInMonths);
		final Calendar startTimeAsCalendar = Calendar.getInstance();
		startTimeAsCalendar.setTimeInMillis(startTime);
		final int indexOfStartPeriod = startTimeAsCalendar.get(Calendar.MONTH);
		final boolean withExtraFolder = extraFolder(differenceInMonths, MONTHS_IN_YEAR, indexOfStartPeriod, maxHistory);

		checkFileCount(expectedCountWithFolders(maxHistory, withExtraFolder));
	}

	long generateDailyRollover(final ConfigParameters cp) {
		slashCount = computeSlashCount(DAILY_DATE_PATTERN);
		cp.fileNamePattern(randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt");
		return logOverMultiplePeriods(cp);
	}

	long generateDailyRolloverAndCheckFileCount(final ConfigParameters cp) {
		final long millisAtEnd = generateDailyRollover(cp);
		final int periodBarriersCrossed = computeCrossedDayBarriers(currentTime, millisAtEnd);
		checkFileCount(expectedCountWithoutFoldersWithInactivity(cp.maxHistory, periodBarriersCrossed, cp.startInactivity + cp.numInactivityPeriods));
		return millisAtEnd;
	}

	@Test
	public void checkCrossedPeriodsWithDSTBarrier() {
		final long SAT_2016_03_26_T_230705_CET = WED_2016_03_23_T_230705_CET + 3 * CoreConstants.MILLIS_IN_ONE_DAY;
		final long MON_2016_03_28_T_000705_CET = SAT_2016_03_26_T_230705_CET + CoreConstants.MILLIS_IN_ONE_DAY;

		final int result = computeCrossedDayBarriers(SAT_2016_03_26_T_230705_CET, MON_2016_03_28_T_000705_CET, "CET");
		assertEquals(2, result);
	}

	private int computeCrossedDayBarriers(final long currentTime, final long millisAtEnd) {
		return computeCrossedDayBarriers(currentTime, millisAtEnd, null);
	}

	private int computeCrossedDayBarriers(final long currentTime, final long millisAtEnd, final String timeZoneID) {
		DateTimeZone dateTimeZone = DateTimeZone.getDefault();
		if (timeZoneID != null) {
			dateTimeZone = DateTimeZone.forID(timeZoneID);
		}
		final LocalDate startInstant = new LocalDate(currentTime, dateTimeZone);
		final LocalDate endInstant = new LocalDate(millisAtEnd, dateTimeZone);
		final Days days = Days.daysBetween(startInstant, endInstant);
		return days.getDays();
	}

	@Test
	public void checkCleanupForBasicDailyRollover() {
		cp.maxHistory(20).simulatedNumberOfPeriods(20 * 3).startInactivity(0).numInactivityPeriods(0);
		generateDailyRolloverAndCheckFileCount(cp);
	}

	@Test
	public void checkCleanupForBasicDailyRolloverWithSizeCap() {
		final long bytesOutputPerPeriod = 15984;
		final int sizeInUnitsOfBytesPerPeriod = 2;

		cp.maxHistory(5).simulatedNumberOfPeriods(10).sizeCap(sizeInUnitsOfBytesPerPeriod * bytesOutputPerPeriod + 1000);
		generateDailyRollover(cp);
		checkFileCount(sizeInUnitsOfBytesPerPeriod + 1);
	}

	@Test
	public void checkThatSmallTotalSizeCapLeavesAtLeastOneArhcive() {
		final long WED_2016_03_23_T_131345_CET = WED_2016_03_23_T_230705_CET - 10 * CoreConstants.MILLIS_IN_ONE_HOUR;

		// long bytesOutputPerPeriod = 15984;

		cp = new ConfigParameters(WED_2016_03_23_T_131345_CET);
		final int verySmallCapSize = 1;
		cp.maxHistory(5).simulatedNumberOfPeriods(3).sizeCap(verySmallCapSize);
		generateDailyRollover(cp);
		StatusPrinter.print(context);
		checkFileCountAtMost(1);

	}

	@Test
	public void checkCleanupForBasicDailyRolloverWithMaxSize() {
		cp.maxHistory(6).simulatedNumberOfPeriods(30).startInactivity(10).numInactivityPeriods(1);
		generateDailyRolloverAndCheckFileCount(cp);
	}

	// Since the duration of a month (in seconds) varies from month to month, tests with inactivity period must
	// be conducted with daily rollover not monthly
	@Test
	public void checkCleanupForDailyRollover_15Periods() {
		cp.maxHistory(5).simulatedNumberOfPeriods(15).startInactivity(6).numInactivityPeriods(3);
		generateDailyRolloverAndCheckFileCount(cp);
	}

	@Test
	public void checkCleanupForDailyRolloverWithInactivity_30Periods() {
		// / -------
		cp.maxHistory(2).simulatedNumberOfPeriods(30).startInactivity(3).numInactivityPeriods(1);
		generateDailyRolloverAndCheckFileCount(cp);
	}

	@Test
	public void checkCleanupForDailyRolloverWithInactivity_10Periods() {
		currentTime = THU_2016_03_17_T_230330_CET;
		cp.maxHistory(6).simulatedNumberOfPeriods(10).startInactivity(2).numInactivityPeriods(2);
		generateDailyRolloverAndCheckFileCount(cp);
	}

	@Test
	public void checkCleanupForDailyRolloverWithSecondPhase() {
		slashCount = computeSlashCount(DAILY_DATE_PATTERN);
		final int maxHistory = 5;
		final String fileNamePattern = randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt";

		final ConfigParameters cp0 = new ConfigParameters(currentTime).maxHistory(maxHistory).fileNamePattern(fileNamePattern)
				.simulatedNumberOfPeriods(maxHistory * 2);
		final long endTime = logOverMultiplePeriods(cp0);

		final ConfigParameters cp1 = new ConfigParameters(endTime + MILLIS_IN_DAY * 10).maxHistory(maxHistory).fileNamePattern(fileNamePattern)
				.simulatedNumberOfPeriods(maxHistory);
		logOverMultiplePeriods(cp1);
		checkFileCount(expectedCountWithoutFolders(maxHistory));
	}

	@Test
	public void dailyRolloverWithCronologPattern() {
		slashCount = computeSlashCount(DAILY_CRONOLOG_DATE_PATTERN);
		final String fileNamePattern = randomOutputDir + "/%d{" + DAILY_CRONOLOG_DATE_PATTERN + "}/clean.txt.zip";
		cp.maxHistory(8).fileNamePattern(fileNamePattern).simulatedNumberOfPeriods(8 * 3);
		logOverMultiplePeriods(cp);
		final int expectedDirMin = 9 + slashCount;
		final int expectDirMax = expectedDirMin + 1 + 1;
		expectedFileAndDirCount(9, expectedDirMin, expectDirMax);
	}


	@Test
	public void dailySizeBasedRolloverWithoutCap() {
		final SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<>();
		sizeAndTimeBasedFNATP.invocationGate = fixedRateInvocationGate;

		sizeAndTimeBasedFNATP.setMaxFileSize(new FileSize(10000));
		tbfnatp = sizeAndTimeBasedFNATP;
		slashCount = computeSlashCount(DAILY_DATE_PATTERN);
		final String fileNamePattern = randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}-clean.%i.zip";
		cp.maxHistory(5).fileNamePattern(fileNamePattern).simulatedNumberOfPeriods(5 * 4);
		logOverMultiplePeriods(cp);
		checkPatternCompliance(5 + 1 + slashCount, "\\d{4}-\\d{2}-\\d{2}-clean(\\.\\d)(.zip)?");
	}

	@Test
	public void dailySizeBasedRolloverWithSizeCap() {
		final SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<>();
		sizeAndTimeBasedFNATP.invocationGate = new FixedRateInvocationGate(ticksPerPeriod / 8);
		final long bytesPerPeriod = 17000;
		final long fileSize = bytesPerPeriod / 5;
		final int expectedFileCount = 10;
		final long sizeCap = expectedFileCount * fileSize;
		sizeAndTimeBasedFNATP.setMaxFileSize(new FileSize(fileSize));
		tbfnatp = sizeAndTimeBasedFNATP;
		slashCount = computeSlashCount(DAILY_DATE_PATTERN);

		// 2016-03-05 00:14:39 CET
		final long simulatedTime = 1457133279186L;
		final ConfigParameters params = new ConfigParameters(simulatedTime);
		final String fileNamePattern = randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}-clean.%i";
		params.maxHistory(60).fileNamePattern(fileNamePattern).simulatedNumberOfPeriods(10).sizeCap(sizeCap);
		logOverMultiplePeriods(params);

		final List<File> foundFiles = findFilesByPattern("\\d{4}-\\d{2}-\\d{2}-clean(\\.\\d)");
		Collections.sort(foundFiles, (f0, f1) -> {
			final String s0 = f0.getName().toString();
			final String s1 = f1.getName().toString();
			return s0.compareTo(s1);
		});
		checkFileCount(expectedFileCount - 1);
	}

	@Test
	public void dailyChronologSizeBasedRollover() {
		final SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<>();
		sizeAndTimeBasedFNATP.setMaxFileSize(new FileSize(10000));
		sizeAndTimeBasedFNATP.invocationGate = fixedRateInvocationGate;
		tbfnatp = sizeAndTimeBasedFNATP;
		slashCount = 1;
		final String fileNamePattern = randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}/clean.%i.zip";
		cp.maxHistory(5).fileNamePattern(fileNamePattern).simulatedNumberOfPeriods(5 * 3);
		logOverMultiplePeriods(cp);
		checkDirPatternCompliance(6);
	}

	@Test
	public void dailyChronologSizeBasedRolloverWithSecondPhase() {
		final SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<>();
		sizeAndTimeBasedFNATP.setMaxFileSize(new FileSize(10000));
		sizeAndTimeBasedFNATP.invocationGate = fixedRateInvocationGate;
		tbfnatp = sizeAndTimeBasedFNATP;
		slashCount = 1;
		final String fileNamePattern = randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}/clean.%i";
		final int maxHistory = 5;
		cp.maxHistory(maxHistory).fileNamePattern(fileNamePattern).simulatedNumberOfPeriods(3);
		final long endTime = logOverMultiplePeriods(cp);

		final int simulatedNumberOfPeriods = maxHistory * 4;
		final ConfigParameters cp1 = new ConfigParameters(endTime + MILLIS_IN_DAY * 7).maxHistory(maxHistory).fileNamePattern(fileNamePattern)
				.simulatedNumberOfPeriods(simulatedNumberOfPeriods);
		logOverMultiplePeriods(cp1);
		checkDirPatternCompliance(maxHistory + 1);
	}

	void logTwiceAndStop(long currentTime, final String fileNamePattern, final int maxHistory, final long durationInMillis) {
		final ConfigParameters params = new ConfigParameters(currentTime).fileNamePattern(fileNamePattern).maxHistory(maxHistory);
		buildRollingFileAppender(params, DO_CLEAN_HISTORY_ON_START);
		rfa.doAppend("Hello ----------------------------------------------------------" + new Date(currentTime));
		currentTime += durationInMillis / 2;
		add(tbrp.compressionFuture);
		add(tbrp.cleanUpFuture);
		waitForJobsToComplete();
		tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
		rfa.doAppend("Hello ----------------------------------------------------------" + new Date(currentTime));
		rfa.stop();
	}


	// LOGBACK-1562
	@Test
	public void cleanHistoryOnStartWithHourPattern() {
		long simulatedTime = WED_2016_03_23_T_230705_CET;
		final String fileNamePattern = randomOutputDir + "clean-%d{" + DAILY_HOUR_PATTERN + "}.txt";
		final int maxHistory = 3;
		for (int i = 0; i <= 5; i++) {
			logTwiceAndStop(simulatedTime, fileNamePattern, maxHistory, MILLIS_IN_HOUR);
			simulatedTime += MILLIS_IN_HOUR;
		}
		checkFileCount(expectedCountWithoutFolders(maxHistory));
	}

	@Ignore
	@Test
	// this test assumes a high degree of collisions in the archived files. Every 24 hours, the archive
	// belonging to the previous day will be overwritten. Given that logback goes 14 days (336 hours) in history
	// to clean files on start up, it is bound to delete more recent files. It is not logback's responsibility
	// to cater for such degenerate cases.
	public void cleanHistoryOnStartWithHourPatternWithCollisions() {
		long now = currentTime;
		final String fileNamePattern = randomOutputDir + "clean-%d{HH}.txt";
		final int maxHistory = 3;
		for (int i = 0; i <= 5; i++) {
			logTwiceAndStop(now, fileNamePattern, maxHistory, MILLIS_IN_DAY);
			now = now + MILLIS_IN_HOUR;
		}
		checkFileCount(expectedCountWithoutFolders(maxHistory));
	}

	@Test
	public void cleanHistoryOnStartWithDayPattern() {
		long simulatedTime = WED_2016_03_23_T_230705_CET;
		final String fileNamePattern = randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt";
		final int maxHistory = 3;
		for (int i = 0; i <= 5; i++) {
			logTwiceAndStop(simulatedTime, fileNamePattern, maxHistory, MILLIS_IN_DAY);
			simulatedTime += MILLIS_IN_DAY;
		}
		checkFileCount(expectedCountWithoutFolders(maxHistory));
	}

	@Test
	public void cleanHistoryOnStartWithHourDayPattern() {
		long simulatedTime = WED_2016_03_23_T_230705_CET;
		final String fileNamePattern = randomOutputDir + "clean-%d{yyyy-MM-dd-HH}.txt";
		final int maxHistory = 3;
		for (int i = 0; i <= 5; i++) {
			logTwiceAndStop(simulatedTime, fileNamePattern, maxHistory, MILLIS_IN_HOUR);
			simulatedTime += MILLIS_IN_HOUR;
		}
		checkFileCount(expectedCountWithoutFolders(maxHistory));
	}



	int expectedCountWithoutFolders(final int maxHistory) {
		return maxHistory + 1;
	}

	int expectedCountWithFolders(final int maxHistory, final boolean withExtraFolder) {
		final int numLogFiles = maxHistory + 1;
		final int numLogFilesAndFolders = numLogFiles * 2;
		int result = numLogFilesAndFolders + slashCount;
		if (withExtraFolder) {
			result += 1;
		}
		return result;
	}

	void buildRollingFileAppender(final ConfigParameters cp, final boolean cleanHistoryOnStart) {
		rfa.setContext(context);
		rfa.setEncoder(encoder);
		tbrp.setContext(context);
		tbrp.setFileNamePattern(cp.fileNamePattern);
		tbrp.setMaxHistory(cp.maxHistory);
		tbrp.setTotalSizeCap(new FileSize(cp.sizeCap));
		tbrp.setParent(rfa);
		tbrp.setCleanHistoryOnStart(cleanHistoryOnStart);
		tbrp.timeBasedFileNamingAndTriggeringPolicy = tbfnatp;
		tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(cp.simulatedTime);
		tbrp.start();
		rfa.setRollingPolicy(tbrp);
		rfa.start();
	}

	boolean DO_CLEAN_HISTORY_ON_START = true;
	boolean DO_NOT_CLEAN_HISTORY_ON_START = false;

	long logOverMultiplePeriods(final ConfigParameters cp) {

		buildRollingFileAppender(cp, DO_NOT_CLEAN_HISTORY_ON_START);

		final int runLength = cp.simulatedNumberOfPeriods * ticksPerPeriod;
		final int startInactivityIndex = cp.startInactivity * ticksPerPeriod;
		final int endInactivityIndex = startInactivityIndex + cp.numInactivityPeriods * ticksPerPeriod;
		final long tickDuration = cp.periodDurationInMillis / ticksPerPeriod;

		for (int i = 0; i <= runLength; i++) {
			final Date currentDate = new Date(tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime());
			if (i < startInactivityIndex || i > endInactivityIndex) {
				final StringBuilder sb = new StringBuilder("Hello");
				final String currentDateStr = currentDate.toString();
				final String iAsString = Integer.toString(i);
				sb.append(currentDateStr);
				SpacePadder.spacePad(sb, 66 + 3 - iAsString.length() - currentDateStr.length());
				sb.append(iAsString);
				rfa.doAppend(sb.toString());
			}

			tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(addTime(tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime(), tickDuration));

			add(tbrp.compressionFuture);
			add(tbrp.cleanUpFuture);
			waitForJobsToComplete();
		}


		try {
			Thread.sleep(100);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rfa.stop();

		//System.out.println("Current time at end of loop: "+new Date(tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime()));
		return tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime();
	}

	void fillWithChar(final StringBuffer sb, final char c, final int count) {
		for (int i = 0; i < count; i++) {
			sb.append(c);
		}
	}

	boolean extraFolder(final int numPeriods, final int periodsPerEra, final int beginPeriod, final int maxHistory) {
		final int valueOfLastMonth = (beginPeriod + numPeriods) % periodsPerEra;
		return valueOfLastMonth < maxHistory;
	}

	long addTime(final long time, final long timeToWait) {
		return time + timeToWait;
	}

	void expectedFileAndDirCount(final int expectedFileAndDirCount, final int expectedDirCountMin, final int expectedDirCountMax) {
		final File dir = new File(randomOutputDir);
		final List<File> fileList = new ArrayList<>();
		findFilesInFolderRecursivelyByPatterMatch(dir, fileList, "clean");
		final List<File> dirList = new ArrayList<>();
		findAllFoldersInFolderRecursively(dir, dirList);
		final String msg = "expectedDirCountMin=" + expectedDirCountMin + ", expectedDirCountMax=" + expectedDirCountMax + " actual value=" + dirList.size();
		assertTrue(msg, expectedDirCountMin <= dirList.size() && dirList.size() <= expectedDirCountMax);
	}

	void checkFileCount(final int expectedCount) {
		final File dir = new File(randomOutputDir);
		final List<File> fileList = new ArrayList<>();
		findAllDirsOrStringContainsFilesRecursively(dir, fileList, "clean");
		assertEquals(expectedCount, fileList.size());
	}

	void checkFileCountAtMost(final int expectedCount) {
		final File dir = new File(randomOutputDir);
		final List<File> fileList = new ArrayList<>();
		findAllDirsOrStringContainsFilesRecursively(dir, fileList, "clean");
		final int fileListSize = fileList.size();

		assertTrue("file list size "+ fileListSize+", expectedCount="+expectedCount, fileListSize <= expectedCount);
	}

	int expectedCountWithoutFoldersWithInactivity(final int maxHistory, final int totalPeriods, final int endOfInactivity) {
		final int availableHistory = totalPeriods + 1 - endOfInactivity;
		return Math.min(availableHistory, maxHistory + 1);
	}

	void genericFindMatching(final FileMatchFunction matchFunc, final File dir, final List<File> fileList, final String pattern, final boolean includeDirs) {
		if (dir.isDirectory()) {
			final File[] matchArray = dir.listFiles((FileFilter) f -> f.isDirectory() || matchFunc.match(f, pattern));
			for (final File f : matchArray) {
				if (f.isDirectory()) {
					if (includeDirs) {
						fileList.add(f);
					}
					genericFindMatching(matchFunc, f, fileList, pattern, includeDirs);
				} else {
					fileList.add(f);
				}
			}
		}
	}

	private void findAllFoldersInFolderRecursively(final File dir, final List<File> fileList) {
		final FileMatchFunction alwaysFalse = (f, pattern) -> false;
		genericFindMatching(alwaysFalse, dir, fileList, null, true);
	}

	private void findAllDirsOrStringContainsFilesRecursively(final File dir, final List<File> fileList, final String pattern) {
		final FileMatchFunction matchFunction = (f, pattern1) -> f.getName().contains(pattern1);
		genericFindMatching(matchFunction, dir, fileList, pattern, true);
	}

	void findFilesInFolderRecursivelyByPatterMatch(final File dir, final List<File> fileList, final String pattern) {
		final FileMatchFunction matchByPattern = (f, pattern1) -> f.getName().matches(pattern1);
		genericFindMatching(matchByPattern, dir, fileList, pattern, false);
	}

	Set<String> groupByClass(final List<File> fileList, final String regex) {
		final Pattern p = Pattern.compile(regex);
		final Set<String> set = new HashSet<>();
		for (final File f : fileList) {
			final String n = f.getName();
			final Matcher m = p.matcher(n);
			m.matches();
			final int begin = m.start(1);
			final String reduced = n.substring(0, begin);
			set.add(reduced);
		}
		return set;
	}

	void checkPatternCompliance(final int expectedClassCount, final String regex) {
		final Set<String> set = findFilesByPatternClass(regex);
		assertEquals(expectedClassCount, set.size());
	}

	private List<File> findFilesByPattern(final String regex) {
		final File dir = new File(randomOutputDir);
		final List<File> fileList = new ArrayList<>();
		findFilesInFolderRecursivelyByPatterMatch(dir, fileList, regex);
		return fileList;
	}

	private Set<String> findFilesByPatternClass(final String regex) {
		final List<File> fileList = findFilesByPattern(regex);
		return groupByClass(fileList, regex);
	}

	void checkDirPatternCompliance(final int expectedClassCount) {
		final File dir = new File(randomOutputDir);
		final List<File> fileList = new ArrayList<>();
		findAllFoldersInFolderRecursively(dir, fileList);
		for (final File f : fileList) {
			assertTrue(f.list().length >= 1);
		}
		assertEquals(expectedClassCount, fileList.size());
	}
}
