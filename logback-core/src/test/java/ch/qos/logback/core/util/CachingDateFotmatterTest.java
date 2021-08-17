package ch.qos.logback.core.util;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

public class CachingDateFotmatterTest {

	final static String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm";

	SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
	TimeZone perthTZ = TimeZone.getTimeZone("Australia/Perth");
	TimeZone utcTZ = TimeZone.getTimeZone("UTC");

	@Before
	public void setUp() {
		sdf.setTimeZone(utcTZ);
	}

	@Test
	public void timeZoneIsTakenIntoAccount() throws ParseException {

		ZoneId perthZone = ZoneId.of("Australia/Perth");
		CachingDateFormatter cdf = new CachingDateFormatter(DATE_PATTERN, perthZone);

		Date march26_2015_0949_UTC = sdf.parse("2015-03-26T09:49");
		System.out.print(march26_2015_0949_UTC);

		String result = cdf.format(march26_2015_0949_UTC.getTime());
		// AWST (Perth) is 8 hours ahead of UTC
		assertEquals("2015-03-26T17:49", result);
	}


}
