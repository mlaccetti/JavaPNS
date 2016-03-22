package javapns.communication;

/**
 * Main class for dealing with proxies.
 *
 * @author Sylvain Pedneault
 */
class ProxyManager {
  private static final String LOCAL_PROXY_HOST_PROPERTY = "javapns.communication.proxyHost";
  private static final String LOCAL_PROXY_PORT_PROPERTY = "javapns.communication.proxyPort";

  private static final String JVM_PROXY_HOST_PROPERTY = "https.proxyHost";
  private static final String JVM_PROXY_PORT_PROPERTY = "https.proxyPort";

  private ProxyManager() {
    // empty
  }

  /**
   * Configure a proxy to use for HTTPS connections created by JavaPNS.
   *
   * @param host the proxyHost
   * @param port the proxyPort
   */
  public static void setProxy(final String host, final String port) {
    System.setProperty(LOCAL_PROXY_HOST_PROPERTY, host);
    System.setProperty(LOCAL_PROXY_PORT_PROPERTY, port);
  }

  /**
   * Configure a proxy to use for HTTPS connections created anywhere in the JVM (not recommended).
   *
   * @param host the proxyHost
   * @param port the proxyPort
   */
  public static void setJVMProxy(final String host, final String port) {
    System.setProperty(JVM_PROXY_HOST_PROPERTY, host);
    System.setProperty(JVM_PROXY_PORT_PROPERTY, port);
  }

  /**
   * Get the proxy host address currently configured.
   * This method checks if a server-specific proxy has been configured,
   * then checks if a proxy has been configured for the entire library,
   * and finally checks if a JVM-wide proxy setting is available for HTTPS.
   *
   * @param server a specific server to check for proxy settings (may be null)
   * @return a proxy host, or null if none is configured
   */
  static String getProxyHost(final AppleServer server) {
    String host = server != null ? server.getProxyHost() : null;
    if (host != null && host.length() > 0) {
      return host;
    } else {
      host = System.getProperty(LOCAL_PROXY_HOST_PROPERTY);
      if (host != null && host.length() > 0) {
        return host;
      } else {
        host = System.getProperty(JVM_PROXY_HOST_PROPERTY);
        if (host != null && host.length() > 0) {
          return host;
        } else {
          return null;
        }
      }
    }
  }

  /**
   * Get the proxy port currently configured.
   * This method first locates a proxy host setting, then returns the proxy port from the same location.
   *
   * @param server a specific server to check for proxy settings (may be null)
   * @return a network port, or 0 if no proxy is configured
   */
  static int getProxyPort(final AppleServer server) {
    String host = server != null ? server.getProxyHost() : null;
    if (host != null && host.length() > 0) {
      return server.getProxyPort();
    } else {
      host = System.getProperty(LOCAL_PROXY_HOST_PROPERTY);
      if (host != null && host.length() > 0) {
        return Integer.parseInt(System.getProperty(LOCAL_PROXY_PORT_PROPERTY));
      } else {
        host = System.getProperty(JVM_PROXY_HOST_PROPERTY);
        if (host != null && host.length() > 0) {
          return Integer.parseInt(System.getProperty(JVM_PROXY_PORT_PROPERTY));
        } else {
          return 0;
        }
      }
    }
  }

  /**
   * Determine if a proxy is currently configured.
   *
   * @param server a specific server to check for proxy settings (may be null)
   * @return true if a proxy is set, false otherwise
   */
  static boolean isUsingProxy(final AppleServer server) {
    final String proxyHost = getProxyHost(server);
    return proxyHost != null && proxyHost.length() > 0;
  }
}
