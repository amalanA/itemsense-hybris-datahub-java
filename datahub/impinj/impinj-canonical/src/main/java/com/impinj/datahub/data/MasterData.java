package com.impinj.datahub.data;


import com.impinj.datahub.util.Sgtin96;
import com.impinj.datahub.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


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
	 *     - empty lines (0 length or only whitespace) will be skipped
	 *     - lines beginning with #
	 *     - no explicit header line
	 *     - each lines is the productCode | EAN.  The EAN will be left padded to match the 13 digit standardo
     *     - one product per line (the first 2 pipe separated values will be used)
	 *       which also accounts for UPC which are 12 digits with a leftmost 0 added.  For example:
			 300046592 | 1019425517
			 10176 | 1234567910
			 111114_black | 1234567911
 	 *
 	 * @param masterDataFile the path to the masterdata file on the local filesystem
 	 */
	public static HashMap<String, String> loadProductMasterData(String masterDataFile) {
	
		HashMap<String, String> productMasterData = new HashMap<>();
	
		if (masterDataFile != null )
		{
			InputStream is = null;
			try // try reading from file system
			{
				is = new FileInputStream (new File (masterDataFile));
			} catch (IOException e ) {
				LOGGER.error ("MasterDataFile exception reading from filesystem.  MasterDataFile: "
					+ masterDataFile, e);
				return productMasterData;
			}
			BufferedReader br = null;
			try {
				br = new BufferedReader (new InputStreamReader (is));
				int lineNumber = 0;
				for (String line; (line = br.readLine ()) != null; ) {
					line = line.trim ();
					lineNumber += 1;
					if (line.length () > 0 && !line.startsWith ("#")) {
						ArrayList<String> values = StringHelper.parseDelimitedStringToList (line, MASTERDATAFILE_DATA_DELIMITER);
						if (values.size () >= 2) {
							Iterator it = values.iterator ();
							String prodCode = (String) it.next ();
							String ean = (String) it.next ();
							String formattedEan = Sgtin96.padLeft (ean, 13, '0');
							productMasterData.put (formattedEan, prodCode.trim ());
						} else {
							LOGGER.debug ("Error parsing master data file (" + masterDataFile + ") @ line " + lineNumber + ":" + line);
						}
					}
				}
			} catch (final IOException e) {
				LOGGER.error ("Error reading MasterData file: " + masterDataFile + ": " + e, e);
				return productMasterData;
			} finally {
			}
			try {
				br.close ();
			} catch (IOException ioe) {
				LOGGER.error ("Master Data File not specified in job configuration.  No items can be processed.");
			} finally {
			}
			try {
				is.close ();
			} catch (IOException ioe) {
				LOGGER.error ("Master Data File not specified in job configuration.  No items can be processed.");
			} finally {
			}
		} else {
			LOGGER.error ("Master Data File not specified in job configuration.  No items can be processed.");
		}
		return productMasterData;
	}
}
