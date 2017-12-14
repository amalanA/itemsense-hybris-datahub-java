package com.impinj.datahub.itemsense;

import com.impinj.datahub.util.StringHelper;
import com.impinj.itemsense.client.data.EpcFormat;
import com.impinj.itemsense.client.data.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

import java.util.stream.Collectors;

public class ItemSenseQueryHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemSenseQueryHelper.class.getName());

	public ItemSenseQueryHelper() {
	}

	/**
	 * This method is the most efficient method to get a filtered list of items.  ItemSense is used to apply filters.
	 * When ItemSense supports epcprefix list, this will simplify further.
	 *
	 * Note: if you are not getting all the items you want, you may need to shift (initially) to the
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
	 * /
  TODO - fix this!!
	public ArrayList<Item> getFilteredItems(ItemSenseConnection itemSenseConnection,
	              EpcFormat epcFormat, String facility, String zones, String epcPrefixes,
	              ZonedDateTime fromTime, ZonedDateTime toTime ) {


		ArrayList <Item> items = new ArrayList <> ();
		ArrayList <String> epcPrefixList = StringHelper.parseCommaDelimitedStringToList (epcPrefixes);

                // if multiple EPC prefixes are specified, multiple ItemSense calls are required
		if (epcPrefixList == null) {
			Collection <Item>myitems = itemSenseConnection.getItemApiLib().showAllItems(null);
			items.addAll(myitems);
		} else {
			// loop through the EPC prefixes
			for (String epcPrefixFilter : epcPrefixList ) {
				// loop through the epcFilters since ItemSense only takes one at a time
				Collection <Item> filteredItems = itemSenseConnection.getDataController()
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
*/

	public Collection<Item> getStepThroughFilteredItems(ItemSenseConnection itemSenseConnection,
	                                               EpcFormat epcFormat, String facility, String zones, String epcPrefixes,
	                                               ZonedDateTime fromTime, ZonedDateTime toTime ) {
		// get All Items
		LOGGER.debug("getStepThroughFilteredItems: " + itemSenseConnection + 
				" FILTERED BY { epcPrefix(s): " + epcPrefixes +
				" zones: " + zones + " facility: " + facility +
				" fromTime: " + fromTime + " toTime: " + toTime + " }");
		Collection <Item> items = getAllItems(itemSenseConnection, null);

		// Filter Items by epcPrefix
		items = filterItemsByPrefixes(items, epcPrefixes);

		// Filter Items by to/from times
		items = filterItemsByTime(items, fromTime, toTime);

		// Filter Items by facility
		items = filterItemsByFacility(items, facility);

		// Filter Items by zones
		items = filterItemsByZones(items, zones);

		return items;
	}
	/**
	 * Returns all items from unfiltered ItemSense query - useful for debugging...
	 * @param itemSenseConnection
	 * @param epcFormat
	 * @return
	 */

	public Collection <Item> getAllItems(ItemSenseConnection itemSenseConnection, EpcFormat epcFormat) {

		Collection <Item> items = itemSenseConnection
			//.getItemApiLib().showAllItems(epcFormat);
			.getItemApiLib().showAllItems(null);
		LOGGER.debug("ItemSenseConnection: " + itemSenseConnection + "  Returned item count: " + items.size() + " with NO filters applied");
		return items;
	}

	// Filter Items by epcPrefix
	public Collection <Item> filterItemsByPrefixes(Collection<Item> items, String epcPrefixes) {
		LOGGER.debug("Before filtering by Prefix(s): " + epcPrefixes + " itemCount: " + items.size());
		if (epcPrefixes == null || epcPrefixes.trim().length () == 0) {
			LOGGER.info ("No epc prefix filters specified.  No prefix filter applied.");
			return items;
		}
		ArrayList <Item> filteredItems = new ArrayList <> ();
		for (String epcPrefix : StringHelper.parseCommaDelimitedStringToList (epcPrefixes) ) {

			ArrayList <Item> itemsPerFilter = items
					.stream()
					.filter(item -> item.getEpc().startsWith(epcPrefix))
					.collect(Collectors.toCollection (ArrayList::new));


			LOGGER.debug ("     Item count for epcPrefix: " + epcPrefix + " is : " + itemsPerFilter.size ());

			filteredItems.addAll (itemsPerFilter);
		}
		LOGGER.debug("After filtering by prefix(s): " + epcPrefixes + " itemCount: " + filteredItems.size());
		return filteredItems;
	}

	// Filter Items by to/from times
	public Collection <Item> filterItemsByTime(Collection<Item> items, ZonedDateTime fromTime,ZonedDateTime toTime) {
		// validate the fromTime is before the toTime
		LOGGER.debug ("Before filtering by fromTime: " + fromTime + " toTime: " + toTime + " itemCount: " + items.size ());
		LOGGER.debug ("Items: " + items);
		if (toTime == null && fromTime == null) {
			LOGGER.info ("No lookback window specified. No time filter applied");
			return items;
		}

		items = (ArrayList<Item>) items.stream()
				.filter(i -> (i.getLastModifiedTime().isAfter(fromTime) && i.getLastModifiedTime().isBefore(toTime))
						|| i.getLastModifiedTime().isEqual(fromTime) || i.getLastModifiedTime ().isEqual (toTime))
				.collect(Collectors.toCollection (ArrayList::new));

		LOGGER.debug("After filtering by fromTime: " + fromTime + " toTime: " + toTime + " itemCount: " + items.size());

		return items;
	}

	// Filter Items by facility
	public Collection<Item> filterItemsByFacility(Collection<Item> items, String facility) {

		LOGGER.debug("Before filtering by facility: " + facility + " itemCount: " + items.size());
		if (facility == null || facility.trim().length () == 0) {
			LOGGER.info ("No facility specified.  No facility filter applied.");
			return items;
		}

		items = items
				.stream()
				.filter(i -> facility.equalsIgnoreCase(i.getFacility ()))
			    .collect(Collectors.toCollection (ArrayList::new));
		LOGGER.debug("After filtering by facility: " + facility + " itemCount: " + items.size());
		return items;
	}

	public Collection<Item> filterItemsByZones(Collection<Item> items, String zones) {
		// Filter Items by zones
		LOGGER.debug("Before filtering by zones: " + zones + " itemCount: " + items.size());
		if (zones == null || zones.trim().length () == 0) {
			LOGGER.info ("No zones specified.  No zones filter applied.");
			return items;
		}

		ArrayList <String> zoneList = StringHelper.parseCommaDelimitedStringToList (zones);
		ArrayList filteredItems = new ArrayList <Item> ();
		for (String zone : zoneList ) {

			ArrayList itemsPerFilter = new ArrayList <Item> ();
			itemsPerFilter = items
					.stream ()
					.filter (item -> zone.equalsIgnoreCase(item.getZone ()))
					.collect(Collectors.toCollection (ArrayList::new));

			LOGGER.debug ("Item count for zone: " + zone + " is : " + itemsPerFilter.size ());
			filteredItems.addAll ( itemsPerFilter);
		}
		// TODO - filter
		LOGGER.debug("After filtering by zones: " + zones + " itemCount: " + items.size());

		return filteredItems;
	}



}
