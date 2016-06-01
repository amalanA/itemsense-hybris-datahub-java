package com.impinj.datahub.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import com.hybris.datahub.dto.integration.RawFragmentData;
import com.impinj.datahub.config.ImpinjConfiguration;
import com.impinj.datahub.constants.ImpinjDatahubConstants;
import com.impinj.datahub.itemsense.ItemSenseApiFactory;
import com.impinj.datahub.itemsense.ItemSenseConfiguration;
import com.impinj.itemsense.client.Item;
import com.impinj.itemsense.client.ItemApiLib;
import com.impinj.itemsense.client.ControlApiLib;
import com.impinj.datahub.data.ProductStockLevel;

/**
 * Job that creates a connection between impinj and retrieves stock information.
 */
public class ImpinjScheduledJob implements Job, ApplicationContextAware
{
	private static ApplicationContext applicationContext;
	private MessageChannel rawFragmentDataInputChannel;
	private ItemSenseConfiguration itemsenseConfiguration;
	private ItemApiLib itemApiLib;
	private ControlApiLib controlApiLib;

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

		setItemsenseConfiguration(ImpinjConfiguration.getConfiguration(
				dataMap.getString(ImpinjDatahubConstants.CONFIG_ENDPOINT_URL),
				dataMap.getString(ImpinjDatahubConstants.CONFIG_USERNAME), dataMap.getString(ImpinjDatahubConstants.CONFIG_PASSWORD)));
		setControlApiLib(ItemSenseApiFactory.getControlApiLib(getItemsenseConfiguration()));

		// validate a job is running.  If not, log as a warning and do not update any data
		try {
			if (!getControlApiLib().anyJobIsRunning())
			{
				LOGGER.info("No ItemSense Job is running for URL: " + getItemsenseConfiguration().getBaseUrl() + ".  Please schedule/run a job" );
			}
			else
			{
				// Job is active, get ItemSense item Api Client
				setItemApiLib(ItemSenseApiFactory.getItemApiLib(getItemsenseConfiguration()));
	
				if (rawFragmentDataInputChannel == null)
				{
					rawFragmentDataInputChannel = (MessageChannel) SpringContext.getApplicationContext().getBean(
							ImpinjDatahubConstants.RAW_FRAGMENT_DATA_INPUT_CHANNEL);
				}
				final List<RawFragmentData> rawData = createRawDataFromImpinj(ImpinjDatahubConstants.DATAHUB_FEED,
						ImpinjDatahubConstants.DATAHUB_IMPINJ_RAW_ITEM_TYPE, dataMap.getString(ImpinjDatahubConstants.CONFIG_WAREHOUSE),
						dataMap.getString(ImpinjDatahubConstants.CONFIG_MASTER_DATA_FILE),
						dataMap.getString(ImpinjDatahubConstants.CONFIG_EPC_PREFIX),
						dataMap.getIntFromString(ImpinjDatahubConstants.CONFIG_LOOKBACK_WINDOW_IN_SECONDS));
				rawFragmentDataInputChannel.send(new GenericMessage(rawData));
			}
		} 
		catch (final IOException e)
		{
		    LOGGER.error("Error querying for for an active job.  ItemSense configuration: " + getItemsenseConfiguration() + " exception: " + e);

		}

	}

	/**
	 * Connects to itemsense to retrieve data and convert it to Raw.
	 *
	 * @param context the jobExecutionContext from Quartz
	 * @param feedNxame
	 * @param type
	 * @return
	 */
	protected List<RawFragmentData> createRawDataFromImpinj(final String feedName, final String type, final String warehouse,
			final String hybrisMasterDataFile, final String epcPrefix, final int lookbackWindowInSeconds)
	{
		final List<RawFragmentData> result = new ArrayList<>();
		RawFragmentData rawFragmentData;

		final Collection<Item> items = getItemApiLib().showAllItems(/* ImpinjDatahubConstants.EPC_FORMAT */"");
		LOGGER.info("ItemSense reported item count: " + items.size());

		final HashMap<String, Integer> hybrisInventoryCount = ProductStockLevel.getMasterDataStockLevelsFromItems(
				hybrisMasterDataFile, items, epcPrefix, lookbackWindowInSeconds);

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
	public ItemSenseConfiguration getItemsenseConfiguration()
	{
		return itemsenseConfiguration;
	}

	/**
	 * @param itemsenseConfiguration the itemsenseConfiguration to set
	 */
	public void setItemsenseConfiguration(final ItemSenseConfiguration itemsenseConfiguration)
	{
		this.itemsenseConfiguration = itemsenseConfiguration;
	}

	/**
	 * @return the controlApiLib
	 */
	public ControlApiLib getControlApiLib()
	{
		return controlApiLib;
	}

	/**
	 * @param controlApiLib the controlApiLib to set
	 */
	public void setControlApiLib(final ControlApiLib controlApiLib)
	{
		this.controlApiLib = controlApiLib;
	}
	/**
	 * @return the itemApiLib
	 */
	public ItemApiLib getItemApiLib()
	{
		return itemApiLib;
	}

	/**
	 * @param itemApiLib the itemApiLib to set
	 */
	public void setItemApiLib(final ItemApiLib itemApiLib)
	{
		this.itemApiLib = itemApiLib;
	}
}
