package com.trend.cloudone.amaas;


import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Random;


public class AMaasFileReaderTest {


    @Test
    public void testAMaasFileReader_FileNotFound() {
        String fileName = "abc.txt";
        try {
            new AMaasFileReader(fileName);
            fail();
        } catch (AMaasException err) {
            assertEquals(AMaasErrorCode.MSG_ID_ERR_FILE_NOT_FOUND, err.getErrorCode());
        }
    }
    

    @Test
    public void testAMaasFileReader_FileNoPermission() {
        String fileName = DataFileCreator.TEST_DATA_FILE_NAME;
        File newFile = null;
        try {
            newFile = new File(fileName);
            newFile.setReadable(false); 
            new AMaasFileReader(fileName);
            fail();
        } catch  (AMaasException err) {
            assertEquals(AMaasErrorCode.MSG_ID_ERR_FILE_NO_PERMISSION, err.getErrorCode());
        } finally {
            newFile.setReadable(true); 
        }
    }
    

    @Test
    public void testReadBytes() {
        try {
            AMaasFileReader fileReader = new AMaasFileReader(DataFileCreator.TEST_DATA_FILE_NAME);
            assertEquals(fileReader.getIdentifier(), DataFileCreator.TEST_DATA_FILE_NAME);
            long len = fileReader.getLength();
            Random rand = new Random();
            int offset = rand.nextInt((int)len);
            int size = rand.nextInt((int)len - offset);
            byte[] buf = new byte[size];
            fileReader.readBytes(offset, buf);
            DataFileCreator.verifyBufWithData(buf, offset); 
        } catch (Exception err) {
            assert false;
        }
    }
}
