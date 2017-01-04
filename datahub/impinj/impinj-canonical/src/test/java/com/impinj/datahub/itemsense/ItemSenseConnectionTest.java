package com.impinj.datahub.itemsense;

import com.impinj.datahub.constants.ImpinjDatahubConstants;

import com.impinj.itemsense.client.coordinator.CoordinatorApiController;
import com.impinj.itemsense.client.data.DataApiController;
import com.impinj.itemsense.client.coordinator.job.JobController;
import com.impinj.itemsense.client.coordinator.job.JobResponse;
import com.impinj.itemsense.client.coordinator.readerhealth.ReaderStatus;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;


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


	@Test
	public void getConnectionCoordinatorControllerTest() {

	    System.out.println ("ItemSenseConnection CAC test : url: " + url);
	    System.out.println ("ItemSenseConnection CAC test : userName: " + userName);
	    System.out.println ("ItemSenseConnection CAC test : password: " + password);
	    ItemSenseConnection isConnection = new ItemSenseConnection ( url, userName, password);


	    CoordinatorApiController cac = isConnection.getCoordinatorController();
	    assertNotNull ("coordinator api controller", cac);
	    assertTrue ("coordinator api controller", cac!=null);

            // do something before job - 
	    List <ReaderStatus> readerStatusList = cac.getHealthController().getAllReaderStatuses();
	    assertNotNull ("ReaderStatusList", readerStatusList);
	    // and finally do something with the conroller
		
	    JobController jobController = cac.getJobController();
	    assertNotNull ("job controller", jobController);
	    Response jobsAsResponse = jobController.getJobsAsResponse();
	    assertNotNull ("jobAsResponse", jobsAsResponse);
	    System.out.println ("jobAsResponse" + jobsAsResponse);
	    //System.out.println ("jobAsResponse" + jobsAsResponse.readEntity(String.class));

	    List <JobResponse> jobStrings = jobsAsResponse.readEntity(new GenericType<List<JobResponse>>() {} );


            List <JobResponse> jobResponseList = jobController.getJobs();
	    assertNotNull ("jobResponseList", jobResponseList);

	}


	@Test
	public void getConnectionDataControllerTest() {

	    System.out.println ("ItemSenseConnection DAC test : url: " + url);
	    System.out.println ("ItemSenseConnection DAC test : userName: " + userName);
	    System.out.println ("ItemSenseConnection DAC test : password: " + password);
		ItemSenseConnection isConnection = new ItemSenseConnection ( url, userName, password);


	    DataApiController dac = isConnection.getDataController();
	    assertNotNull ("data api controller", dac);
	    assertTrue ("data api controller", dac!=null);

	}

}

