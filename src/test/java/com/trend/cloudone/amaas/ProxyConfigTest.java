package com.trend.cloudone.amaas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import org.junit.Test;

/**
 * Unit tests for ProxyConfig class.
 */
public class ProxyConfigTest {

    private static final int TEST_PORT = 8080;

    @Test
    public void testNoProxyConfiguration() {
        ProxyConfig config = new ProxyConfig(null, null, null, null, null);
        assertNull(config.getProxyUrl("example.com"));
        assertFalse(config.hasProxyAuth());
        assertNull(config.getProxyUser());
        assertNull(config.getProxyPass());
    }

    @Test
    public void testHttpProxyConfiguration() {
        ProxyConfig config = new ProxyConfig(
            "http://proxy.example.com:8080",
            null,
            null,
            null,
            null
        );
        String proxyUrl = config.getProxyUrl("target.com");
        assertEquals("http://proxy.example.com:8080", proxyUrl);
        assertFalse(config.isSocks5Proxy(proxyUrl));

        String[] hostPort = config.parseProxyHostPort(proxyUrl);
        assertEquals("proxy.example.com", hostPort[0]);
        assertEquals("8080", hostPort[1]);
    }

    @Test
    public void testHttpsProxyConfiguration() {
        ProxyConfig config = new ProxyConfig(
            "http://http-proxy.com:8080",
            "https://https-proxy.com:8443",
            null,
            null,
            null
        );
        String proxyUrl = config.getProxyUrl("target.com");
        assertEquals("https://https-proxy.com:8443", proxyUrl);
        assertFalse(config.isSocks5Proxy(proxyUrl));
    }

    @Test
    public void testSocks5ProxyConfiguration() {
        ProxyConfig config = new ProxyConfig(
            null,
            "socks5://socks.example.com:1080",
            null,
            null,
            null
        );
        String proxyUrl = config.getProxyUrl("target.com");
        assertEquals("socks5://socks.example.com:1080", proxyUrl);
        assertTrue(config.isSocks5Proxy(proxyUrl));

        String[] hostPort = config.parseProxyHostPort(proxyUrl);
        assertEquals("socks.example.com", hostPort[0]);
        assertEquals("1080", hostPort[1]);
    }

    @Test
    public void testProxyAuthentication() {
        ProxyConfig config = new ProxyConfig(
            "http://proxy.com:8080",
            null,
            null,
            "testuser",
            "testpass"
        );
        assertTrue(config.hasProxyAuth());
        assertEquals("testuser", config.getProxyUser());
        assertEquals("testpass", config.getProxyPass());
    }

    @Test
    public void testIncompleteAuthentication() {
        ProxyConfig config1 = new ProxyConfig(
            "http://proxy.com:8080",
            null,
            null,
            "testuser",
            null
        );
        assertFalse(config1.hasProxyAuth());

        ProxyConfig config2 = new ProxyConfig(
            "http://proxy.com:8080",
            null,
            null,
            null,
            "testpass"
        );
        assertFalse(config2.hasProxyAuth());

        ProxyConfig config3 = new ProxyConfig(
            "http://proxy.com:8080",
            null,
            null,
            "",
            "testpass"
        );
        assertFalse(config3.hasProxyAuth());
    }

    @Test
    public void testNoProxyBypass() {
        ProxyConfig config = new ProxyConfig(
            "http://proxy.com:8080",
            null,
            "localhost,127.0.0.1,*.local",
            null,
            null
        );

        assertNull(config.getProxyUrl("localhost"));
        assertNull(config.getProxyUrl("127.0.0.1"));
        assertNotNull(config.getProxyUrl("example.com"));
    }

    @Test
    public void testNoProxyWildcard() {
        ProxyConfig config = new ProxyConfig(
            "http://proxy.com:8080",
            null,
            "*",
            null,
            null
        );

        assertNull(config.getProxyUrl("localhost"));
        assertNull(config.getProxyUrl("example.com"));
        assertNull(config.getProxyUrl("any.host.com"));
    }

