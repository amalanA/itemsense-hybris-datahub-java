package com.impinj.datahub.itemsense;

import com.impinj.datahub.util.TimeHelper;
import com.impinj.itemsense.client.data.item.Item;
import com.impinj.datahub.constants.ImpinjDatahubConstants;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ItemSenseQueryHelperTest
{
	private ArrayList<Item> items;
	private ZonedDateTime testTime = TimeHelper.getNowUTC ();


	@Before
	public void setUp() {

		items = new ArrayList<> ();

		Item item1 = new Item ();
		item1.setEpc ("30140005FC13EB8000000001");
		item1.setZone ("Zone1");
		item1.setFacility ("MyTestFacility");
		item1.setLastModifiedTime (testTime.minusSeconds(60));

		items.add(item1);

		Item item2 = new Item ();
		item2.setEpc ("30140005FC13EB8000000002");
		item2.setZone ("Zone1");
		item2.setFacility ("MyTestFacility");
		item2.setLastModifiedTime (testTime.minusSeconds(119));

		items.add(item2);

		Item item3 = new Item ();
		item3.setEpc ("30140008782B0AC000000001");
		item3.setZone ("Zone1");
		item3.setFacility ("MyTestFacility");
		item3.setLastModifiedTime (testTime.minusSeconds(125));

		items.add(item3);

		Item item4 = new Item ();
		item4.setEpc ("30140009E055124000000001");
		item4.setZone ("Zone1");
		item4.setFacility ("MyTestFacility");
		item4.setLastModifiedTime (testTime.minusSeconds(60));

		items.add(item4);

		Item item5 = new Item ();
		item5.setEpc ("3014000A4413114000000001");
		item5.setZone ("Zone1");
		item5.setFacility ("MyTestFacility");
		item5.setLastModifiedTime (testTime.minusSeconds(60));

		items.add(item5);

		Item item6 = new Item ();
		item6.setEpc ("3014000D10180B4000000001");
		item6.setZone ("Zone1");
		item6.setFacility ("MyTestFacility");
		item6.setLastModifiedTime (testTime.minusSeconds(119));

		items.add(item6);

		Item item7 = new Item ();
		item7.setEpc ("3014000D10180B4000000002");
		item7.setZone ("Zone1");
		item7.setFacility ("MyTestFacility");
		item7.setLastModifiedTime (testTime.minusSeconds(125));

		items.add(item7);

		Item item8 = new Item ();
		item8.setEpc ("3014000DD81B670000000001");
		item8.setZone ("Zone1");
		item8.setFacility ("MyTestFacility");
		item8.setLastModifiedTime (testTime.minusSeconds(60));

		items.add(item8);

		Item item9 = new Item ();
		item9.setEpc ("3014000DF434E9C000000001");
		item9.setZone ("Zone2");
		item9.setFacility ("MyTestFacility");
		item9.setLastModifiedTime (testTime.minusSeconds(119));

		items.add(item9);

		Item item10 = new Item ();
		item10.setEpc ("3014000F7432348000000001");
		item10.setZone ("Zone2");
		item10.setFacility ("MyTestFacility");
		item10.setLastModifiedTime (testTime.minusSeconds(125));

		items.add(item10);

		Item item11 = new Item ();
		item11.setEpc ("3014000FEC298DC000000001");
		item11.setZone ("Zone2");
		item11.setFacility ("MyTestFacility");
		item11.setLastModifiedTime (testTime.minusSeconds(600));

		items.add(item11);

		Item item12 = new Item ();
		item12.setEpc ("3014000FEC298DC000000002");
		item12.setZone ("Zone2");
		item12.setFacility ("MyTestFacility");
		item12.setLastModifiedTime (testTime.minusSeconds(60));

		items.add(item12);

		Item item13 = new Item ();
		item13.setEpc ("3014000FEC298DC000000003");
		item13.setZone ("Zone2");
		item13.setFacility ("MyTestFacility");
		item13.setLastModifiedTime (testTime.minusSeconds(60));

		items.add(item13);

		Item item14 = new Item ();
		item14.setEpc ("3014000FEC298DC000000005");
		item14.setZone ("Zone2");
		item14.setFacility ("DEFAULT");
		item14.setLastModifiedTime (testTime.minusSeconds(119));

		items.add(item14);

		Item item15 = new Item ();
		item15.setEpc ("3015000FEC298DC000000006");
		item15.setZone ("Zone2");
		item15.setFacility ("MyTestFacility");
		item15.setLastModifiedTime (testTime.minusSeconds(125));

		items.add(item15);

		Item item16 = new Item ();
		item16.setEpc ("3015001024045AC000000001");
		item16.setZone ("Zone2");
		item16.setFacility ("MyTestFacility");
		item16.setLastModifiedTime (testTime.minusSeconds(600));

		items.add(item16);

		Item item17 = new Item ();
		item17.setEpc ("3015001024045AC000000002");
		item17.setZone ("Zone2");
		item17.setFacility ("DEFAULT");
		item17.setLastModifiedTime (testTime.minusSeconds(125));

		items.add(item17);

	}


	@Test
	public void filterItemsByFacilityTest()
	{
		ItemSenseQueryHelper isQueryHelper = new ItemSenseQueryHelper ();
		ArrayList<Item>  filteredItems = isQueryHelper.filterItemsByFacility(items, null);
		assertTrue ("Null Facility test", items.size () == filteredItems.size ());

		filteredItems = isQueryHelper.filterItemsByFacility (items, "DEFAULT");
		assertTrue ("DEFAULT Facility test", filteredItems.size () == 2);

		filteredItems = isQueryHelper.filterItemsByFacility (items, "default");
		assertTrue ("default Facility test", filteredItems.size () == 2);

		filteredItems = isQueryHelper.filterItemsByFacility (items, "notaafacility");
		assertTrue ("notafacility Facility test", filteredItems.size () == 0);
	}

	@Test
	public void filterItemsByPrefixesTest()
	{
		ItemSenseQueryHelper isQueryHelper = new ItemSenseQueryHelper ();
		ArrayList<Item>  filteredItems = isQueryHelper.filterItemsByPrefixes(items, null);
		assertTrue ("Null Prefix test", items.size () == filteredItems.size ());

		filteredItems = isQueryHelper.filterItemsByPrefixes (items, "3014");
		assertTrue ("3014 Prefix test", filteredItems.size () == 14);

		filteredItems = isQueryHelper.filterItemsByPrefixes (items, "3014000D,3015");
		assertTrue ("3014 Prefix test", filteredItems.size () == 7);

		filteredItems = isQueryHelper.filterItemsByPrefixes (items, "999, 3014000D,3015");
		assertTrue ("3014 Prefix test", filteredItems.size () == 7);
	}


	@Test
	public void filterItemsByTimeTest()
	{
		ItemSenseQueryHelper isQueryHelper = new ItemSenseQueryHelper ();
		ArrayList<Item>  filteredItems = isQueryHelper.filterItemsByTime(items, null, null);
		assertTrue ("Null Time test", items.size () == filteredItems.size ());

		filteredItems = isQueryHelper.filterItemsByTime (items, testTime.minusSeconds (30), testTime);
		assertTrue ("30 seconds before testTime Time test", filteredItems.size () == 0);

		filteredItems = isQueryHelper.filterItemsByTime (items, testTime.minusSeconds (120), testTime);
		assertTrue ("120 seconds before testTime Time test", filteredItems.size () == 10);

		filteredItems = isQueryHelper.filterItemsByTime (items, testTime.minusSeconds (500), testTime.minusSeconds (60));
		assertTrue ("minus 500-60", filteredItems.size () == 15);

		filteredItems = isQueryHelper.filterItemsByTime (items, testTime.minusSeconds (500), testTime.minusSeconds (61));
		assertTrue ("minus 500-61", filteredItems.size () == 9);

	}

	@Test
	public void filterItemsByZonesTest()
	{
		ItemSenseQueryHelper isQueryHelper = new ItemSenseQueryHelper ();
		ArrayList<Item>  filteredItems = isQueryHelper.filterItemsByZones(items, null);
		assertTrue ("Null Zones test", items.size () == filteredItems.size ());

		filteredItems = isQueryHelper.filterItemsByZones (items, "Zone1");
		assertTrue ("Zone1 Zones test", filteredItems.size () == 8);

		filteredItems = isQueryHelper.filterItemsByZones (items, "zONE1");
		assertTrue ("zONE1 Zones test", filteredItems.size () == 8);

		filteredItems = isQueryHelper.filterItemsByZones (items, "Zone1,Zone2");
		assertTrue ("Zone1,Zone2 Zones test", filteredItems.size () == 17);

		filteredItems = isQueryHelper.filterItemsByZones (items, "999");
		assertTrue ("999 Zones test", filteredItems.size () == 0);
	}

}

