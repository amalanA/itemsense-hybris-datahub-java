package com.impinj.datahub.constants;

/**
 * The Class ImpinjDatahubConstants.
 */
public class ImpinjDatahubConstants
{
	/**
	 * Used for configuration.
	 */
	public final static String PROPERTIES_FILENAME = "config.properties";
	public final static String CONFIG_JOBS = "jobs";
	public final static String CONFIG_CRON_EXPRESSION = "cronexpression";
	public final static String CONFIG_USERNAME = "username";
	public final static String CONFIG_PASSWORD = "password";
	public final static String CONFIG_WAREHOUSE = "warehouse";
	public final static String CONFIG_ENDPOINT_URL = "endpointurl";
	public final static String CONFIG_MASTER_DATA_FILE = "hybrismasterdatapath";
	public final static String CONFIG_LOOKBACK_WINDOW_IN_SECONDS = "itemlookbackwindowinseconds";
	public final static String CONFIG_EPC_PREFIX = "epcprefix";
	/**
	 * Datahub constants.
	 */
	public static final String DATAHUB_FEED = "DEFAULT_FEED";
	public static final String DATAHUB_POOL = "GLOBAL";
	public static final String DATAHUB_IMPINJ_RAW_ITEM_TYPE = "RawImpinjStockItem";
	public static final String DATAHUB_EXTENSION_SOURCE = "impinj extension source";
	public static final String DATAHUB_TARGET_SYSTEM = "HybrisApparelCoreInstallation";

	/**
	 * These values must match with RawImpinjStockItem types.
	 */
	public static final String RAW_PRODUCT_CODE = "productId";
	public static final String RAW_STOCK_LEVEL = "stockLevel";
	public static final String RAW_WAREHOUSE_ID = "warehouseId";
	public static final String RAW_FRAGMENT_DATA_INPUT_CHANNEL = "rawFragmentDataInputChannel";

	/**
	 * Impinj constants.
	 */
	public static final String EPC_FORMAT = "PURE_ID";
	/**
	 * Instantiates a new Impinj DataHub constants.
	 */
	private ImpinjDatahubConstants()
	{
		// not called
	}

}
