package com.impinj.datahub.data;

import com.impinj.datahub.itemsense.Sgtin96;
import com.impinj.itemsense.client.Item;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProductStockLevel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductStockLevel.class);

    private final String ean;

    private final int quantity;

    /**
     * ProductStockLevel is returns the number of a given product (defined by EAN) which is loaded into data hub

     * @param ean          EAN13
     * @param quantity     Number of items found for this EAN/ProductCode
     */

    public ProductStockLevel(String ean, int quantity) {
        this.ean = ean;
        this.quantity = quantity;
    }

    public String getEan() {
        return this.ean;
    }

    public int getQuantity() {
        return this.quantity;
    }


    /**
     *
     * generate a list of productStock levels based on converting the EPC returned from ItemSense 
     * following the SGTIN-96 standard.
     * WARNING: If the EAN encoded into the EPC is not valid, the decoded value will not match
     *
     * @param items items read by itemsense client
     */
    public static List<ProductStockLevel> getItemSenseProductStockLevels(Collection<Item> items) {
        List<Sgtin96> productInfos = items.stream().map(i -> Sgtin96.FromSgtin96Epc(i.getEpc())).filter(sgtin96 -> sgtin96 != null).collect(Collectors.toList());

		LOGGER.debug("getItemSenseProductStockLevels-- items size: " + items.size());
		LOGGER.debug("getItemSenseProductStockLevels-- items: " + items);
        return productInfos.stream().collect(Collectors.groupingBy(p -> p.toEAN(), Collectors.counting()))
                .entrySet().stream().map((m) -> new ProductStockLevel(m.getKey(), m.getValue().intValue()))
                .collect((Collectors.toList()));

    }

    /**
     * return the current level of stock by product code for all products defined in the masterdata file
     * each product is initialized to a stock level of 0 then updated if ItemSense reported a quantity
     *
     * @param masterDataFile  path to masterdata file
     * @param currentLevels   stock levels from ItemSense by productStockLevel (EAN)
     */
    public static HashMap<String, Integer> getMasterDataStockLevels(String masterDataFile, List<ProductStockLevel> currentLevels ) {
        HashMap<String, Integer> masterDataStockLevels = new HashMap<String, Integer>();
        HashMap<String, String> masterDataInfo = MasterData.loadProductMasterData(masterDataFile);

        masterDataInfo.values().stream().forEach(productCode -> masterDataStockLevels.put(productCode, 0));
	LOGGER.info("Number of Master Data product codes: " + masterDataInfo.size());

        currentLevels.stream().filter(p -> masterDataInfo.containsKey(p.getEan())).forEach(p -> masterDataStockLevels.put(masterDataInfo.get(p.getEan()), p.getQuantity()));

		LOGGER.debug("--- currentStockLevels size: " + currentLevels.size());
		LOGGER.debug("--- currentStockLevels: " + currentLevels);
		LOGGER.debug("--- applied MasterDataStockLevels size: " + masterDataStockLevels.size());
		LOGGER.debug("--- applied MasterDataStockLevels: " + masterDataStockLevels);
        return masterDataStockLevels;

    }

    /** 
     * gets stock levels with no filters
     *
     * @param masterDataFile path to master data file
     * @param items          items returned by ItemSense client
     */
    public static HashMap<String, Integer> getMasterDataStockLevelsFromItems(String masterDataFile, Collection<Item> items) {
        return getMasterDataStockLevelsFromItems(masterDataFile, items, null, null);
    }

    /** 
     * gets stock levels with filters
     *
     * @param masterDataFile path to master data file
     * @param items          items returned by ItemSense client
     * @param epcPrefix      filter by EPC prefix (configured in data hub job)
     * @param lookbackWindowInSeconds      filter by last updated time on items (configured in data hub job).  Filters out
     *                       items not updated in itemsense in the number of seconds.  must be greater than the 
     *                       frequency in ItemSense
     */

    public static HashMap<String, Integer> getMasterDataStockLevelsFromItems(String masterDataFile, Collection<Item> items, String epcPrefix, Integer lookbackWindowInSeconds ) {

        LOGGER.debug ("getMasterDataStockLevelsFromItems:  UNFILTERED ItemSense item Count : " + items.size());
        LOGGER.debug ("UNFILTERED ItemSense items: " + items);
        if(epcPrefix != null && !epcPrefix.isEmpty()){
            LOGGER.debug ("+++ filtering for epcPrefix: " + epcPrefix);
            LOGGER.debug ("+++ pre epcPrefix filtered item count : " + items.size());
            items = items.stream().filter(i-> i.getEpc().startsWith(epcPrefix)).collect(Collectors.toList());
            LOGGER.debug ("--- filtered item count : " + items.size());
        } else {
            LOGGER.debug ("--- epcPrefix not set: " + epcPrefix);
        }

        if(lookbackWindowInSeconds != null && lookbackWindowInSeconds.intValue() > 0){
            LOGGER.debug("+++ filtering for lookbackwindowinseconds: " + lookbackWindowInSeconds);
            LOGGER.debug ("+++ pre lookback seconds filtered  item count : " + items.size());
            items = items.stream().filter(i-> ChronoUnit.SECONDS.between(ZonedDateTime.parse(i.getLastModifiedTime()), ZonedDateTime.now()) < lookbackWindowInSeconds.longValue()).collect(Collectors.toList());
            LOGGER.debug ("--- filtered item count : " + items.size());
        } else {
            LOGGER.debug ("--- lookbackwindowinseconds not set: " + lookbackWindowInSeconds);
        }


        LOGGER.debug("++++++++++++++++ DEMO ONLY!!!   filtering out zone:  ABSENT");
        LOGGER.debug ("+++ pre ABSENT ZONE filtered item count : " + items.size());
	

	ArrayList<Item> filteredItems = new ArrayList<Item> ();
        items.forEach(item-> { 
            System.out.println("Item: epc: " + item.getEpc() + " Zone: " + item.getZone() ); 
            if (item.getZone().equals("ABSENT")) {
            	 System.out.println("ABSENT Item: epc: " + item.getEpc() + " Zone: " + item.getZone() ); 
            } else {
                System.out.println("KEEPER Item: epc: " + item.getEpc() + " Zone: " + item.getZone() ); 
                filteredItems.add(item);
            }
        } );

   
        //items = items.stream().filter(i-> (!"ABSENT".equals(i.getZone()))).collect(Collectors.toList());
        

        LOGGER.debug ("--- filtered item count : " + filteredItems.size());


        return getMasterDataStockLevels(masterDataFile, getItemSenseProductStockLevels(filteredItems));
    }

    @Override
    public String toString() {
        return "ProductStockLevel{" + "EAN=" + ean + ", quantity=" + quantity + '}';
    }
}
