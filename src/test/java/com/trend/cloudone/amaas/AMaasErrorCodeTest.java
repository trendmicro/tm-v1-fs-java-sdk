package com.trend.cloudone.amaas;


import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class AMaasErrorCodeTest {
    @Test
    public void testGetErrorCode() {
        assertEquals("MSG_ID_ERR_FILE_NOT_FOUND", AMaasErrorCode.MSG_ID_ERR_FILE_NOT_FOUND.getErrorCode());
    }


    @Test
    public void testGetMessage() {
        String myMsg = AMaasErrorCode.MSG_ID_ERR_MISSING_AUTH.getMessage();
        assertEquals("Must provide an API key to use the client.", myMsg);
    }


    @Test
    public void testGetMessage2() {
        String myMsg = AMaasErrorCode.MSG_ID_GRPC_ERROR.getMessage(2, "UNKNOWN");
        assertEquals("Received gRPC status code: 2, msg: UNKNOWN.", myMsg);
    }
}
