package com.impinj.datahub.data;


import com.impinj.datahub.itemsense.Sgtin96;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;


/**
 *
 * Class to read master data file into memory
 *
 */
public class MasterData {

	private static final Logger LOGGER = LoggerFactory.getLogger(MasterData.class);
	private static final String MASTERDATAFILE_DATA_DELIMITER = "|";

	/**
 	 * Returns a map of all the products found in the product master data file specified.
 	 * The format of the product master data file is as follows:
	 *     - no header line
    	 *     - one product per line
         *     - each lines is the productCode | EAN.  The EAN will be left padded to match the 13 digit standardo 
	 *       which also accounts for UPC which are 12 digits with a leftmost 0 added.  For example:
			 300046592 | 1019425517
			 10176 | 1234567910
			 111114_black | 1234567911
 	 *
 	 * @param masterDataFile the path to the masterdata file on the local filesystem
 	 */
	public static HashMap<String, String> loadProductMasterData(String masterDataFile) {
	
		HashMap<String, String> productMasterData = new HashMap<>();
	
		if (masterDataFile != null) 
		{
			BufferedReader br = null;
			try 
			{  
				LOGGER.debug("Reading MasterData file: " + masterDataFile);
				br = new BufferedReader(new FileReader(masterDataFile));

				for (String line; (line = br.readLine()) != null; ) 
				{
					if (line.length() > 0) 
					{
						StringTokenizer st = new StringTokenizer(line, MASTERDATAFILE_DATA_DELIMITER);
						String prodCode = st.nextToken();
						String ean = st.nextToken().trim();
						String formattedEan = Sgtin96.padLeft(ean, 13, '0');
						productMasterData.put(formattedEan, prodCode.trim());
					}
				}
	
				br.close();
			} catch (final IOException e) {
				
				LOGGER.error("Error reading MasterData file: " + masterDataFile + e);
			}
			finally { }
		}
	
		return productMasterData;
	}
}
