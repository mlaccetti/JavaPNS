package javapns.communication;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * A trust manager that automatically trusts all servers.
 * Used to avoid having handshake errors with Apple's sandbox servers.
 * 
 * @author Sylvain Pedneault
 */
class ServerTrustingTrustManager implements X509TrustManager {
  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    throw new CertificateException("Client is not trusted.");
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) {
    // trust all servers
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return null;//new X509Certificate[0];
  }
}