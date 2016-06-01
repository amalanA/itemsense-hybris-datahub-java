# ItemSense IoT Connector for SAP Hybris Commerce
=================

The ItemSense IoT Connector for SAP Hybris Commerce updates available inventory for locations configured in Hybris Commerce using near-real time counts of RAIN RFID tagged items reported by the Impinj platform. _Prior to implementing, the SI and Retailer should consider other processes and systems that can add accuracy to the available inventory.  An example of this would be the POS identifying items sold in the same window data is being provided, or if the POS happened to use RFID, the extension could be further extended to exclude EPCs of sold items._

This project contains the code and instructions for deploying a stand-alone Data Hub instance that queries ItemSense for all items in a "warehouse" and publishes that the item count by product code (SKU) to Hybris Commerce as the available inventory for those items in that warehouse.

## Assumptions
- All areas of a warehouse/store that contain inventory are monitored by the Impinj Platform.  (ItemSense + fixed infrastructure RAIN readers (typically xArrays) 
- All inventory to be monitored has RAIN tags.
- Master Data file is provided to map valid EAN codes to Product Codes.  A sample master data file based on the UK Apparel store (provisioned by the B2C_ACC recipe) is provided under datahub/impinj/impinj-canonical/src/main/resources.  Note some of the EAN values in the demo store are not valid.
- RAIN tagged item EPCs are SGTIN-96 encoded.  The EAN/UPC is decoded from the EPC and then mapped to the Product code via the master data file



## Definitions
- Electronic Product Code (EPC) - Syntax for unique identifiers assigned to physical objects, unit loads, locations, or other identifiable entity playing a role in business operations.  GS1's EPC Tag Data Standard (TDS) specifies the data format of the EPC, and provides encodings for numbering schemes -- incuding the GS1 Keys -- within an EPC.  See [GS1 EPC/RFID Standards](http://www.gs1.org/epc-rfid).
- European Article Number (EAN) - World-wide 13 digit barcoding standard bar code numbering scheme used internationally to identify consumer products.  A superset of UPC, and governed by GS1.
- RAIN RFID - Wireless technology that connects billions of everyday items to the internet.  RAIN techonology is based on the [UHF Gen2 standard](http://www.gs1.org/epcrfid/epc-rfid-uhf-air-interface-protocol/2-0-1).  See <http://rainrfid.org>. 
- RAIN RFID Alliance - Global organization promoting the universal adoption of RAIN technology. See <http://rainrfid.org>. 
- SGTIN-96 - 
- warehouse - A site/location/store configured in Hybris Commerce with inventory available to the web store.  

