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
  public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
    throw new CertificateException("Client is not trusted.");
  }

  public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
    // trust all servers
  }

  public X509Certificate[] getAcceptedIssuers() {
    return null;
  }
}
