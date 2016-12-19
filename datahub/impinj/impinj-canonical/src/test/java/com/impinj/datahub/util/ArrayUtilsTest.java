package com.impinj.datahub.util;

import com.impinj.datahub.util.StringHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ArrayUtilsTest
{
	@Before
	public void setUp()
	{
		/*
		try
		{
			assertNotNull(config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS));
			assertTrue(Integer.parseInt(config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS)) >= 1);

			impinjJob = new ImpinjScheduledJob();
			impinjJob.setItemSenseConnection ( new ItemSenseConnection (
					config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_ENDPOINT_URL),
					config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_USERNAME),
					config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_PASSWORD)));
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		*/
	}

	@Test
	public void getFromTimeByLookbackWindowTest()
	{
		String test0 = "warehouse_test";
		String test1 = "one,two,three";
		String test2 = " alpha , beta , theta , omega";
		String test3 = "1|2|3|45|67|89";
		String test4 = "1|2|3|45|67|89|";
		String test5 = "|1|2|3|45|67|89|";
		String test6 = ";1;;2;3;;45;67;89;";

		ArrayList <String> test0List = StringHelper.parseCommaDelimitedStringToList (test0);
		assertNotNull("test 0:  " + test0, test0List);
		assertTrue("test 0:  " + test0, test0List.size ()==1);

		ArrayList <String> test1List = StringHelper.parseCommaDelimitedStringToList (test1);
		assertNotNull("test 1:  " + test1, test1List);
		assertTrue("test 1:  " + test1, test1List.size ()==3);

		ArrayList <String> test2List = StringHelper.parseCommaDelimitedStringToList (test2);
		assertNotNull("test 2:  " + test2, test2List);
		assertTrue("test 2:  " + test2, test2List.size ()==4);

		ArrayList <String> test3List = StringHelper.parsePipeDelimitedStringToList (test3);
		assertNotNull("test 3:  " + test3, test3List);
		assertTrue("test 3:  " + test3, test3List.size ()==6);

		ArrayList <String> test4List = StringHelper.parsePipeDelimitedStringToList (test4);
		assertNotNull("test 4:  " + test4, test4List);
		assertTrue("test 4:  " + test4, test4List.size ()==6);

		ArrayList <String> test5List = StringHelper.parsePipeDelimitedStringToList (test5);
		assertNotNull("test 5:  " + test5, test5List);
		assertTrue("test 5:  " + test5, test5List.size ()==6);

		ArrayList <String> test6List = StringHelper.parseDelimitedStringToList (test6, ";");
		assertNotNull("test 6:  " + test6, test6List);
		assertTrue("test 6:  " + test6, test5List.size ()==6);

	}
}
