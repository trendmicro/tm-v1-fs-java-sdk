package com.trend.cloudone.amaas;


import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;


public class AMaasFileBufferTest {
    @Test
    public void testReadBytes() {
        try {
            Path path = Paths.get(DataFileCreator.TEST_DATA_FILE_NAME);
            byte[] data = Files.readAllBytes(path);
            AMaasBufferReader bufReader = new AMaasBufferReader(data, DataFileCreator.TEST_DATA_FILE_NAME);
            long len = bufReader.getLength();
            Random rand = new Random();
            int offset = rand.nextInt((int)len);
            int size = rand.nextInt((int)len - offset);
            byte[] buf = new byte[size];
            bufReader.readBytes(offset, buf);
            assertEquals(bufReader.getIdentifier(), DataFileCreator.TEST_DATA_FILE_NAME);
            assertEquals(bufReader.getLength(), data.length);
            DataFileCreator.verifyBufWithData(buf, offset); 
        } catch (Exception err) {
            assert false;
        }
    }
}
