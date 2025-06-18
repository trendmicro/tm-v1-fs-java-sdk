package com.trend.cloudone.amaas;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration class for handling HTTP and SOCKS5 proxy settings.
 * Reads proxy configuration from environment variables.
 */
public class ProxyConfig {

    private static final Logger logger = Logger.getLogger(
        ProxyConfig.class.getName()
    );
    // Default port constants
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final int DEFAULT_SOCKS_PORT = 1080;

    private final String httpProxy;
    private final String httpsProxy;
    private final String noProxy;
    private final String proxyUser;
    private final String proxyPass;
    private final Set<String> noProxyHosts;

    /**
     * Creates a new ProxyConfig instance by reading environment variables.
     */
    public ProxyConfig() {
        this.httpProxy = System.getenv("HTTP_PROXY");
        this.httpsProxy = System.getenv("HTTPS_PROXY");
        this.noProxy = System.getenv("NO_PROXY");
        this.proxyUser = System.getenv("PROXY_USER");
        this.proxyPass = System.getenv("PROXY_PASS");
        this.noProxyHosts = parseNoProxyHosts(this.noProxy);
    }

    /**
     * Creates a ProxyConfig with explicit values for testing purposes.
     * @param httpProxy HTTP proxy URL
     * @param httpsProxy HTTPS proxy URL
     * @param noProxy comma-separated list of hosts to bypass proxy
     * @param proxyUser proxy authentication username
     * @param proxyPass proxy authentication password
     */
    public ProxyConfig(
        final String httpProxy,
        final String httpsProxy,
        final String noProxy,
        final String proxyUser,
        final String proxyPass
    ) {
        this.httpProxy = httpProxy;
        this.httpsProxy = httpsProxy;
        this.noProxy = noProxy;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
        this.noProxyHosts = parseNoProxyHosts(this.noProxy);
    }

    /**
     * Determines the appropriate proxy URL for the given target host.
     * @param targetHost the target host to connect to
     * @return proxy URL if proxy should be used, null otherwise
     */
    public String getProxyUrl(final String targetHost) {
        if (shouldBypassProxy(targetHost)) {
            return null;
        }

        // For HTTPS targets, prefer HTTPS_PROXY, fallback to HTTP_PROXY
        if (httpsProxy != null && !httpsProxy.isEmpty()) {
            return httpsProxy;
        }

        if (httpProxy != null && !httpProxy.isEmpty()) {
            return httpProxy;
        }

        return null;
    }

    /**
     * Checks if the proxy URL uses SOCKS5 protocol.
     * @param proxyUrl the proxy URL to check
     * @return true if it's a SOCKS5 proxy, false otherwise
     */
    public boolean isSocks5Proxy(final String proxyUrl) {
        if (proxyUrl == null) {
            return false;
        }
        return proxyUrl.toLowerCase().startsWith("socks5://");
    }

    /**
     * Checks if proxy authentication is configured.
     * @return true if both username and password are set, false otherwise
     */
    public boolean hasProxyAuth() {
        return (
            proxyUser != null
            && !proxyUser.trim().isEmpty()
            && proxyPass != null
            && !proxyPass.isEmpty()
        );
    }

    /**
     * Gets the proxy username.
     * @return proxy username or null if not configured
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * Gets the proxy password.
     * @return proxy password or null if not configured
     */
    public String getProxyPass() {
        return proxyPass;
    }

    /**
     * Gets the NO_PROXY value for setting Java system properties.
     * @return NO_PROXY string or null if not configured
     */
    public String getNoProxyValue() {
        return noProxy;
    }

    /**
     * Parses a proxy URL and returns the host and port.
     * @param proxyUrl the proxy URL to parse
     * @return array with [host, port] or null if parsing fails
     */
    public String[] parseProxyHostPort(final String proxyUrl) {
        if (proxyUrl == null || proxyUrl.trim().isEmpty()) {
            return null;
        }

        try {
            URI uri = URI.create(proxyUrl);
            String host = uri.getHost();
            int port = uri.getPort();

            if (host == null) {
                return null;
            }
            // Use default ports if not specified
            if (port == -1) {
                String scheme = uri.getScheme();
                if ("http".equalsIgnoreCase(scheme)) {
                    port = getDefaultHttpPort();
                } else if ("https".equalsIgnoreCase(scheme)) {
                    port = getDefaultHttpsPort();
                } else if ("socks5".equalsIgnoreCase(scheme)) {
                    port = getDefaultSocksPort();
                } else {
                    return null;
                }
            }

            return new String[]{host, String.valueOf(port)};
        } catch (Exception e) {
            logger.log(
                Level.WARNING,
                "Failed to parse proxy URL: " + proxyUrl,
                e
            );
            return null;
        }
    }

    /**
     * Creates an InetSocketAddress for the proxy.
     * @param proxyUrl the proxy URL
     * @return InetSocketAddress for the proxy or null if parsing fails
     */
    public InetSocketAddress getProxyAddress(final String proxyUrl) {
        String[] hostPort = parseProxyHostPort(proxyUrl);
        if (hostPort == null) {
            return null;
        }

        try {
            return new InetSocketAddress(
                hostPort[0],
                Integer.parseInt(hostPort[1])
            );
        } catch (NumberFormatException e) {
            logger.log(
                Level.WARNING,
                "Invalid port number in proxy URL: " + proxyUrl,
                e
            );
            return null;
        }
    }

    /**
     * Checks if the target host should bypass proxy.
     * @param targetHost the target host to check
     * @return true if proxy should be bypassed, false otherwise
     */
    private boolean shouldBypassProxy(final String targetHost) {
        if (
            targetHost == null || noProxyHosts == null || noProxyHosts.isEmpty()
        ) {
            return false;
        }

        // Check for exact match or wildcard
        if (noProxyHosts.contains("*")) {
            return true;
        }

        // Check for exact host match
        if (noProxyHosts.contains(targetHost)) {
            return true;
        }

        // Check for wildcard patterns
        for (String noProxyHost : noProxyHosts) {
            if (
                noProxyHost.startsWith("*.")
                && targetHost.endsWith(noProxyHost.substring(1))
            ) {
                return true;
            }
        }

        return false;
    }

    /**
     * Parses the NO_PROXY environment variable into a set of hosts.
     * @param noProxy the NO_PROXY value
     * @return set of hosts that should bypass proxy
     */
    private Set<String> parseNoProxyHosts(final String noProxy) {
        Set<String> hosts = new HashSet<>();

        if (noProxy == null || noProxy.trim().isEmpty()) {
            return hosts;
        }

        String[] hostArray = noProxy.split(",");
        for (String host : hostArray) {
            String trimmedHost = host.trim();
            if (!trimmedHost.isEmpty()) {
                hosts.add(trimmedHost);
            }
        }

        return hosts;
    }

    /**
     * Gets the default HTTP port.
     * @return default HTTP port (80)
     */
    private static int getDefaultHttpPort() {
        return DEFAULT_HTTP_PORT;
    }

    /**
     * Gets the default HTTPS port.
     * @return default HTTPS port (443)
     */
    private static int getDefaultHttpsPort() {
        return DEFAULT_HTTPS_PORT;
    }

    /**
     * Gets the default SOCKS port.
     * @return default SOCKS port (1080)
     */
    private static int getDefaultSocksPort() {
        return DEFAULT_SOCKS_PORT;
    }
}
