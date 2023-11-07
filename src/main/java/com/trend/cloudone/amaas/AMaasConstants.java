package com.trend.cloudone.amaas;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.grpc.Metadata;

/*
 * Class defines AMaaS SDK constants.
 */
final class AMaasConstants {

    /*
     * Enum class for AMaaS authentication token types.
     */
    public enum TokenType {
        AUTH_TYPE_BEARER,
        AUTH_TYPE_APIKEY
    }

    protected static final Metadata.Key<String> META_DATA_KEY = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    /*
     * Data structure to store region to grpc scanner server mapping.
     */
    private static final Map<String, String> regionMap = Stream.of(new String[][] {
        {"us-1", "antimalware.us-1.cloudone.trendmicro.com:443"},
        {"in-1", "antimalware.in-1.cloudone.trendmicro.com:443"},
        {"de-1", "antimalware.de-1.cloudone.trendmicro.com:443"},
        {"sg-1", "antimalware.sg-1.cloudone.trendmicro.com:443"},
        {"au-1", "antimalware.au-1.cloudone.trendmicro.com:443"},
        {"jp-1", "antimalware.jp-1.cloudone.trendmicro.com:443"},
        {"gb-1", "antimalware.gb-1.cloudone.trendmicro.com:443"},
        {"ca-1", "antimalware.ca-1.cloudone.trendmicro.com:443"},
        {"ae-1", "antimalware.ae-1.cloudone.trendmicro.com:443"}
      }).collect(Collectors.toMap(data -> data[0], data -> data[1]));


    protected static final String getHostForRegion(String region) {
        return AMaasConstants.regionMap.get(region);
    }
}
