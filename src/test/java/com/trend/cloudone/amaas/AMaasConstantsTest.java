package com.trend.cloudone.amaas;


import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class AMaasConstantsTest {
    @Test
    public void testGetHostForRegion() {
        String region = AMaasConstants.getHostForRegion("us-1");
        assertEquals("antimalware.us-1.cloudone.trendmicro.com:443", region);

        region = AMaasConstants.getHostForRegion("ca-1");
        assertEquals("antimalware.ca-1.cloudone.trendmicro.com:443", region);
    }


    @Test
    public void testGetHostForRegionNotExist() {
         String region = AMaasConstants.getHostForRegion("trend-us-1");
         assertEquals(null, region);
    }
}
