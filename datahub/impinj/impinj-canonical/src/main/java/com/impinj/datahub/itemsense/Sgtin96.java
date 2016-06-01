package com.impinj.datahub.itemsense;


import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * utility class following SGTIN-96 to encode/decode
 */
public class Sgtin96 {
    private final static String uriPrefix = "urn:epc:tag:sgtin-96:";
    private final static int header = 48; // 8-bits
    private final static int filterValueMin = 0;
    private final static int filterValueMax = 7;
    private int  filterValue = 1;

    private final static int partitionValueMin = 0;
    private final static int partitionValueMax = 6;

    // Company prefix value
    private BigInteger companyPrefix; // 20-40 bits; default 24-bits, per partition
    private int companyPrefixLengthInBits = 24;
    private int companyPrefixLengthInDigits = 7;

    // Item reference value
    private BigInteger itemReference; // 24-4 bits; default 20-bits, per partition
    private int itemReferenceLengthInBits = 20;
    private int itemReferenceLengthInDigits = 6;

    // Item UPC
    private String upc;
    private int upcCheckDigit;

    // Serial number value
    private BigInteger serialNumber ; // 38 bits
    private final static int serialNumberMaxBits = 38;

    private final static int CONVERTHEX = 16;
    private final static int CONVERTDECIMAL = 10;
    private final static int CONVERTBINARY = 2;





    // company prefix and item reference fields.
    //
    // Partition value | Company Prefix Bits | Item Reference Bits
    // -----------------------------------------------------------
    //      0          |        40           |          4
    //      1          |        37           |          7
    //      2          |        34           |          10
    //      3          |        30           |          14
    //      4          |        27           |          17
    //      5          |        24           |          20
    //      6          |        20           |          24
    // -----------------------------------------------------------
    //
    
    private int partition = 5; // 3 bits


    public int getHeader() {
        return header;
    }

