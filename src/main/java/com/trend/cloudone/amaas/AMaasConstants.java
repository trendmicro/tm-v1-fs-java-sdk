package com.trend.cloudone.amaas;

import io.grpc.Metadata;

/*
 * Class defines AMaaS SDK constants.
 */
final class AMaasConstants {

    public static final String V1FS_APP = "V1FS";

    private AMaasConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /*
     * Enum class for AMaaS authentication token types.
     */
    public enum TokenType {
        AUTH_TYPE_BEARER,
        AUTH_TYPE_APIKEY
    }

    protected static final Metadata.Key<String> META_DATA_KEY = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    protected static final Metadata.Key<String> META_APP_KEY = Metadata.Key.of("tm-app-name", Metadata.ASCII_STRING_MARSHALLER);
}
