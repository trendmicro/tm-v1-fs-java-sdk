package com.trend.cloudone.amaas;


import org.junit.Test;

import com.trend.cloudone.amaas.AMaasReader.HashType;

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
            AMaasBufferReader bufReader = new AMaasBufferReader(data, DataFileCreator.TEST_DATA_FILE_NAME, true);
            long len = bufReader.getLength();
            Random rand = new Random();
            int offset = rand.nextInt((int) len);
            int size = rand.nextInt((int) len - offset);
            byte[] buf = new byte[size];
            bufReader.readBytes(offset, buf);
            assertEquals(bufReader.getIdentifier(), DataFileCreator.TEST_DATA_FILE_NAME);
            assertEquals(bufReader.getLength(), data.length);
            DataFileCreator.verifyBufWithData(buf, offset);
        } catch (Exception err) {
            assert false;
        }
    }

    @Test
    public void testHashes() {
        try {
            String str = "This is a very long long long test";
            String sha1 = "sha1:e4d5a3a79140b1c141f947efef6a7372c8f2bbc4";
            String sha256 = "sha256:c24f43025bc40b53e4e5948cf69cb59498b47d2127cf358de846cda6fefccc63";
            byte[] data = str.getBytes();
            AMaasBufferReader bufReader = new AMaasBufferReader(data, "abc", true);
            assertEquals(sha1, bufReader.getHash(HashType.HASH_SHA1));
            assertEquals(sha256, bufReader.getHash(HashType.HASH_SHA256));
        } catch (Exception err) {
            assert false;
        }
    }
}