    public int getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(int filterValue) {
        this.filterValue = filterValue;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public BigInteger getCompanyPrefix() {
        return companyPrefix;
    }

    public void setCompanyPrefix(BigInteger companyPrefix) {
        this.companyPrefix = companyPrefix;
    }

    public BigInteger getItemReference() {
        return itemReference;
    }

    public void setItemReference(BigInteger itemReference) {
        this.itemReference = itemReference;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(BigInteger serialNumber) {
        this.serialNumber = serialNumber;
    }

    public static Sgtin96 FromString(String Sgtin96AsString)
    {
        Sgtin96 sgtin96 = null;

        if (Sgtin96AsString == null && Sgtin96AsString.isEmpty())
        {
            throw new RuntimeException("Null or Empty SGTIN-96 String.");
        }

        if (true == IsValidUri(Sgtin96AsString))
        {
            // It is a URI String, so populate the object data members
            // accordingly
            try
            {
                sgtin96 = FromSgtin96Uri(Sgtin96AsString);
            }
            catch (Exception ex)
            {
                throw ex;
            }
        }
        else if (true == IsValidEpc(Sgtin96AsString))
        {
            // It is an EPC String, so populate the object data members
            // accordingly
            try
            {
                sgtin96 = FromSgtin96Epc(Sgtin96AsString);
            }
            catch (Exception ex)
            {
                throw ex;
            }
        }
        else
        {
            throw new RuntimeException("Invalid SGTIN-96 String");
        }

        return sgtin96;
    }

    /**
     * Convert Uri toSGTIN96
     *
     * @param uri  valid URI per GS1 tag standards
     */

    public static Sgtin96 FromSgtin96Uri(String Uri)
    {
        Sgtin96 sgtin96 = new Sgtin96();
        String LocalUri = Uri;
        String[] DataValues;

        if (IsValidUri(Uri))
        {
            // Strip out the fixed URI prefix String so that we can look at the data
            String DataFields = LocalUri.replace(uriPrefix, "");
            // Now, extract each of the remaining fields as a separate String
            DataValues = DataFields.split(".");

            // Extract the Filter value
            sgtin96.filterValue = Integer.parseInt(DataValues[0]);
            // Extract the Company Prefix value
            sgtin96.companyPrefix =  new BigInteger(DataValues[1]);
            // Extract the Item Reference Value
            sgtin96.itemReference = new BigInteger(DataValues[2]);
            // Calculate the Partition value from the number of
            // digits in the Item Reference field
            sgtin96.partition = (int)(DataValues[2].length() - 1);
            // Populate the Digit and Bit length fields for the
            // Company Prefix and Item Reference values
            SetLengthsFromPartition( sgtin96);
            // Extract the serial number
            sgtin96.serialNumber =new BigInteger(DataValues[3]);
            // Add company prefix digits, less the first '0' character,
            // which is added in the UPC to GTIN conversion
            String companyPrefixFormat =  "%" + (sgtin96.companyPrefixLengthInDigits - 1) + "d";
            sgtin96.upc = String.format(companyPrefixFormat, sgtin96.companyPrefix);
            // If the number of companyPrefix digits is less than 12, then
            // Add item reference digits, less the first '0' character,
            // which is added in the UPC to GTIN conversion
            if (12 > sgtin96.companyPrefixLengthInDigits)
            {
                String itemPrefixFormat = "%" + (sgtin96.itemReferenceLengthInDigits - 1) + "d";
                sgtin96.upc += String.format(itemPrefixFormat, sgtin96.itemReference);

            }

            sgtin96.upcCheckDigit = CalculateUpcCheckDigit(sgtin96.upc);
        }
        else
        {
            throw new RuntimeException("Invalid SGTIN-96 URI");
        }

        return sgtin96;
    }

    /**
     * Populate the current object with the data contained within the furnished Epc String
     *
     * @param Epc 	           A SGTIN-96 EPC String, e.g. 30340789000C0E42DFDC1C35
     * @exception RuntimeException Thrown when an EPC that is not 96-bits long is provided.
     */
    public static Sgtin96 FromSgtin96Epc(String Epc)
    {
        Sgtin96 ReturnSgtin96 = new Sgtin96();
        String EpcToValidate = "";

        if (Epc != null && ! Epc.isEmpty())
        {
            EpcToValidate = Epc.replace(" ", "");
        }
        else
        {
            throw new RuntimeException("null SGTIN-96 EPC");
        }

        if (true == IsValidEpc(EpcToValidate))
        {
            String BinaryEpc = padLeft(new BigInteger(EpcToValidate,CONVERTHEX).toString(CONVERTBINARY), 96, '0') ;
            // Extract the Filter Value
            ReturnSgtin96.filterValue = Integer.parseInt(BinaryEpc.substring(8, 8+3),CONVERTBINARY);
            // Extract the Partition Value
            ReturnSgtin96.partition = Integer.parseInt(BinaryEpc.substring(11, 11+ 3), CONVERTBINARY);
            // Populate the Digit and Bit length fields for the
            // Company Prefix and Item Reference values
            SetLengthsFromPartition(ReturnSgtin96);

            // Extract the Company Prefix
            ReturnSgtin96.companyPrefix =
                    new BigInteger(BinaryEpc.substring(14, ReturnSgtin96.companyPrefixLengthInBits + 14), 2);
            // Extract the Item Reference
            ReturnSgtin96.itemReference =
                    new BigInteger(BinaryEpc.substring(14 + ReturnSgtin96.companyPrefixLengthInBits, 14 + ReturnSgtin96.companyPrefixLengthInBits + ReturnSgtin96.itemReferenceLengthInBits), 2);
            // Extract the Serial number
            ReturnSgtin96.serialNumber =
                    new BigInteger(BinaryEpc.substring(58, 58 + 38), 2);
            // Add company prefix digits, less the first '0' character,
            // which is added in the UPC to GTIN conversion
            String companyPrefixFormat = "%0" + (ReturnSgtin96.companyPrefixLengthInDigits - 1 ) + "d";
            ReturnSgtin96.upc = String.format( companyPrefixFormat, ReturnSgtin96.companyPrefix);
            // If the number of companyPrefix digits is less than 12, then
            // Add item reference digits, less the first '0' character,
            // which is added in the UPC to GTIN conversion
            if (12 > ReturnSgtin96.companyPrefixLengthInDigits)
            {
                String itemPrefixFormat = "%0" + (ReturnSgtin96.itemReferenceLengthInDigits - 1 ) + "d";
                ReturnSgtin96.upc += String.format(itemPrefixFormat, ReturnSgtin96.itemReference );
            }



            // Now calculate the UPC check digit
            ReturnSgtin96.upcCheckDigit = CalculateUpcCheckDigit(ReturnSgtin96.upc);
        }
        else
        {
           return null;
        }

        return ReturnSgtin96;
    }

   /**
    * Creates an Sgtin96 object from the provided UPC data according
    * to the procedure outlined in the GS1 document
    * "Translate a U.P.C. to a GTIN to an SGTIN to an EPC"
    * Found on 01/02/2014 at:
    * http://www.gs1us.org/DesktopModules/Bring2mind/DMX/Download.aspx?EntryId=361&Command=CoreDownload&PortalId=0&TabId=73
    * 
    * @param UPC  The Universal Product Code, as a String, to create an SGTIN-96 from.
    * @param companyPrefixLength The length of the Company Prefix field in the UPC. Valid values are 6 to 10, inclusive.
    * @return A populated Sgtin96 object, without a serial number.
    */
    public static Sgtin96 FromUPC(String UPC, int companyPrefixLength)
    {
        Sgtin96 ReturnSgtin96 = null;

        StringBuilder UriRepresentation = new StringBuilder();

        // Verify that the provided UPC is valid
        if (IsValidGtin(UPC))
        {
            // Build an appropriate URI String.
            // First, add the prefix:
            UriRepresentation.append(uriPrefix);
            // append the filter value:
            UriRepresentation.append("1.");
            // Extract the company prefix. As a '0' prefix is added to
            // make up the correct number of company prefix digits,
            // per the GS1 standard, we extract one less character than
            // is defined by companyPrefixLength.
            // Ref: http://www.gs1us.org/DesktopModules/Bring2mind/DMX/Download.aspx?EntryId=361&Command=CoreDownload&PortalId=0&TabId=73
            UriRepresentation.append( padLeft(UPC.substring(0, companyPrefixLength - 1) , companyPrefixLength, '0'));
            // append the period delimiter:
            UriRepresentation.append(".");
            // Add an Indicator Digit value of '0' to indicate item level
            // packaging.
            UriRepresentation.append('0');
            // Add the Item Reference Number, skipping the check digit at the end
            UriRepresentation.append(UPC.substring(companyPrefixLength - 1, UPC.length() - (companyPrefixLength)));
            // append a zero-value serial number
            UriRepresentation.append(".0");
            // Now create SGTIN using the FromUri API:
            ReturnSgtin96 = Sgtin96.FromSgtin96Uri(UriRepresentation.toString());
        }
        else
        {
            throw (new RuntimeException("Invalid UPC String."));
        }

        return ReturnSgtin96;
    }

    public static Sgtin96 FromGTIN(String Gtin, int companyPrefixLength)
    {
        Sgtin96 ReturnSgtin96 = new Sgtin96();
        String gtinLessFillerDigit = Gtin.substring(1, Gtin.length() - 1);

        StringBuilder UriRepresentation = new StringBuilder();

        // Verify that the provided UPC is valid
        if (IsValidGtin(Gtin))
        {
            // Build an appropriate URI String.
            // First, add the prefix:
            UriRepresentation.append(uriPrefix);
            // append the filter value:
            UriRepresentation.append("1.");
            // Extract the company prefix
            UriRepresentation.append(gtinLessFillerDigit.substring(0, companyPrefixLength));
            // append the period delimiter:
            UriRepresentation.append(".");
            // Add an Indicator Digit value of '0' to indicate item level
            // packaging.
            UriRepresentation.append('0');
            // Add the Item Reference Number, skipping the check digit at the end
            int ItemReferenceDigitCount = (gtinLessFillerDigit.length() - companyPrefixLength) - 1;
            UriRepresentation.append(gtinLessFillerDigit.substring(companyPrefixLength, ItemReferenceDigitCount));
            // append a zero-value serial number
            UriRepresentation.append(".0");
            // Now create SGTIN using the FromUri API:
            ReturnSgtin96 = Sgtin96.FromSgtin96Uri(UriRepresentation.toString());
        }
        else
        {
            throw (new RuntimeException("Invalid GTIN String."));
        }

        return ReturnSgtin96;
    }

    /**
     * Returns object contents in SGTIN-96 URI String format.
     */
    @Override
    public String toString()
    {
        return this.ToUri();
    }

    /**
     * Returns object contents in SGTIN-96 URI String format.
     *
     * @return SGTIN-96 URI String representation of object.
     */
    public String ToUri()
    {
        // Placeholder for building URI String
        StringBuilder SgtinUri = new StringBuilder();

        // Add the URI prefix
        SgtinUri.append(uriPrefix);
        // append the Filter Value
        SgtinUri.append(filterValue);
        // Add the '.' delimiter
        SgtinUri.append(".");

        // append the Company Prefix
        SgtinUri.append(padLeft(companyPrefix.toString(), companyPrefixLengthInDigits, '0'));
        // Add the '.' delimiter
        SgtinUri.append(".");
        // append the Item Reference
        SgtinUri.append( padLeft(itemReference.toString(), itemReferenceLengthInDigits, '0'));
        // Add the '.' delimiter
        SgtinUri.append(".");

        // append the Serial Number
        SgtinUri.append(serialNumber.toString());

        // Return the URI String
        return SgtinUri.toString();
    }

    /**
     * Returns object contents in SGTIN-96 EPC String format.
     *
     * @return SGTIN-96 EPC String representation of object.
     */
    public String ToEpc()
    {
        // Placeholder for a binary representation of the EPC
        StringBuilder BinarySgtinEpc = new StringBuilder();

        // Add the SGTIN-96 header in binary format
        BinarySgtinEpc.append(padLeft( Integer.toString(header, CONVERTBINARY), 8, '0'));
        // append the Filter Value in binary format
        BinarySgtinEpc.append (padLeft(Integer.toString(filterValue, CONVERTBINARY),3, '0'));
        // append the Partition in binary format
        BinarySgtinEpc.append( padLeft( Integer.toString(partition, CONVERTBINARY), 3, '0'));
        // append the Company Prefix
        BinarySgtinEpc.append(padLeft(companyPrefix.toString(CONVERTBINARY), companyPrefixLengthInBits, '0'));
        // append the Item Reference
        BinarySgtinEpc.append(padLeft(itemReference.toString(CONVERTBINARY), itemReferenceLengthInBits, '0'));
        // append the Serial Number
        BinarySgtinEpc.append(padLeft(serialNumber.toString(CONVERTBINARY), 38, '0'));

        // Return the EPC String in Hexadecimal format
        return Integer.toHexString(Integer.parseInt(BinarySgtinEpc.toString(), 2));
    }

    /**
     * Returns UPC for object contents
     *
     * @return UPC String representation of object.
     */
    public String ToUpc()
    {
        // Placeholder for building URI String
        StringBuilder SgtinUpc = new StringBuilder();

        SgtinUpc.append(upc);
        SgtinUpc.append(upcCheckDigit);

        // Return the URI String
        return SgtinUpc.toString();
    }

    /**
     * Returns EAN for object contents
     *
     * @return EAN String representation of object.
     */
    public String toEAN(){
        // Placeholder for building URI String
        StringBuilder SgtinUpc = new StringBuilder();

        SgtinUpc.append(upc);
        SgtinUpc.append(upcCheckDigit);

        // Return the URI String
        return padLeft(SgtinUpc.toString(), 13, '0');
    }

    /**
     * Examines a String to determine whether it is a valid SGTIN in
     * either URI or EPC form.
     *
     * @param testString String to test
     *
     * @return True if the String is a valid SGTIN.  False if the String is not a value SGTIN.
     */
    public static boolean IsValidSGTIN(String testString)
    {
        boolean validSGTIN = false;

        if ( testString != null && !testString.isEmpty())
        {
            // A valid SGTIN has to pass scrutiny either a URI or an EPC
            validSGTIN = IsValidUri(testString) || IsValidEpc(testString);
        }

        return validSGTIN;
    }

    /**
     * get GTIN from SGTIN string
     *
     * @param SGTIN
     *
     * @return GTIN
     */
    public static String GetGTIN(String inputSGTIN)
    {
        String returnGTIN = "";

        if (inputSGTIN == null || inputSGTIN.isEmpty())
        {
            throw new NullPointerException("Null or Empty SGTIN-96 String.");
        }

        if (true == IsValidUri(inputSGTIN))
        {
            // It is a URI String, so populate the object data members
            // accordingly
            try
            {
                Sgtin96 tempSgtin = FromSgtin96Uri(inputSGTIN);
                tempSgtin.serialNumber = BigInteger.ZERO;
                returnGTIN = tempSgtin.ToUri();
            }
            catch (Exception ex)
            {
                throw ex;
            }
        }
        else if (true == IsValidEpc(inputSGTIN))
        {
            // It is an EPC String, so populate the object data members
            // accordingly
            try
            {
                Sgtin96 tempSgtin = FromSgtin96Epc(inputSGTIN);
                tempSgtin.serialNumber = BigInteger.ZERO;
                returnGTIN = tempSgtin.ToUri();
            }
            catch (Exception ex)
            {
                throw ex;
            }
        }
        else
        {
            throw new RuntimeException("Invalid SGTIN-96 String");
        }

        return returnGTIN;
    }

    /// <summary>
    /// Returns an SGTIN value with a zero value serial number.
    /// </summary>
    /// <returns>
    /// String URI representation of Sgtin96 object with
    /// zero value serial number.
    /// </returns>
    public String GetSGTINZeroValueSerialNumber()
    {
        String returnSgtin ="";

        BigInteger tempSerialNumber = serialNumber;
        serialNumber = BigInteger.ZERO;

        returnSgtin = this.ToUri();

        serialNumber = tempSerialNumber;

        return returnSgtin;
    }

    /**
     * Compares two Sgtin96 object data member values.
     * @param obj An Sgtin96 object to compare against.
     *
     * @return  true if data members of both objects match, false if they do not.
     */
    @Override
    public boolean equals(Object obj) {

            // If parameter is null return false.
            if (obj == null) {
                return false;
            }

            // If parameter cannot be cast to Sgtin96 return false.
            Sgtin96 p = (Sgtin96) obj;
            if (p == null) {
                return false;
            }

            // Return true if the fields match:
            return (filterValue == p.filterValue) &&
                    (partition == p.partition) &&
                    (companyPrefix == p.companyPrefix) &&
                    (companyPrefixLengthInBits == p.companyPrefixLengthInBits) &&
                    (companyPrefixLengthInDigits == p.companyPrefixLengthInDigits) &&
                    (itemReference == p.itemReference) &&
                    (itemReferenceLengthInBits == p.itemReferenceLengthInBits) &&
                    (itemReferenceLengthInDigits == p.itemReferenceLengthInDigits) &&
                    (serialNumber == p.serialNumber);

    }

    @Override
    public int hashCode()
    {
        return super.hashCode() ^ filterValue;
    }


    public boolean isSameProduct(Sgtin96 obj) {

        // If parameter is null return false.
        if (obj == null) {
            return false;
        }

        // If parameter cannot be cast to Sgtin96 return false.
        Sgtin96 p = (Sgtin96) obj;
        if (p == null) {
            return false;
        }
        return  (companyPrefix == p.companyPrefix) &&
                (companyPrefixLengthInBits == p.companyPrefixLengthInBits) &&
                (companyPrefixLengthInDigits == p.companyPrefixLengthInDigits) &&
                (itemReference == p.itemReference) &&
                (itemReferenceLengthInBits == p.itemReferenceLengthInBits) &&
                (itemReferenceLengthInDigits == p.itemReferenceLengthInDigits);
    }

    private static boolean IsValidUri(String CandidateSgtin)
    {
        boolean ValidUriDetected = false;
        String[] DataValues;
        String SgtinCandidate = "";

        // Check to make sure that we have data to work with;
        // a null or empty String is invalid. Throw the appropriate
        // exception if this is the case.
        if (CandidateSgtin != null && !CandidateSgtin.isEmpty())
        {
            SgtinCandidate = CandidateSgtin;

            // Strip out the fixed URI prefix String so that we can look at the data
            String DataFields = SgtinCandidate.replace(uriPrefix,"");
            // Now, extract each of the remaining fields as a separate String
            DataValues = DataFields.split(".");

            // Define this variable here so that it has scope outside of
            // the IF. Negative logic used so that this value is meaningful
            // in determining the value of ValidUriDetected
            boolean DataValuesAreNotNumeric = false;

            // If there are not enough URI fields, return false
            if (4 == DataValues.length)
            {
                // Ensure that each of the DataValues is numeric

                Pattern IsDigit =  Pattern.compile("[\\d]+");
                for (String str : DataValues)
                {
                    if (false == IsDigit.matcher(str).matches())
                    {
                        DataValuesAreNotNumeric = true;
                    }
                }

                // Do further sanity checks only if all DataValues are numeric
                if (false == DataValuesAreNotNumeric)
                {
                    // First, verify the Filter field
                    int FilterField = Integer.parseInt(DataValues[0], CONVERTDECIMAL);

                    if (filterValueMin <= FilterField
                            &&
                            filterValueMax >= FilterField)
                    {
                        // Everything's good so far, so now check the company
                        // prefix and item reference fields.  These must match
                        // one of the the following associations
                        //
                        // Company Prefix Digits | Item Reference Digits
                        // -----------------------------------------
                        //        12             |          1
                        //        11             |          2
                        //        10             |          3
                        //        9              |          4
                        //        8              |          5
                        //        7              |          6
                        //        6              |          7
                        // -----------------------------------------
                        // This table can be summarized by verifying that
                        // the number of Company Prefix digits is between 6 & 12
                        // and that the sum of the Company Prefix digits and
                        // Item Reference digits = 13.

                        String Prefix = DataValues[1];
                        int PrefixDigitCount = Prefix.length();

                        String ItemReference = DataValues[2];
                        int ItemReferenceDigitCount = ItemReference.length();

                        boolean ValidCoPrefixAndItemRef = false;

                        if (6 <= PrefixDigitCount
                                &&
                                12 >= PrefixDigitCount
                                &&
                                (13 == (PrefixDigitCount + ItemReferenceDigitCount))
                                )
                        {
                            ValidCoPrefixAndItemRef = true;
                        }

                        // Now verify that the serial number is 38 bits or less
                        if (true == ValidCoPrefixAndItemRef)
                        {
                            String SerialNumber = DataValues[3];
                            int SerialNumberAsInt = Integer.parseInt(SerialNumber, CONVERTDECIMAL);
                            int SerialNumberBitCount = 0;
                            String SerialNumberAsHex = Integer.toHexString(SerialNumberAsInt);
                            String SerialNumberAsBits =Integer.toBinaryString(SerialNumberAsInt);

                            SerialNumberBitCount = SerialNumberAsBits.length();

                            if (serialNumberMaxBits >= SerialNumberBitCount)
                            {
                                ValidUriDetected = true;
                            }
                        }
                    }
                }
            }
        }

        return ValidUriDetected;
    }

    private static boolean IsValidEpc(String CandidateSgtin)
    {
        boolean ValidEpcDetected = false;

        // Return false immediately if a Null or Empty String is passed in
        if (CandidateSgtin != null && !CandidateSgtin.isEmpty())
        {
            // Verify that the String has the right number of characters
            // for an EPC
            if (24 == CandidateSgtin.length())
            {

                // Translate the EPC from a Hex String to a binary String
                String BinaryEpc = padLeft(new BigInteger(CandidateSgtin, CONVERTHEX).toString(CONVERTBINARY), 96, '0');
                Integer HeaderValue;
                Integer FilterValue;

                // Verify that the EPC is represented by 96-bits
                if (96 != BinaryEpc.length())
                {
                   return false;
                }
                else
                {
                    // Verify that the Header is correct
                    HeaderValue = Integer.parseInt(BinaryEpc.substring(0, 8), CONVERTBINARY);
                    if (header == HeaderValue)
                    {
                        // Verify that the Filter is between the min and max values
                        FilterValue = Integer.parseInt(BinaryEpc.substring(8, 8 + 1), CONVERTBINARY);

                        if ((filterValueMin <= FilterValue
                                &&
                                filterValueMax >= FilterValue))
                        {
                            // Everything's good, so continue and verify the
                            // Partition is between min and max values
                            Integer Partition = Integer.parseInt(BinaryEpc.substring(11, 11 + 3 ), 2);
                            if ((partitionValueMin <= Partition
                                    &&
                                    partitionValueMax >= Partition))
                            {
                                // The remainder of the bits are very difficult
                                // to verify, so call the EPC good
                                ValidEpcDetected = true;
                            }
                        }
                    }
                }
            }
        }

        return ValidEpcDetected;
    }

    private static int CalculateUpcCheckDigit(String UPC)
    {
        int check = 0;

        if (UPC == UPC.replaceAll("[^0-9]",  ""))
        {

            // pad with zeros to lengthen to 13 digits
            UPC = padLeft( UPC, 13, '0');

            // evaluate check digit
            int[] a = new int[13];
            a[0] = Character.getNumericValue(UPC.charAt(0)) * 3;
            a[1] = Character.getNumericValue(UPC.charAt(1));
            a[2] = Character.getNumericValue(UPC.charAt(2)) * 3;
            a[3] = Character.getNumericValue(UPC.charAt(3));
            a[4] = Character.getNumericValue(UPC.charAt(4)) * 3;
            a[5] = Character.getNumericValue(UPC.charAt(5));
            a[6] = Character.getNumericValue(UPC.charAt(6)) * 3;
            a[7] = Character.getNumericValue(UPC.charAt(7));
            a[8] = Character.getNumericValue(UPC.charAt(8)) * 3;
            a[9] = Character.getNumericValue(UPC.charAt(9));
            a[10] =  Character.getNumericValue(UPC.charAt(10))* 3;
            a[11] =  Character.getNumericValue(UPC.charAt(11));
            a[12] =  Character.getNumericValue(UPC.charAt(12)) * 3;
            int sum = a[0] + a[1] + a[2] + a[3] + a[4] + a[5] + a[6] + a[7] + a[8] + a[9] + a[10] + a[11] + a[12];
            check = (10 - (sum % 10)) % 10;
        }

        return check;
    }

    public static boolean IsValidGtin(String code)
    {
        if (code != code.replaceAll("[^0-9]",  ""))
        {
            // is not numeric
            return false;
        }
        // pad with zeros to lengthen to 14 digits
        switch (code.length())
        {
            case 8:
                code = "000000" + code;
                break;
            case 12:
                code = "00" + code;
                break;
            case 13:
                code = "0" + code;
                break;
            case 14:
                break;
            default:
                // wrong number of digits
                return false;
        }
        // calculate check digit
        int[] a = new int[13];
        a[0] = Character.getNumericValue(code.charAt(0)) * 3;
        a[1] = Character.getNumericValue(code.charAt(1));
        a[2] = Character.getNumericValue(code.charAt(2)) * 3;
        a[3] = Character.getNumericValue(code.charAt(3));
        a[4] = Character.getNumericValue(code.charAt(4)) * 3;
        a[5] = Character.getNumericValue(code.charAt(5));
        a[6] = Character.getNumericValue(code.charAt(6)) * 3;
        a[7] = Character.getNumericValue(code.charAt(7));
        a[8] = Character.getNumericValue(code.charAt(8)) * 3;
        a[9] = Character.getNumericValue(code.charAt(9));
        a[10] =  Character.getNumericValue(code.charAt(10))* 3;
        a[11] =  Character.getNumericValue(code.charAt(11));
        a[12] =  Character.getNumericValue(code.charAt(12)) * 3;
        int sum = a[0] + a[1] + a[2] + a[3] + a[4] + a[5] + a[6] + a[7] + a[8] + a[9] + a[10] + a[11] + a[12];
        int check = (10 - (sum % 10)) % 10;
        // evaluate check digit
        int last = Character.getNumericValue(code.charAt(13));
        return check == last;
    }

    public static boolean IsValidUPC(String UPC)
    {
        if (UPC != UPC.replaceAll("[^0-9]",  ""))
        {
            // is not numeric
            return false;
        }

        // pad with zeros to lengthen to 14 digits
        switch (UPC.length())
        {
            case 8:
            case 12:
            case 13:
            case 14:
                UPC = String.format("%1$" + (14 - UPC.length()) + "s", UPC).replace(' ','0');
                break;
            default:
                // wrong number of digits
                return false;
        }

        // evaluate check digit
        int[] a = new int[13];
        a[0] = Character.getNumericValue(UPC.charAt(0)) * 3;
        a[1] = Character.getNumericValue(UPC.charAt(1));
        a[2] = Character.getNumericValue(UPC.charAt(2)) * 3;
        a[3] = Character.getNumericValue(UPC.charAt(3));
        a[4] = Character.getNumericValue(UPC.charAt(4)) * 3;
        a[5] = Character.getNumericValue(UPC.charAt(5));
        a[6] = Character.getNumericValue(UPC.charAt(6)) * 3;
        a[7] = Character.getNumericValue(UPC.charAt(7));
        a[8] = Character.getNumericValue(UPC.charAt(8)) * 3;
        a[9] = Character.getNumericValue(UPC.charAt(9));
        a[10] =  Character.getNumericValue(UPC.charAt(10))* 3;
        a[11] =  Character.getNumericValue(UPC.charAt(11));
        a[12] =  Character.getNumericValue(UPC.charAt(12)) * 3;
        int sum = a[0] + a[1] + a[2] + a[3] + a[4] + a[5] + a[6] + a[7] + a[8] + a[9] + a[10] + a[11] + a[12];
        int check = (10 - (sum % 10)) % 10;
        // evaluate check digit
        int last = Character.getNumericValue(UPC.charAt(13));
        return check == last;
    }

    private static void SetLengthsFromPartition(Sgtin96 Sgtin96ToUpdate)
    {
        switch (Sgtin96ToUpdate.partition)
        {
            case 0:
                Sgtin96ToUpdate.companyPrefixLengthInBits = 40;
                break;
            case 1:
                Sgtin96ToUpdate.companyPrefixLengthInBits = 37;
                break;
            case 2:
                Sgtin96ToUpdate.companyPrefixLengthInBits = 34;
                break;
            case 3:
                Sgtin96ToUpdate.companyPrefixLengthInBits = 30;
                break;
            case 4:
                Sgtin96ToUpdate.companyPrefixLengthInBits = 27;
                break;
            case 5:
                Sgtin96ToUpdate.companyPrefixLengthInBits = 24;
                break;
            case 6:
                Sgtin96ToUpdate.companyPrefixLengthInBits = 20;
                break;
            default:
                break;
        }
        Sgtin96ToUpdate.itemReferenceLengthInBits =
                44 - Sgtin96ToUpdate.companyPrefixLengthInBits;
        Sgtin96ToUpdate.itemReferenceLengthInDigits =
                1 + Sgtin96ToUpdate.partition;
        Sgtin96ToUpdate.companyPrefixLengthInDigits =
                13 - Sgtin96ToUpdate.itemReferenceLengthInDigits;
    }

    public static String padLeft(String stringTopad, int length, char padChar){
       return  String.format("%"+length +"s", stringTopad).replace(' ', padChar);
    }




}
