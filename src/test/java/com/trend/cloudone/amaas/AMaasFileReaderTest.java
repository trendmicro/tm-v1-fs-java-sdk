package com.trend.cloudone.amaas;


import org.junit.Test;

import com.trend.cloudone.amaas.AMaasReader.HashType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Random;


public class AMaasFileReaderTest {


    @Test
    public void testAMaasFileReaderFileNotFound() {
        String fileName = "abc.txt";
        try {
            new AMaasFileReader(fileName);
            fail();
        } catch (AMaasException err) {
            assertEquals(AMaasErrorCode.MSG_ID_ERR_FILE_NOT_FOUND, err.getErrorCode());
        }
    }


    @Test
    public void testAMaasFileReaderFileNoPermission() {
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
            int offset = rand.nextInt((int) len);
            int size = rand.nextInt((int) len - offset);
            byte[] buf = new byte[size];
            fileReader.readBytes(offset, buf);
            DataFileCreator.verifyBufWithData(buf, offset);
        } catch (Exception err) {
            err.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testFileHashes() {
        try {
            String fileName = "data.txt";
            String path = getClass().getClassLoader().getResource("").getPath();

            String sha1 = "sha1:98026e8c073707b779b0c4b922884d078cf6e110";
            String sha256 = "sha256:67ee709d8a925003d817a98eae4c12f98193f7a44bc262d93e6265c41bf096f5";
            AMaasReader bufReader = new AMaasFileReader(path + "/" + fileName);
            assertEquals(sha1, bufReader.getHash(HashType.HASH_SHA1));
            assertEquals(sha256, bufReader.getHash(HashType.HASH_SHA256));
        } catch (Exception err) {
            err.printStackTrace();
            assert false;
        }
    }
}
