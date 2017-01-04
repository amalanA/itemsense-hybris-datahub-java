package com.impinj.datahub.scheduler;


import com.hybris.datahub.dto.integration.RawFragmentData;

import com.impinj.datahub.util.TimeHelper;
import com.impinj.itemsense.client.data.item.Item;
import com.impinj.datahub.itemsense.ItemSenseJobHelper;
import com.impinj.datahub.itemsense.ItemSenseQueryHelper;
import com.impinj.datahub.itemsense.ItemSenseConnection;
import com.impinj.datahub.constants.ImpinjDatahubConstants;
import com.impinj.datahub.data.ProductStockLevel;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Job that creates a connection between impinj and retrieves stock information.
 */
public class ImpinjScheduledJob implements Job, ApplicationContextAware
{
	private static ApplicationContext applicationContext;
	private MessageChannel rawFragmentDataInputChannel;
	private ItemSenseConnection itemSenseConnection;

	private static final Logger LOGGER = LoggerFactory.getLogger(ImpinjScheduledJob.class);

	/**
	 * @param rawFragmentDataInputChannel the input channel
	 */
	@Required
	public void setRawFragmentDataInputChannel(final MessageChannel rawFragmentDataInputChannel)
	{
		this.rawFragmentDataInputChannel = rawFragmentDataInputChannel;
	}

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException
	{
		final JobKey jobKey = context.getJobDetail().getKey();
		final JobDataMap dataMap = context.getJobDetail().getJobDataMap();

		LOGGER.info("Impinj Data Hub job is running: Job " + jobKey.getName());

		setItemSenseConnection( new ItemSenseConnection (
				dataMap.getString(ImpinjDatahubConstants.CONFIG_ENDPOINT_URL),
				dataMap.getString(ImpinjDatahubConstants.CONFIG_USERNAME),
				dataMap.getString(ImpinjDatahubConstants.CONFIG_PASSWORD)));

		// validate a job is running.  If not, log as a warning and do not update any data

		ItemSenseJobHelper jobHelper = new ItemSenseJobHelper ();
		if (!jobHelper.isJobRunning (getItemSenseConnection (), dataMap.getString(ImpinjDatahubConstants.CONFIG_FACILITY) )) {
				LOGGER.info("No ItemSense Job is running for URL: " + getItemSenseConnection().getBaseUrl() + " facility: "
						+ dataMap.getString(ImpinjDatahubConstants.CONFIG_FACILITY)  + ".  Please schedule/run a job" );
			}
			else
			{
				if (rawFragmentDataInputChannel == null)
				{
					rawFragmentDataInputChannel = (MessageChannel) SpringContext.getApplicationContext().getBean(
							ImpinjDatahubConstants.RAW_FRAGMENT_DATA_INPUT_CHANNEL);
				}
				final List<RawFragmentData> rawData = createRawDataFromImpinj(ImpinjDatahubConstants.DATAHUB_FEED,
						ImpinjDatahubConstants.DATAHUB_IMPINJ_RAW_ITEM_TYPE, dataMap.getString(ImpinjDatahubConstants.CONFIG_WAREHOUSE),
						dataMap.getString(ImpinjDatahubConstants.CONFIG_MASTER_DATA_FILE),
						dataMap.getString(ImpinjDatahubConstants.CONFIG_EPC_PREFIX),
						dataMap.getLongFromString(ImpinjDatahubConstants.CONFIG_LOOKBACK_WINDOW_IN_SECONDS),
						dataMap.getString(ImpinjDatahubConstants.CONFIG_FACILITY),
						dataMap.getString(ImpinjDatahubConstants.CONFIG_ZONES)
						);
				rawFragmentDataInputChannel.send(new GenericMessage(rawData));
			}
	}

	/**
	 * Connects to itemsense to retrieve data and convert it to Raw.
	 */
	protected List<RawFragmentData> createRawDataFromImpinj(final String feedName, final String type, final String warehouse,
			final String hybrisMasterDataFile, final String epcPrefix, final long lookbackWindowInSeconds,
            final String facility, final String zones)
	{
		final List<RawFragmentData> result = new ArrayList<>();
		RawFragmentData rawFragmentData;
		ZonedDateTime toTime = TimeHelper.getNowUTC ();
		ZonedDateTime fromTime = TimeHelper.getFromTimeByLookbackWindow (toTime, lookbackWindowInSeconds);

		ItemSenseQueryHelper isQueryHelper = new ItemSenseQueryHelper ();
		final Collection<Item> items = isQueryHelper.getFilteredItems (getItemSenseConnection (),
				null, facility, zones, epcPrefix, fromTime, toTime);
		LOGGER.info("ItemSense (filtered) reported item count: " + items.size());

		final HashMap<String, Integer> hybrisInventoryCount = ProductStockLevel.getMasterDataStockLevelsFromItems(
				hybrisMasterDataFile, items);

		for (final Map.Entry<String, Integer> item : hybrisInventoryCount.entrySet())
		{
			rawFragmentData = new RawFragmentData();
			final Map<String, String> line = new HashMap<>();
			line.put(ImpinjDatahubConstants.RAW_PRODUCT_CODE, item.getKey());
			line.put(ImpinjDatahubConstants.RAW_STOCK_LEVEL, String.valueOf(item.getValue()));
			line.put(ImpinjDatahubConstants.RAW_WAREHOUSE_ID, warehouse);

			rawFragmentData.setValueMap(line);
			rawFragmentData.setType(type);
			rawFragmentData.setDataFeedName(feedName);
			rawFragmentData.setExtensionSource(ImpinjDatahubConstants.DATAHUB_EXTENSION_SOURCE);

			result.add(rawFragmentData);
		}

		return result;
	}

	public void setApplicationContext(final ApplicationContext context) throws BeansException
	{
		this.applicationContext = context;
	}

	/**
	 * @return the application context
	 */
	public static ApplicationContext getApplicationContext()
	{
		return applicationContext;

	}

	/**
	 * @return the itemsenseConfiguration
	 */
	public ItemSenseConnection getItemSenseConnection()
	{
		return itemSenseConnection;
	}

	/**
	 * @param itemSenseConnection the itemsenseConfiguration to set
	 */
	public void setItemSenseConnection(final ItemSenseConnection itemSenseConnection)
	{
		this.itemSenseConnection = itemSenseConnection;
	}
}
