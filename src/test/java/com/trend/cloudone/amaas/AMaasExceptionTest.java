package com.trend.cloudone.amaas;


import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;


public class AMaasExceptionTest {
    @Test
    public void testGetErrorCode() {
        AMaasException err = new AMaasException(AMaasErrorCode.MSG_ID_GRPC_ERROR, 2, "UNKNOWN");
        assertEquals(AMaasErrorCode.MSG_ID_GRPC_ERROR, err.getErrorCode());
        assertEquals("Received gRPC status code: 2, msg: UNKNOWN.", err.getMessage());
    }


    @Test
    public void testGetErrorCodeThrowable() {
        FileNotFoundException except = new FileNotFoundException("abc.txt not found"); 
        AMaasException err = new AMaasException(AMaasErrorCode.MSG_ID_GRPC_ERROR, except, 2, "UNKNOWN");
        assertEquals(AMaasErrorCode.MSG_ID_GRPC_ERROR, err.getErrorCode());
        assertEquals("Received gRPC status code: 2, msg: UNKNOWN.", err.getMessage());
    }
}