    @Test
    public void testNoProxyWithSpaces() {
        ProxyConfig config = new ProxyConfig(
            "http://proxy.com:8080",
            null,
            " localhost , 127.0.0.1 , *.local ",
            null,
            null
        );

        assertNull(config.getProxyUrl("localhost"));
        assertNull(config.getProxyUrl("127.0.0.1"));
        assertNotNull(config.getProxyUrl("example.com"));
    }

    @Test
    public void testDefaultPorts() {
        ProxyConfig config = new ProxyConfig();

        String[] httpHostPort = config.parseProxyHostPort("http://proxy.com");
        assertEquals("proxy.com", httpHostPort[0]);
        assertEquals("80", httpHostPort[1]);

        String[] httpsHostPort = config.parseProxyHostPort("https://proxy.com");
        assertEquals("proxy.com", httpsHostPort[0]);
        assertEquals("443", httpsHostPort[1]);

        String[] socksHostPort = config.parseProxyHostPort(
            "socks5://proxy.com"
        );
        assertEquals("proxy.com", socksHostPort[0]);
        assertEquals("1080", socksHostPort[1]);
    }

    @Test
    public void testInvalidProxyUrls() {
        ProxyConfig config = new ProxyConfig();

        assertNull(config.parseProxyHostPort("invalid-url"));
        assertNull(config.parseProxyHostPort(""));
        assertNull(config.parseProxyHostPort(null));
        assertNull(config.parseProxyHostPort("http://"));
    }

    @Test
    public void testProxyAddress() {
        ProxyConfig config = new ProxyConfig(
            "http://proxy.example.com:8080",
            null,
            null,
            null,
            null
        );

        InetSocketAddress address = config.getProxyAddress(
            "http://proxy.example.com:8080"
        );
        assertNotNull(address);
        assertEquals("proxy.example.com", address.getHostName());
        assertEquals(TEST_PORT, address.getPort());

        // Test invalid URL
        assertNull(config.getProxyAddress("invalid-url"));
    }

    @Test
    public void testHostWithPort() {
        ProxyConfig config = new ProxyConfig(
            "http://proxy.com:8080",
            null,
            "localhost:8080,example.com:443",
            null,
            null
        );

        assertNull(config.getProxyUrl("localhost:8080"));
        assertNull(config.getProxyUrl("example.com:443"));
        assertNotNull(config.getProxyUrl("other.com:80"));
    }

    @Test
    public void testEmptyNoProxy() {
        ProxyConfig config = new ProxyConfig(
            "http://proxy.com:8080",
            null,
            "",
            null,
            null
        );
        assertNotNull(config.getProxyUrl("localhost"));
        assertNotNull(config.getProxyUrl("example.com"));
    }

    @Test
    public void testCaseInsensitiveSocks5Detection() {
        ProxyConfig config = new ProxyConfig();

        assertTrue(config.isSocks5Proxy("socks5://proxy.com:1080"));
        assertTrue(config.isSocks5Proxy("SOCKS5://proxy.com:1080"));
        assertTrue(config.isSocks5Proxy("Socks5://proxy.com:1080"));
        assertFalse(config.isSocks5Proxy("http://proxy.com:8080"));
        assertFalse(config.isSocks5Proxy("https://proxy.com:8080"));
        assertFalse(config.isSocks5Proxy(null));
    }

    @Test
    public void testWildcardMatching() {
        ProxyConfig config = new ProxyConfig(
            "http://proxy.com:8080",
            null,
            "*.example.com,*.test.local",
            null,
            null
        );

        assertNull(config.getProxyUrl("api.example.com"));
        assertNull(config.getProxyUrl("www.example.com"));
        assertNull(config.getProxyUrl("service.test.local"));
        assertNotNull(config.getProxyUrl("example.com"));
        assertNotNull(config.getProxyUrl("other.com"));
    }

    @Test
    public void testEnvironmentVariableConstructor() {
        // Test the default constructor that reads from environment variables
        ProxyConfig config = new ProxyConfig();

        // Since we can't control environment variables in unit tests,
        // we just verify the constructor doesn't throw exceptions
        assertNotNull(config);

        // Test that it handles null values gracefully
        assertNull(config.getProxyUser());
        assertNull(config.getProxyPass());
        assertFalse(config.hasProxyAuth());
    }
}
