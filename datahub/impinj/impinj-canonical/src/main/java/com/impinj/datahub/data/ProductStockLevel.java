package com.impinj.datahub.data;

import com.impinj.datahub.util.Sgtin96;
import com.impinj.itemsense.client.data.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        List<Sgtin96> productSgtins = items
                .stream()
                .map(i -> Sgtin96.FromSgtin96Epc(i.getEpc()))
                .filter(sgtin96 -> sgtin96 != null)
                .collect(Collectors.toList());

		LOGGER.debug("getItemSenseProductStockLevels-- items size: " + items.size());
		LOGGER.debug("getItemSenseProductStockLevels-- items: " + items);
        List<ProductStockLevel> productStockLevels = productSgtins
                .stream()
                .collect(Collectors.groupingBy(p -> p.toEAN(), Collectors.counting()))
                .entrySet()
                .stream()
                .map((m) -> new ProductStockLevel(m.getKey(), m.getValue().intValue()))
                .collect((Collectors.toList()));
        return productStockLevels;

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

        // load 0 quantity for each product
        masterDataInfo.values().stream().forEach(productCode -> masterDataStockLevels.put(productCode, 0));
	LOGGER.info("Number of Master Data product codes: " + masterDataInfo.size());

        currentLevels
                .stream()
                .filter(p -> masterDataInfo.containsKey(p.getEan()))
                .forEach(p -> masterDataStockLevels.put(masterDataInfo.get(p.getEan()), p.getQuantity()));

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
	    return getMasterDataStockLevels(masterDataFile, getItemSenseProductStockLevels(items));
    }

    @Override
    public String toString() {
        return "ProductStockLevel{" + "EAN=" + ean + ", quantity=" + quantity + '}';
    }
}
