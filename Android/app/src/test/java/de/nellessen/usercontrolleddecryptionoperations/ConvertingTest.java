package de.nellessen.usercontrolleddecryptionoperations;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ConvertingTest {

    private static final byte[] HEX_BYTES = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
    private static final String EXPECTED_HEX_STRING = "DEADBEEF";

    @Test
    public void createExpectedHexString() {
        String actualHexString = Converting.byteArrayToHexString(HEX_BYTES);
        assertEquals( "Regular multiplication should work", actualHexString, EXPECTED_HEX_STRING);
    }
}