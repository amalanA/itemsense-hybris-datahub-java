package com.impinj.datahub.itemsense;

import com.impinj.datahub.constants.ImpinjDatahubConstants;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import com.impinj.datahub.config.DynamicConfig;
import com.impinj.datahub.constants.ImpinjDatahubConstants;

public class ItemSenseJobHelperTest
{
        private static final Logger LOGGER = LoggerFactory.getLogger (ItemSenseJobHelperTest.class.getName ());

	private String url = null;
	private String userName = null;
	private String password = null;
	private String facility = null;

        private final DynamicConfig config = DynamicConfig.getConfig();

	@Before
	public void setUp() {
		
		try {
		url = config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_ENDPOINT_URL);
		userName = config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_USERNAME);
		password = config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_PASSWORD);
		facility = config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_FACILITY);
 		} catch (final IOException e) {
    	     		e.printStackTrace();
		}
	}


	@Test
	public void isJobRunningTest() {

	    ItemSenseConnection isConnection = new ItemSenseConnection ( url, userName, password);

	    ItemSenseJobHelper jobHelper = new ItemSenseJobHelper();
	    try {
                // for now, don't care about the value, only that no exception is thrown
            	boolean isJobRunning  = jobHelper.isJobRunning(isConnection, facility);
		LOGGER.debug("test isJobRunning (" + isJobRunning + ") for " + " : " +url + " : " + userName + " : " + password + " : " + facility);

            } catch (Exception e ) {
		fail("exception thrown when checking for running job");
            }
	}
}
