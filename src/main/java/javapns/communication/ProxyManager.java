package javapns.communication;

import sun.misc.BASE64Encoder;

/**
 * Main class for dealing with proxies.
 *
 * @author Sylvain Pedneault
 */
public class ProxyManager {

  private static final String LOCAL_PROXY_HOST_PROPERTY          = "javapns.communication.proxyHost";
  private static final String LOCAL_PROXY_PORT_PROPERTY          = "javapns.communication.proxyPort";
  private static final String LOCAL_PROXY_AUTHORIZATION_PROPERTY = "javapns.communication.proxyAuthorization";

  private static final String JVM_PROXY_HOST_PROPERTY          = "https.proxyHost";
  private static final String JVM_PROXY_PORT_PROPERTY          = "https.proxyPort";
  private static final String JVM_PROXY_AUTHORIZATION_PROPERTY = "https.proxyAuthorization";

  private ProxyManager() {
  }

  /**
   * Configure a proxy to use for HTTPS connections created by JavaPNS.
   *
   * @param host the proxyHost
   * @param port the proxyPort
   */
  public static void setProxy(String host, String port) {
    System.setProperty(ProxyManager.LOCAL_PROXY_HOST_PROPERTY, host);
    System.setProperty(ProxyManager.LOCAL_PROXY_PORT_PROPERTY, port);
  }

  /**
   * Configure the authorization for the proxy configured through the setProxy method.
   *
   * @param username the user name to use
   * @param password the password to use
   */
  public static void setProxyBasicAuthorization(String username, String password) {
    ProxyManager.setProxyAuthorization(ProxyManager.encodeProxyAuthorization(username, password));
  }

  /**
   * Configure the authorization for the proxy configured through the setProxy method.
   *
   * @param authorization the pre-encoded value for the Proxy-Authorization header sent to the proxy
   */
  public static void setProxyAuthorization(String authorization) {
    System.setProperty(ProxyManager.LOCAL_PROXY_AUTHORIZATION_PROPERTY, authorization);
  }

  public static String encodeProxyAuthorization(String username, String password) {
    BASE64Encoder encoder = new BASE64Encoder();
    String pwd = username + ":" + password;
    String encodedUserPwd = encoder.encode(pwd.getBytes());
    String authorization = "Basic " + encodedUserPwd;
    return authorization;
  }

  /**
   * Configure a proxy to use for HTTPS connections created anywhere in the JVM (not recommended).
   *
   * @param host the proxyHost
   * @param port the proxyPort
   */
  public static void setJVMProxy(String host, String port) {
    System.setProperty(ProxyManager.JVM_PROXY_HOST_PROPERTY, host);
    System.setProperty(ProxyManager.JVM_PROXY_PORT_PROPERTY, port);
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
  public static String getProxyHost(AppleServer server) {
    String host = server != null ? server.getProxyHost() : null;
    if (host != null && host.length() > 0) {
      return host;
    } else {
      host = System.getProperty(ProxyManager.LOCAL_PROXY_HOST_PROPERTY);
      if (host != null && host.length() > 0) {
        return host;
      } else {
        host = System.getProperty(ProxyManager.JVM_PROXY_HOST_PROPERTY);
        if (host != null && host.length() > 0) {
          return host;
        } else {
          return null;
        }
      }
    }
  }

  /**
   * Get the proxy authorization currently configured.
   * This method checks if a server-specific proxy has been configured,
   * then checks if a proxy has been configured for the entire library,
   * and finally checks if a JVM-wide proxy setting is available for HTTPS.
   *
   * @param server a specific server to check for proxy settings (may be null)
   * @return a proxy authorization, or null if none is configured
   */
  public static String getProxyAuthorization(AppleServer server) {
    String authorization = server != null ? server.getProxyAuthorization() : null;
    if (authorization != null && authorization.length() > 0) {
      return authorization;
    } else {
      authorization = System.getProperty(ProxyManager.LOCAL_PROXY_AUTHORIZATION_PROPERTY);
      if (authorization != null && authorization.length() > 0) {
        return authorization;
      } else {
        authorization = System.getProperty(ProxyManager.JVM_PROXY_AUTHORIZATION_PROPERTY);
        if (authorization != null && authorization.length() > 0) {
          return authorization;
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
  public static int getProxyPort(AppleServer server) {
    String host = server != null ? server.getProxyHost() : null;
    if (host != null && host.length() > 0) {
      return server.getProxyPort();
    } else {
      host = System.getProperty(ProxyManager.LOCAL_PROXY_HOST_PROPERTY);
      if (host != null && host.length() > 0) {
        return Integer.parseInt(System.getProperty(ProxyManager.LOCAL_PROXY_PORT_PROPERTY));
      } else {
        host = System.getProperty(ProxyManager.JVM_PROXY_HOST_PROPERTY);
        if (host != null && host.length() > 0) {
          return Integer.parseInt(System.getProperty(ProxyManager.JVM_PROXY_PORT_PROPERTY));
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
  public static boolean isUsingProxy(AppleServer server) {
    String proxyHost = ProxyManager.getProxyHost(server);
    boolean proxyConfigured = proxyHost != null && proxyHost.length() > 0;
    return proxyConfigured;
  }

}
