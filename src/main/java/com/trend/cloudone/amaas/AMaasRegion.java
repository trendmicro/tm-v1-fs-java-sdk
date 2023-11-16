package com.trend.cloudone.amaas;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;


/*
 * Class defines AMaaS Regions.
 */
final class AMaasRegion {
    private static final Logger logger = Logger.getLogger(AMaasClient.class.getName());

    static final String AWS_JP_REGION    = "ap-northeast-1";
    static final String AWS_SG_REGION    = "ap-southeast-1";
    static final String AWS_AU_REGION    = "ap-southeast-2";
    static final String AWS_IN_REGION    = "ap-south-1";
    static final String AWS_US_REGION    = "us-east-1";
    static final String AWS_DE_REGION    = "eu-central-1";
    static final String AWS_CA_REGION    = "ca-central-1";
    static final String AWS_TREND_REGION = "us-east-2";
    static final String AWS_GB_REGION    = "eu-west-2";
    static final String C1_JP_REGION     = "jp-1";
    static final String C1_SG_REGION     = "sg-1";
    static final String C1_AU_REGION     = "au-1";
    static final String C1_IN_REGION     = "in-1";
    static final String C1_US_REGION     = "us-1";
    static final String C1_DE_REGION     = "de-1";
    static final String C1_CA_REGION     = "ca-1";
    static final String C1_TREND_REGION  = "trend-us-1";
    static final String C1_GB_REGION     = "gb-1";

    static final List<String> C1Regions = Arrays.asList(new String[]{C1_AU_REGION, C1_CA_REGION, C1_DE_REGION, C1_GB_REGION, C1_IN_REGION, C1_JP_REGION, C1_SG_REGION, C1_US_REGION, C1_TREND_REGION});
    static final List<String> V1Regions = Arrays.asList(new String[]{AWS_AU_REGION, AWS_CA_REGION, AWS_DE_REGION, AWS_GB_REGION, AWS_IN_REGION, AWS_JP_REGION, AWS_SG_REGION, AWS_US_REGION});
    static final List<String> SupportedV1Regions = Arrays.asList(new String[]{AWS_AU_REGION, AWS_DE_REGION, AWS_IN_REGION, AWS_JP_REGION, AWS_SG_REGION, AWS_US_REGION});
    static final List<String> SupportedC1Regions = Arrays.asList(new String[]{C1_AU_REGION, C1_CA_REGION, C1_DE_REGION, C1_GB_REGION, C1_IN_REGION, C1_JP_REGION, C1_SG_REGION, C1_US_REGION});
    static final List<String> AllRegions = AMaasRegion.concat(C1Regions, V1Regions);
    static final List<String> AllValidRegions = AMaasRegion.concat(SupportedC1Regions, SupportedV1Regions);

    static final Map<String, String> V1ToC1RegionMapping = new HashMap<String, String>() {
        {
	        put(AWS_AU_REGION, C1_AU_REGION);
	        put(AWS_DE_REGION, C1_DE_REGION);
	        put(AWS_IN_REGION, C1_IN_REGION);
	        put(AWS_JP_REGION, C1_JP_REGION);
	        put(AWS_SG_REGION, C1_SG_REGION);
	        put(AWS_US_REGION, C1_US_REGION);
        }
    };

    static final Map<String, String> C1Region2HostMapping = new HashMap<String, String>(){
		{
            put(C1_US_REGION, "antimalware.us-1.cloudone.trendmicro.com");
		    put(C1_IN_REGION, "antimalware.in-1.cloudone.trendmicro.com");
		    put(C1_DE_REGION, "antimalware.de-1.cloudone.trendmicro.com");
		    put(C1_SG_REGION, "antimalware.sg-1.cloudone.trendmicro.com");
		    put(C1_AU_REGION, "antimalware.au-1.cloudone.trendmicro.com");
		    put(C1_JP_REGION, "antimalware.jp-1.cloudone.trendmicro.com");
		    put(C1_GB_REGION, "antimalware.gb-1.cloudone.trendmicro.com");
		    put(C1_CA_REGION, "antimalware.ca-1.cloudone.trendmicro.com");
		    put(C1_TREND_REGION, "antimalware.trend-us-1.cloudone.trendmicro.com");
        }
	};

    private static List<String> concat(List<String> array1, List<String> array2) {

        List<String> result = new ArrayList<String>(AMaasRegion.C1Regions);
        result.addAll(V1Regions);
        return result;
    }

    /*
     * Method to get FQDN for the given region.
     * @param targetRegion region to use
     * @return the FQDN of the given region.
     */
    public static String getServiceFqdn(String targetRegion) {
        // ensure the region exists in v1 or c1
	    String region = targetRegion;
        if (!AllRegions.contains(region)) {
            logger.log(Level.INFO,"{0} is not a supported region", region);
            return "";
	    }
	    // if it is a V1 region, map it to a C1 region
        if (SupportedV1Regions.contains(region)) {
            String c1_region = AMaasRegion.V1ToC1RegionMapping.get(region);
            if (c1_region == "") {
                logger.log(Level.INFO,"{0} is not a supported region", region);
                return "";
            }
            region = c1_region;
        }

        String fqdn = AMaasRegion.C1Region2HostMapping.get(region);
        if (fqdn == null) {
            logger.log(Level.INFO,"{0} is not a supported region", region);
            return "";
        }
        return fqdn;
    }

    public static String getAllRegionsAsString() {
        return String.join(",", AMaasRegion.AllValidRegions);
    }
}
