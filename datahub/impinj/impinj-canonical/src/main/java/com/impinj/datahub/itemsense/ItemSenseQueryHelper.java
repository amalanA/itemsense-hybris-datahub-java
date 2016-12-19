package com.impinj.datahub.itemsense;

import com.impinj.datahub.util.StringHelper;
import com.impinj.itemsense.client.data.EpcFormat;
import com.impinj.itemsense.client.data.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class ItemSenseQueryHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemSenseQueryHelper.class.getName());


	public ItemSenseQueryHelper() {
	}

	/**
	 * This method is the most efficient method to get a filtered list of items.  ItemSense is used to apply filters.
	 * When ItemSense supports epcprefix list, this will simplify further.
	 *
	 * Note: if you are not getting all the items you want, you may need to shift (initially to the
	 *       getStepThroughFilteredItems method to see what is being omitted.
	 *
	 * @param itemSenseConnection
	 * @param epcFormat
	 * @param facility
	 * @param zones
	 * @param epcPrefixes
	 * @param fromTime
	 * @param toTime
	 * @return
	 */
	public static ArrayList<Item> getFilteredItems(ItemSenseConnection itemSenseConnection,
	              EpcFormat epcFormat, String facility, String zones, String epcPrefixes,
	              ZonedDateTime fromTime, ZonedDateTime toTime ) {


		ArrayList <Item> items = new ArrayList <Item> ();
		ArrayList <String> epcPrefixList = StringHelper.parseCommaDelimitedStringToList (epcPrefixes);
		if (epcPrefixList == null) {
			items = itemSenseConnection
					.getDataController()
					.getItemController()
					.getAllItems(null, null, zones, null, facility, fromTime.toString(), toTime.toString());

		} else {
			// loop through the EPC prefixes
			for (String epcPrefixFilter : epcPrefixList ) {
				// loop through the epcFilters since ItemSense only takes one at a time
				ArrayList <Item> filteredItems = itemSenseConnection
						.getDataController()
						.getItemController()
						.getAllItems(null, epcPrefixFilter, zones, null, facility, fromTime.toString(), toTime.toString());
				items.addAll(filteredItems);
				LOGGER.debug("ItemSenseConnection: " + itemSenseConnection + "  Returned item count: " + filteredItems.size() +
						" FILTERED BY { epcPrefix: " + epcPrefixFilter +
						" zones: " + zones + " facility: " + facility +
						" fromTime: " + fromTime + " toTime: " + toTime + " }");
			}
		}
		LOGGER.debug("ItemSenseConnection: " + itemSenseConnection + "  Returned item count: " + items.size() +
				" FILTERED BY { epcPrefix(s): " + epcPrefixList +
				" zones: " + zones + " facility: " + facility +
				" fromTime: " + fromTime + " toTime: " + toTime + " }");
		return items;
	}

	public static ArrayList<Item> getStepThroughFilteredItems(ItemSenseConnection itemSenseConnection,
	                                               EpcFormat epcFormat, String facility, String zones, String epcPrefixes,
	                                               ZonedDateTime fromTime, ZonedDateTime toTime ) {
		// get All Items
		ArrayList <Item> items = getAllItems(itemSenseConnection, null);

		// Filter Items by epcPrefix




		return items;
	}
	/**
	 * Returns all items from unfiltered ItemSense query - useful for debugging...
	 * @param itemSenseConnection
	 * @param epcFormat
	 * @return
	 */

	public static ArrayList <Item> getAllItems(ItemSenseConnection itemSenseConnection, EpcFormat epcFormat) {

		ArrayList <Item> items = itemSenseConnection
			.getDataController()
			.getItemController()
			.getAllItems(epcFormat, null, null, null, null, null, null);
		LOGGER.debug("ItemSenseConnection: " + itemSenseConnection + "  Returned item count: " + items.size() + " with NO filters applied");
		return items;
	}

}
