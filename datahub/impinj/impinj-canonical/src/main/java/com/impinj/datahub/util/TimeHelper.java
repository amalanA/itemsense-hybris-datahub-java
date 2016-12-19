package com.impinj.datahub.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger (TimeHelper.class.getName ());


	public TimeHelper() {
	}

	/**
	 * Utility to calc beginning of query window
	 *
	 * @param toTime
	 * @param queryLookbackWindowInSeconds
	 * @return
	 */
	public static ZonedDateTime getFromTimeByLookbackWindow(ZonedDateTime toTime, long queryLookbackWindowInSeconds) {
		return toTime.minusSeconds (queryLookbackWindowInSeconds);
	}

	/**
	 * Get "now" in ItemSense time (UTC)
	 *
	 * @return
	 */
	public static ZonedDateTime getNowUTC() {
		return ZonedDateTime.now (ZoneOffset.UTC);
	}
}
