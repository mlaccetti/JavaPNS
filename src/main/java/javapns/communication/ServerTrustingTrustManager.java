package javapns.communication;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * A trust manager that automatically trusts all servers.
 * Used to avoid having handshake errors with Apple's sandbox servers.
 *
 * @author Sylvain Pedneault
 */
class ServerTrustingTrustManager implements X509TrustManager {

  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    throw new CertificateException("Client is not trusted.");
  }

  public void checkServerTrusted(X509Certificate[] chain, String authType) {
    // trust all servers
  }

  public X509Certificate[] getAcceptedIssuers() {
    return null;//new X509Certificate[0];
  }
}