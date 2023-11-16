package com.trend.cloudone.amaas;


import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class AMaasCallCredentialsTest {
    @Test
    public void testApplyAPIKey() {
        AMaasCallCredentials cred = new AMaasCallCredentials("tmc12PIlyWZwpwJHTAaycJul1FiXcO7", AMaasConstants.V1FS_APP);
        assertEquals(cred.getTokenType(), AMaasConstants.TokenType.AUTH_TYPE_APIKEY);
    }
}
