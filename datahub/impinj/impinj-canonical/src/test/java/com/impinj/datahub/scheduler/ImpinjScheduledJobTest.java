package com.impinj.datahub.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.impinj.datahub.itemsense.ItemSenseConnection;
import org.junit.Before;
import org.junit.Test;

import com.hybris.datahub.dto.integration.RawFragmentData;
import com.impinj.datahub.config.DynamicConfig;
import com.impinj.datahub.constants.ImpinjDatahubConstants;

public class ImpinjScheduledJobTest
{
	private final DynamicConfig config = DynamicConfig.getConfig();
	private ImpinjScheduledJob impinjJob;

	@Before
	public void setUp()
	{
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
			// Configuration file error
			e.printStackTrace();
		}

	}

	@Test
	public void getConnectorTest()
	{
		List<RawFragmentData> rawItems;
		try
		{
			final String feedName = "feed_test";
			final String type = "type_test";
			final String warehouse = "warehouse_test";

			final String masterDataFile = config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_MASTER_DATA_FILE);
			final String epcPrefixes = config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_EPC_PREFIX);
			final String lookbackWindowInSeconds = config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_LOOKBACK_WINDOW_IN_SECONDS);
			final String facility = config.getProperty (ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_FACILITY);
		        final String zones = config.getProperty (ImpinjDatahubConstants.CONFIG_JOBS + ".1." + ImpinjDatahubConstants.CONFIG_ZONES);


System.out.println ("test rawItems: feedname: " + feedName);
System.out.println ("test rawItems: type: " + type);
System.out.println ("test rawItems: warehouse: " + warehouse);
System.out.println ("test rawItems: masterDataFile: " + masterDataFile);
System.out.println ("test rawItems: epcPrefixes: " + epcPrefixes);
System.out.println ("test rawItems: lookbackWindowInSeconds: " + lookbackWindowInSeconds);
System.out.println ("test rawItems: facility: " + facility);
System.out.println ("test rawItems: zones: " + zones);
			rawItems = impinjJob.createRawDataFromImpinj(
					feedName,
					type,
					warehouse,
					masterDataFile,
					epcPrefixes,
					Integer.parseInt(lookbackWindowInSeconds),
					facility,
					zones);
			assertNotNull(rawItems);

			// could eventually get zero items from itemsense.
			if (rawItems.size() > 0)
			{
				final RawFragmentData rawItem = rawItems.get(0);
				assertEquals(feedName, rawItem.getDataFeedName());
				assertEquals(type, rawItem.getType());
				assertEquals(warehouse, rawItem.getValueMap().get(ImpinjDatahubConstants.RAW_WAREHOUSE_ID));
				assertNotNull(rawItem.getValueMap().get(ImpinjDatahubConstants.RAW_PRODUCT_CODE));
				assertNotNull(rawItem.getValueMap().get(ImpinjDatahubConstants.RAW_STOCK_LEVEL));
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

}
