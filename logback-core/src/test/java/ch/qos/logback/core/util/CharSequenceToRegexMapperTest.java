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
package ch.qos.logback.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormatSymbols;
import java.util.Locale;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

public class CharSequenceToRegexMapperTest {
	static Locale KO_LOCALE = new Locale("ko", "KR");
	Locale oldLocale = Locale.getDefault();

	@After
	public void tearDown() {
		Locale.setDefault(oldLocale);
	}

	@Test
	public void findMinMaxLengthsInSymbolsWithTrivialInputs() {
		final String[] symbols = { "a", "bb" };
		final int[] results = CharSequenceToRegexMapper.findMinMaxLengthsInSymbols(symbols);
		assertEquals(1, results[0]);
		assertEquals(2, results[1]);
	}

	@Test
	public void emptyStringValuesShouldBeIgnoredByFindMinMaxLengthsInSymbols() {
		final String[] symbols = { "aaa", "" };
		final int[] results = CharSequenceToRegexMapper.findMinMaxLengthsInSymbols(symbols);
		assertEquals(3, results[0]);
		assertEquals(3, results[1]);
	}

	@Test
	@Ignore
	public void noneOfTheSymbolsAreOfZeroLengthForKorean() {
		Locale.setDefault(KO_LOCALE);
		noneOfTheSymbolsAreOfZeroLength();
	}

	@Test
	@Ignore
	public void noneOfTheSymbolsAreOfZeroLengthForSwiss() {
		Locale.setDefault(new Locale("fr", "CH"));
		noneOfTheSymbolsAreOfZeroLength();
	}

	private void noneOfTheSymbolsAreOfZeroLength() {
		final DateFormatSymbols dateFormatSymbols = DateFormatSymbols.getInstance();
		// checkEmptyString(dateFormatSymbols.getShortMonths(), "ShortMonths");
		// checkEmptyString(dateFormatSymbols.getMonths(), "Months");
		checkEmptyString(dateFormatSymbols.getShortWeekdays(), "ShortWeekdays");
		checkEmptyString(dateFormatSymbols.getWeekdays(), "Weekdays");
		checkEmptyString(dateFormatSymbols.getAmPmStrings(), "AmPmStrings");

	}

	private void checkEmptyString(final String[] symbolArray, final String category) {
		for (final String s : symbolArray) {
			System.out.println(category + " [" + s + "]");
			assertTrue(category + " contains empty strings", s.length() > 0);
		}
	}

}
