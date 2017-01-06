/**
 * IMPINJ CONFIDENTIAL AND PROPRIETARY
 *
 * This source code is the sole property of Impinj, Inc. Reproduction or utilization of this source
 * code in whole or in part is forbidden without the prior written consent of Impinj, Inc.
 *
 * (c) Copyright Impinj, Inc. 2015. All rights reserved.
 */

package com.impinj.itemsense.client.data;

import java.net.URI;
import java.util.*;


import com.google.gson.Gson;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import javax.ws.rs.core.MediaType;

import javax.ws.rs.core.MultivaluedMap;

import com.impinj.itemsense.client.data.item.Item;

/**
 * Helpers for working with the Item Rest service. Works directly with wadl2java generated client
 * code.
 */
@Log4j
public class ItemApiLib {

  private final Gson gson;
  public static final int MAXIMUM_PAGE_SIZE = 1000;
  private final WebResource target;

  /**
   * Constructor
   *
   * @param gson Gson instance
   * @param client Jersey client
   * @param uri the base URI for the items service
   */
  public ItemApiLib(final Gson gson, final Client client, final URI uri) {
    this.gson = gson;
    target = client.resource(uri);
  }

  /**
   * Gets every item from the Query API.
   *
   * @return A collection of Item objects.
   */
  public Collection<Item> showAllItems(final String epcFormat) {
    // Iterate through all pages of results, building a list of items
    final Collection<Item> itemsList = new ArrayList<>();
    String pageMarker = null;
    do {

      System.out.println( "ItemApiLib.showAllItems: epcFormat: " + epcFormat + " pageMarker: " + pageMarker);
      final Map result =
          sendShowItemsRequest(null, null, null, epcFormat, pageMarker, MAXIMUM_PAGE_SIZE);
    System.out.println("Map size: " + result.size());
    System.out.println("Map keyset: " + result.keySet());
    System.out.println("Map Result: " + result.toString());
      getItemsFromResultSet(result, itemsList);
      pageMarker =
          result.containsKey("nextPageMarker") ? (String) result.get("nextPageMarker") : null;
    } while (pageMarker != null);

    return itemsList;
  }

  /**
   * Returns a list of items based on the specified query parameters
   *
   * @param epcPrefix A hexadecimal string representing an EPC prefix of an item. Only the items
   *        with EPCs that start with this prefix will be returned.
   * @param zoneNames A comma-separated list of zone IDs. Only items in these zones will be
   *        returned.
   * @param confidence A confidence level (HIGH / LOW) Only items with this confidence level will be
   *        returned.
   * @param pageMarker A string indicating which page of results to return. A new marker is returned
   *        after each query and can be used to fetch subsequent pages.When using a page marker, the
   *        other query parameters must be the same as those used in the previous query.
   * @param maximumPageSize The maximum number of records to return per query.
   * @return A collection of Item objects.
   */
  public Collection<Item> showItems(final String epcPrefix, final String zoneNames,
      final String confidence, final String epcFormat, final String pageMarker,
      @NonNull final Integer maximumPageSize) {
    final Map result = sendShowItemsRequest(epcPrefix, zoneNames, confidence, epcFormat, pageMarker,
        maximumPageSize);
    final Collection<Item> itemsList = new ArrayList<>();
    getItemsFromResultSet(result, itemsList);
    return itemsList;
  }

  private Map sendShowItemsRequest(final String epcPrefix, final String zoneNames,
      final String confidence, final String epcFormat, final String pageMarker,
      @NonNull final Integer maximumPageSize) {
    log.debug("Sending /data/v1/items/show request, epcPrefix=" + epcPrefix + ", zoneNames=" + zoneNames
        + ", confidence=" + confidence + ", epcFormat=" + epcFormat + ", pageMarker=" + pageMarker
        + ", maximumPageSize=" + maximumPageSize);

    MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    if( epcPrefix != null ){
      map.put("epcPrefix", Arrays.asList(epcPrefix));
    }
    if( zoneNames != null ){
      map.put("zoneNames", Arrays.asList(zoneNames));
    }
    if( confidence != null ){
      map.put("confidence", Arrays.asList(confidence));
    }
    if( epcFormat != null ){
      map.put("epcFormat", Arrays.asList(epcFormat));
    }
    if( pageMarker != null ){
      map.put("pageMarker", Arrays.asList(pageMarker));
    }
    if(maximumPageSize != null){
      map.put("maximumPageSize", Arrays.asList(maximumPageSize.toString()));
    }


    final String response = target.path("/data/v1/items/show").queryParams(map).accept(MediaType.APPLICATION_JSON_TYPE)
        .get(String.class);
    log.trace("/data/v1/items/show response: " + response);
System.out.println("/data/v1/items/show response: " + response);
    return gson.fromJson(response, Map.class);
  }

  /**
   * Extracts the items from the result set and builds a list of Item objects. This method expects
   * the result set to key called "items" with a list of maps (representing each individual item).
   *
   * @param result The result set from an API query
   * @param itemsList The list to add the items to
   */
  private void getItemsFromResultSet(final Map result, final Collection<Item> itemsList) {
    if (!result.containsKey("items")) {
      throw new RuntimeException("Malformed result set: \"items\" key is missing");
    }
    final Object items = result.get("items");
    if (!(items instanceof List)) {
      throw new RuntimeException("Malformed result set: \"items\" is not a list");
    }
    for (final Object item : (List) items) {
      if (!(item instanceof Map)) {
        throw new RuntimeException("Malformed result set: item is not a map");
      }
      itemsList.add(Item.fromMap((Map) item));
    }
  }
}

