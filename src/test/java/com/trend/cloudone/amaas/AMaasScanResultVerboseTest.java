package com.trend.cloudone.amaas;


import org.junit.Test;
import static org.junit.Assert.assertEquals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.trend.cloudone.amaas.datamodel.AMaasScanResultVerbose;


public class AMaasScanResultVerboseTest {
    private static final int TMBLACK_VERSION = 253;
    private static final int LPTVPN_VERSION = 313;

    @Test
    public void testGoodResult() {
        String scanResult = "{\"scanType\":\"sdk\",\"objectType\":\"file\",\"timestamp\":{\"start\":\"2024-05-04T01:05:14.306Z\",\"end\":\"2024-05-04T01:05:15.572Z\"},\"schemaVersion\":\"1.0.0\",\"scannerVersion\":\"1.0.0-1\",\"fileName\":\"TRENDX_detect.exe\",\"rsSize\":356352,\"scanId\":\"4fa87030-c633-4d95-b9e7-29e6fb189d1e\",\"accountId\":\"\",\"result\":{\"atse\":{\"elapsedTime\":1158239,\"fileType\":7,\"fileSubType\":2,\"version\":{\"engine\":\"23.57.0-1002\",\"lptvpn\":313,\"ssaptn\":721,\"tmblack\":253,\"tmwhite\":229,\"macvpn\":906},\"malwareCount\":0,\"malware\":null,\"error\":null,\"fileTypeName\":\"EXE\",\"fileSubTypeName\":\"VSDT_EXE_W32\"},\"trendx\":{\"elapsedTime\":106343,\"fileType\":7,\"fileSubType\":2,\"version\":{\"engine\":\"23.57.0-1002\",\"tmblack\":253,\"trendx\":332},\"malwareCount\":1,\"malware\":[{\"name\":\"Ransom.Win32.TRX.XXPE1\",\"fileName\":\"/Users/liangsengk/Downloads/PE_TestKit10/detect/TRENDX_detect.exe\",\"type\":\"Ransom\",\"fileType\":0,\"fileSubType\":0,\"fileTypeName\":\"DIR\",\"fileSubTypeName\":\"\"}],\"error\":null,\"fileTypeName\":\"EXE\",\"fileSubTypeName\":\"VSDT_EXE_W32\"}},\"fileSHA1\":\"b448479b0a6a5d387c71600e1b75700ba7f42b0a\",\"fileSHA256\":\"4b7593109f81b5a770d440d8c28fa1457cd4b95d51b5d049fb301fc99c41da39\",\"appName\":\"V1FS\"}";

        Gson gson = new GsonBuilder().create();
        AMaasScanResultVerbose result = gson.fromJson(scanResult, AMaasScanResultVerbose.class);

        assertEquals("2024-05-04T01:05:14.306Z", result.getTimestamp().getStart());
        assertEquals("23.57.0-1002", result.getResult().getAtse().getVersion().getEngine());
        assertEquals(LPTVPN_VERSION, result.getResult().getAtse().getVersion().getLptvpn());
        assertEquals(TMBLACK_VERSION, result.getResult().getTrendx().getVersion().getTmblack());
        assertEquals("EXE", result.getResult().getAtse().getFileTypeName());
        assertEquals("VSDT_EXE_W32", result.getResult().getAtse().getFileSubTypeName());
        assertEquals("EXE", result.getResult().getTrendx().getFileTypeName());
        assertEquals("VSDT_EXE_W32", result.getResult().getTrendx().getFileSubTypeName());
        assertEquals("DIR", result.getResult().getTrendx().getMalware()[0].getFileTypeName());
        assertEquals(null, result.getResult().getAtse().getMalware());
    }
}
