package com.impinj.datahub.itemsense;

import com.impinj.datahub.constants.ImpinjDatahubConstants;

//import com.impinj.itemsense.client.coordinator.CoordinatorApiController;
//import com.impinj.itemsense.client.data.DataApiController;
//import com.impinj.itemsense.client.coordinator.job.JobController;
//import com.impinj.itemsense.client.coordinator.job.JobResponse;
//import com.impinj.itemsense.client.coordinator.readerhealth.ReaderStatus;
import com.impinj.itemsense.client.coordinator.ControlApiLib;
import com.impinj.itemsense.client.data.ItemApiLib;


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import com.impinj.datahub.config.DynamicConfig;
import com.impinj.datahub.constants.ImpinjDatahubConstants;

public class ItemSenseConnectionTest
{

	private String url = null;
	private String userName = null;
	private String password = null;

        private final DynamicConfig config = DynamicConfig.getConfig();

	@Before
	public void setUp() {
		
		try {
		url = config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_ENDPOINT_URL);
		userName = config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_USERNAME);
		password = config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_PASSWORD);
 		} catch (final IOException e) {
    	     		e.printStackTrace();
		}
	}


/*
	@Test
	public void getConnectionCoordinatorControllerTest() {

	    ItemSenseConnection isConnection = new ItemSenseConnection ( url, userName, password);


	    CoordinatorApiController cac = isConnection.getCoordinatorController();
	    assertNotNull ("coordinator api controller", cac);
	    assertTrue ("coordinator api controller", cac!=null);

            // do something before job - 
	    //List <ReaderStatus> readerStatusList = cac.getHealthController().getAllReaderStatuses();
	    //assertNotNull ("ReaderStatusList", readerStatusList);
	    // and finally do something with the conroller
		
	    JobController jobController = cac.getJobController();
	    assertNotNull ("job controller", jobController);
	    Response jobsAsResponse = jobController.getJobsAsResponse();
	    assertNotNull ("jobAsResponse", jobsAsResponse);

            List <JobResponse> jobResponseList = jobController.getJobs();
	    assertNotNull ("jobResponseList", jobResponseList);

	}
*/


/*
	@Test
	public void getConnectionDataControllerTest() {

		ItemSenseConnection isConnection = new ItemSenseConnection ( url, userName, password);


	    DataApiController dac = isConnection.getDataController();
	    assertNotNull ("data api controller", dac);
	    assertTrue ("data api controller", dac!=null);

	}
*/
       @Test
       public void getItemApiLibTest () {
           ItemSenseConnection isConnection = new ItemSenseConnection ( url, userName, password);
       	   ItemApiLib ial = isConnection.getItemApiLib();
	   assertNotNull ("ItemApiLib", ial);
	   assertTrue ("ItemApiLib", ial!=null);
       }
}

