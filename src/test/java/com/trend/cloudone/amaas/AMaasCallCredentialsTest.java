package com.trend.cloudone.amaas;


import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class AMaasCallCredentialsTest {
    @Test
    public void testApplyBearToken() {
        AMaasCallCredentials cred = new AMaasCallCredentials("eyhz<eb5j}B>qwe=Ns'_z4p!yMHD@3RL7i*@bY8:!}ZoN`~OgW3F@_s=i!(.ey-*-V>m_,;C_XUFFGGX?)Iey&OgZ66c'h{2;y7Qw");
        assertEquals(cred.getTokenType(), AMaasConstants.TokenType.AUTH_TYPE_BEARER);
    }


    @Test
    public void testApplyAPIKey() {
        AMaasCallCredentials cred = new AMaasCallCredentials("tmc12PIlyWZwpwJHTAaycJul1FiXcO7");
        assertEquals(cred.getTokenType(), AMaasConstants.TokenType.AUTH_TYPE_APIKEY);
    }
}
