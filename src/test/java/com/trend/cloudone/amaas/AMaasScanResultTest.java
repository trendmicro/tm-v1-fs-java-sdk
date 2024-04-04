package com.trend.cloudone.amaas;


import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class AMaasScanResultTest {
    @Test
    public void testGoodResult() {
        String version = "1.0.0";
        String ts = "2023-09-01 01:02:03.123456789";
        String fileName = "file1";
        String scanId = "scan123";
        int scanResult = 0;
        AMaasScanResult result = new AMaasScanResult(version, scanResult, scanId, ts, fileName, null);
        assertEquals(version, result.getVersion());
        assertEquals(scanResult, result.getScanResult());
        assertEquals(scanId, result.getScanId());
        assertEquals(fileName, result.getFileName());
        assertArrayEquals(null, result.getFoundMalwares());
    }

    @Test
    public void testBadResult() {
        String version = "1.0.0";
        String ts = "2023-09-01 01:02:03.123456789";
        String fileName = "file1";
        String scanId = "scan123";
        int scanResult = 1;
        MalwareItem[] foundMalwares = new MalwareItem[] {new MalwareItem("malware1", "file1"), new MalwareItem("malware1", "file1")};
        AMaasScanResult result = new AMaasScanResult(version, scanResult, scanId, ts, fileName, foundMalwares);
        assertEquals(version, result.getVersion());
        assertEquals(scanResult, result.getScanResult());
        assertEquals(scanId, result.getScanId());
        assertEquals(fileName, result.getFileName());
        assertArrayEquals(foundMalwares, result.getFoundMalwares());
    }

    @Test
    public void testSetBadResult() {
        String version = "1.0.0";
        String ts = "2023-09-01 01:02:03.123456789";
        String fileName = "file1";
        String scanId = "scan123";
        int scanResult = 0;
        AMaasScanResult result = new AMaasScanResult(version, scanResult, scanId, ts, fileName, null);
        MalwareItem[] foundMalwares = new MalwareItem[] {new MalwareItem("malware1", "file1"), new MalwareItem("malware1", "file1")};
        result.setFoundMalwares(foundMalwares);
        result.setScanResult(1);
        assertEquals(version, result.getVersion());
        assertEquals(1, result.getScanResult());
        assertEquals(scanId, result.getScanId());
        assertEquals(fileName, result.getFileName());
        assertArrayEquals(foundMalwares, result.getFoundMalwares());
    }
}
