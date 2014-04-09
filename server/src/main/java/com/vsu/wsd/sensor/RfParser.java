package com.vsu.wsd.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Victor Su
 */
public class RfParser {
    private static final Logger logger = LoggerFactory.getLogger(RfParser.class);

    private final static String PADDING = "00000000";

    // packet format is:
    //   preamble
    //   28-bit unique identifier
    //   3-bit sequence number
    //   18-bit temperature value
    //   8-bit humidity value
    //   28-bit unknown/checksum
    //
    private final static String PREAMBLE = "101010";
    private final static int UID_LEN = 28;
    private final static int SEQ_LEN = 3;
    private final static int TEMP_LEN = 18;
    private final static int HUMIDITY_LEN = 8;

    private final static Integer[] onesCode = {
            0xF,  // 1111
            0x0,  // 0000
            0x8,  // 1000
            0x7,  // 0111
            0xC,  // 1100
            0x3,  // 0011
            0xB,  // 1011
            0x4,  // 0100
            0xE,  // 1110
            0x1   // 0001
    };

    private final static Integer[] hundredsCode = {
            0xFE, // 11111110
            0x81, // 10000001
            0xC1, // 11000001
            0xBE, // 10111110
            0xE1  // 11100001
    };

    private final static Integer[] signCode = { 1, 0, 0, 1, 0 };
    private final static Integer[] tensMask = { 0, 0xF, 0xF, 0, 0xF, 0, 0, 0xF, 0xF, 0 };


    /**
     * Parse data fields from the specified response data string.  Note that the
     * response data string should only contain the bit stream from the sensor;
     * all packet headers should be stripped.
     * @param data  The response data string.
     * @return      RF data object.
     */
    public static RfData parse(String data) {
        RfData rfData = new RfData();

        int len = (data.length() / 2);
        int i;

        // convert the hex string into a bit string
        StringBuilder builder = new StringBuilder();

        for (i = 0; i < len; i++) {
            // pad with zeros to 8 bits
            String result = PADDING + Integer.toBinaryString(Integer.parseInt(data.substring(2 * i, 2 * i + 2), 16) & 0xff);
            builder.append(result.substring(result.length() - 8, result.length()));
        }

        String bitStr = builder.toString();

        //logger.debug("bitStr: " + bitStr);

        if (bitStr.length() >= PREAMBLE.length() + UID_LEN + SEQ_LEN + TEMP_LEN + HUMIDITY_LEN) {
            // locate the preamble
            int pos = bitStr.indexOf(PREAMBLE);

            if (pos != -1) {
                // parse unique id
                int start = pos + PREAMBLE.length();
                int end = start + UID_LEN;
                String uid = getFragment(bitStr, start, end);

                if (!uid.isEmpty()) {
                    // parse sequence number
                    start = end;
                    end = start + SEQ_LEN;
                    String seq = getFragment(bitStr, start, end);

                    if (!seq.isEmpty()) {
                        rfData.sequence = Integer.parseInt(seq, 2);

                        // parse temperature value
                        start = end;
                        end = start + TEMP_LEN;
                        String temp = getFragment(bitStr, start, end);

                        if (!temp.isEmpty()) {
                            DecodedValue val = new DecodedValue();
                            if (decodeTensAndOnes(temp, val)) {
                                if (decodeSignAndHundreds(temp, val)) {
                                    rfData.temperature = val.getValue();
                                }
                            }

                            // parse humidity value
                            start = end;
                            end = start + HUMIDITY_LEN;
                            String humid = getFragment(bitStr, start, end);

                            if (!humid.isEmpty()) {
                                val = new DecodedValue();
                                if (decodeTensAndOnes(humid, val)) {
                                    rfData.humidity = val.getValue();
                                }
                            }
                        }
                    }
                }
            }
        }

        return rfData;
    }

    /**
     * Returns a fragment from the specified string, handling the condition if
     * the start or end indices are out of bounds.
     * @param str    The string to extract a fragment from.
     * @param start  The start index.
     * @param end    The end index.
     * @return       The string fragment.
     */
    private static String getFragment(String str, int start, int end) {
        String fragment = "";

        try {
            fragment = str.substring(start, end);
        } catch (IndexOutOfBoundsException e) {
        }

        return fragment;
    }

    /**
     * Reverse the bit order in a byte.
     * @param in  The byte to reverse.
     * @return    The byte with bit order reversed.
     */
    private static byte reverseBits(byte in) {
        byte out = 0;

        for (int i = 0 ; i < 8 ; i++) {
            byte bit = (byte)(in & 1);
            out = (byte)((out << 1) | bit);
            in = (byte)(in >> 1);
        }

        return out;
    }

    /**
     * Decodes the hundreds place value and sign from the specified bit string.
     * @param str  The bit string
     * @param val  The decoded value object to return results
     * @return     True if decoded successfully, false otherwise
     */
    private static boolean decodeSignAndHundreds(String str, DecodedValue val) {

        if (str.length() >= 17) {
            int hundreds = Integer.parseInt(str.substring(8, 16), 2);
            int sign = Integer.parseInt(str.substring(16, 17), 2);

            int hundredsValue = Arrays.asList(hundredsCode).indexOf(hundreds);

            if (hundredsValue != -1) {
                val.hundreds = hundredsValue;

                // If the sign bit matches the value in the sign codes,
                // the value is positive, otherwise it is negative
                if (signCode[hundredsValue] != sign) {
                    val.sign = -1;
                } else {
                    val.sign = 1;
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Decodes the ones and tens place values from the specified bit string.
     * @param str  The bit string
     * @param val  The decoded value object to return results
     * @return     True if decoded successfully, false otherwise
     */
    private static boolean decodeTensAndOnes(String str, DecodedValue val) {
        if (str.length() >= 8) {
            int ones = Integer.parseInt(str.substring(0, 4), 2);
            int tens = Integer.parseInt(str.substring(4, 8), 2);

            // get the ones place value
            int onesValue = Arrays.asList(onesCode).indexOf(ones);

            if (onesValue != -1) {
                val.ones = onesValue;

                // generate tens place codes
                Integer[] tensCode = new Integer[onesCode.length];
                for (int i = 0; i < onesCode.length; i++) {
                    tensCode[i] = onesCode[i] ^ tensMask[onesValue];
                }

                // get the tens place value
                int tensValue = Arrays.asList(tensCode).indexOf(tens);

                if (tensValue != -1) {
                    val.tens = tensValue;
                    return true;
                }
            }
        }

        return false;
    }

}
