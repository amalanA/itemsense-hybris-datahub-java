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
	private ItemSenseConnection itemSenseConnection2;

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
                String url = dataMap.getString(ImpinjDatahubConstants.CONFIG_ENDPOINT_URL);
                String itemsenseUserName = dataMap.getString(ImpinjDatahubConstants.CONFIG_USERNAME);
                String itemsensePassword = dataMap.getString(ImpinjDatahubConstants.CONFIG_PASSWORD);
		LOGGER.info("+++++++ItemSenseConnection1: URL: " + url + " isUsername: " + itemsenseUserName + " isPassword: " + itemsensePassword);
		setItemSenseConnection( new ItemSenseConnection ( url, itemsenseUserName, itemsensePassword));

                url = dataMap.getString(ImpinjDatahubConstants.CONFIG_ENDPOINT_URL2);
                itemsenseUserName = dataMap.getString(ImpinjDatahubConstants.CONFIG_USERNAME2);
                itemsensePassword = dataMap.getString(ImpinjDatahubConstants.CONFIG_PASSWORD2);
		LOGGER.info("+++++++ItemSenseConnection2: URL: " + url + " isUsername: " + itemsenseUserName + " isPassword: " + itemsensePassword);
		setItemSenseConnection2( new ItemSenseConnection ( url, itemsenseUserName, itemsensePassword));

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
						dataMap.getString(ImpinjDatahubConstants.CONFIG_FACILITY2),
						dataMap.getString(ImpinjDatahubConstants.CONFIG_ZONES),
						dataMap.getString(ImpinjDatahubConstants.CONFIG_ZONES2)
						);
				rawFragmentDataInputChannel.send(new GenericMessage(rawData));
			}
	}

	/**
	 * Connects to itemsense to retrieve data and convert it to Raw.
	 */
	protected List<RawFragmentData> createRawDataFromImpinj(final String feedName, final String type, final String warehouse,
			final String hybrisMasterDataFile, final String epcPrefix, final long lookbackWindowInSeconds,
            final String facility, final String facility2, final String zones, final String zones2)
	{
		final List<RawFragmentData> result = new ArrayList<>();
		RawFragmentData rawFragmentData;
		ZonedDateTime toTime = TimeHelper.getNowUTC ();
		ZonedDateTime fromTime = TimeHelper.getFromTimeByLookbackWindow (toTime, lookbackWindowInSeconds);

		ItemSenseQueryHelper isQueryHelper = new ItemSenseQueryHelper ();
		final Collection<Item> allItems = isQueryHelper.getStepThroughFilteredItems (getItemSenseConnection (),
				null, facility, zones, epcPrefix, fromTime, toTime);
		LOGGER.info("ItemSense (filtered) reported item count: " + allItems.size());

		final Collection<Item> itemsToRemove = isQueryHelper.getStepThroughFilteredItems (getItemSenseConnection2 (),
				null, facility2, zones2, epcPrefix, fromTime, toTime);
		LOGGER.info("ItemSense (filtered) reported local item count: " + itemsToRemove.size());

		final Collection <Item> items = rationalizeItemsBetweenItemSenses( allItems, itemsToRemove );

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
	/**
	 * @return the second itemsenseConfiguration
	 */
	public ItemSenseConnection getItemSenseConnection2()
	{
		return itemSenseConnection2;
	}

	/**
	 * @param itemSenseConnection the itemsenseConfiguration to set
	 */
	public void setItemSenseConnection2(final ItemSenseConnection itemSenseConnection2)
	{
		this.itemSenseConnection2 = itemSenseConnection2;
	}

	/** HACK to let rationalize items from 2 itemsenses.  If item is in itemsToRemove, and item is in allItems, remove it
	 *  because it is in one of the local zones that takes it out of online ATP
	 *
	 * @param allItems
	 * @param itemsToRemove
	 * @return
	 */
	protected Collection <Item> rationalizeItemsBetweenItemSenses( Collection <Item> allItems, Collection <Item> itemsToRemove ) {

 		if (allItems == null || allItems.size() == 0) {
                   return allItems;
                }
                if (itemsToRemove == null ||  itemsToRemove.size() == 0) {
                   return allItems;
                }

		LOGGER.debug("size of all items before rationalizing: " + allItems.size());
		LOGGER.debug("size of all items to remove before rationalizing: " + itemsToRemove.size());
  		// old school - iterators
                // If it matches a remove item, remove it, and remove it from the remove list 
                Iterator itemItr = allItems.iterator();
                while (itemItr.hasNext()) {
                    Item item = (Item) itemItr.next();
                    Iterator removeItr = itemsToRemove.iterator();
                    while (removeItr.hasNext()) {
                        Item itemToRemove = (Item) removeItr.next();
		//LOGGER.debug("rationalizing: item EPC: " + item.getEpc());
		//LOGGER.debug("rationalizing: remove EPC: " + itemToRemove.getEpc());
		        if (item.getEpc().equals(itemToRemove.getEpc())) {
    			    itemItr.remove();
    			    removeItr.remove();
		LOGGER.debug("rationalizing: removing EPC " +item.getEpc() +" from inventory");
                            break;
		        }
		    }
                }
		LOGGER.debug("size of all items after rationalizing: " + allItems.size());
		LOGGER.debug("size of all items to remove after rationalizing: " + itemsToRemove.size());
		return allItems;

	}
}
