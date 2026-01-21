package com.trend.cloudone.amaas;


import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class AMaasRegionTest {
    @Test
    public void testGetHostForRegion() {
        String region = AMaasRegion.getServiceFqdn("us-east-1");
        assertEquals("antimalware.us-1.cloudone.trendmicro.com", region);

        region = AMaasRegion.getServiceFqdn("eu-central-1");
        assertEquals("antimalware.de-1.cloudone.trendmicro.com", region);

        region = AMaasRegion.getServiceFqdn("me-central-1");
        assertEquals("antimalware.ae-1.cloudone.trendmicro.com", region);

        region = AMaasRegion.getServiceFqdn("ca-central-1");
        assertEquals("antimalware.ca-1.cloudone.trendmicro.com", region);

        region = AMaasRegion.getServiceFqdn("eu-west-2");
        assertEquals("antimalware.gb-1.cloudone.trendmicro.com", region);
    }


    @Test
    public void testGetHostForRegionNotExist() {
         String region = AMaasRegion.getServiceFqdn("blah blah okay");
         assertEquals("", region);
    }
}
