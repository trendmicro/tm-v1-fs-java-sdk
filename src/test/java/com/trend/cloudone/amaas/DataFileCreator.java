package com.trend.cloudone.amaas;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public final class DataFileCreator {
    /**
     * Name of test data file.
     */
    public static final String TEST_DATA_FILE_NAME = "test_data.bin";
    private static final int NUM_DATA_LOOP = 10;
    private static final int MAX_POSN_OFFSET = 256;

    private DataFileCreator() {
    }

    /**
     * Create a data file for testing.
     */
    public static void createDataFile() {
        try (FileOutputStream binaryFile = new FileOutputStream(TEST_DATA_FILE_NAME)) {
            for (int j = 0; j < NUM_DATA_LOOP; j++) {
                for (int i = 0; i < MAX_POSN_OFFSET; i++) {
                    // Convert the number to a single byte and write to the file
                    byte byteValue = (byte) i;
                    binaryFile.write(byteValue);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a data file.
     */
    public static void deleteDataFile() {
        File file = new File(TEST_DATA_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Verify the content in the data buffer.
     * @param buf Buffer to be verified.
     * @param offset the offset to used.
     */
    public static void verifyBufWithData(final byte[] buf, final int offset) {
        int blen = buf.length;
        int cnt = 0;
        for (int posn = offset; posn < blen; posn++) {
            int val = posn % MAX_POSN_OFFSET;
            byte byteValue = (byte) val;
            //System.out.println(byteValue);
            //System.out.println(buf[cnt] );
            assert buf[cnt] == byteValue;
            cnt++;
        }
    }
}
