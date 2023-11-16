package com.trend.cloudone.amaas;


import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class AMaasRegionTest {
    @Test
    public void testGetHostForRegion() {
        String region = AMaasRegion.getServiceFqdn("us-1");
        assertEquals("antimalware.us-1.cloudone.trendmicro.com", region);

        region = AMaasRegion.getServiceFqdn("eu-central-1");
        assertEquals("antimalware.de-1.cloudone.trendmicro.com", region);
    }


    @Test
    public void testGetHostForRegionNotExist() {
         String region = AMaasRegion.getServiceFqdn("blah blah okay");
         assertEquals("", region);
    }
}
