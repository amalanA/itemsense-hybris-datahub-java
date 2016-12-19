package com.impinj.datahub.util;

import com.impinj.datahub.constants.ImpinjDatahubConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class StringHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringHelper.class.getName());


	public StringHelper() {
	}

	/**
	 * convienence for comma (,) delimiter
	 *
	 * @param commaDelimitedString
	 * @return
	 */
	public static ArrayList<String> parseCommaDelimitedStringToList (String commaDelimitedString) {
		return parseDelimitedStringToList (commaDelimitedString, ImpinjDatahubConstants.COMMA_DELIMITER);
	}

	/**
	 * convienence for pipe (|) delimiter
	 *
	 * @param pipeDelimitedList
	 * @return
	 */
	public static ArrayList<String> parsePipeDelimitedStringToList (String pipeDelimitedList) {
		return parseDelimitedStringToList (pipeDelimitedList, ImpinjDatahubConstants.PIPE_DELIMITER);
	}

	/**
	 * Utility to parse delimited string to list of strings
	 *
	 * @param delimitedString
	 * @param delimiter
	 * @return Array list
	 */
	public static ArrayList<String> parseDelimitedStringToList (String delimitedString, String delimiter) {
		ArrayList <String> list = null;
		// if something was passed in that is not null and has a value other than whitespace, parse it.
		if (delimitedString!=null && delimitedString.trim().length() > 0) {
			list = new ArrayList<String> ();
			StringTokenizer st = new StringTokenizer (delimitedString.trim (), delimiter);
			while (st.hasMoreTokens ()) {
				String value = st.nextToken ().trim();
				if (value.length () > 0) {
					list.add (value);
				}
			}
		}
		return list;
	}

}
