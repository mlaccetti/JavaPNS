package javapns.communication;

import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.KeyStore;

/**
 * <h1>Class representing an abstract connection to an Apple server</h1>
 * <p>
 * Communication protocol differences between Notification and Feedback servers are
 * implemented in {@link javapns.notification.ConnectionToNotificationServer} and {@link javapns.feedback.ConnectionToFeedbackServer}.
 *
 * @author Sylvain Pedneault
 */
public abstract class ConnectionToAppleServer {
  /* PKCS12 */
  public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";
  /* JKS */
  public static final String KEYSTORE_TYPE_JKS = "JKS";
  private static final Logger logger = LoggerFactory.getLogger(ConnectionToAppleServer.class);
  /* The algorithm used by KeyManagerFactory */
  private static final String ALGORITHM = KeyManagerFactory.getDefaultAlgorithm();

  /* The protocol used to create the SSLSocket */
  private static final String PROTOCOL = "TLS";

  private final AppleServer server;

  private KeyStore keyStore;
  private SSLSocketFactory socketFactory;

  /**
   * Builds a connection to an Apple server.
   *
   * @param server connection details
   * @throws KeystoreException thrown if an error occurs when loading the keystore
   */
  protected ConnectionToAppleServer(final AppleServer server) throws KeystoreException {
    this.server = server;
    this.keyStore = KeystoreManager.loadKeystore(server);
  }

  /**
   * Builds a connection to an Apple server.
   *
   * @param server   connection details
   * @param keystore
   */
  protected ConnectionToAppleServer(final AppleServer server, final KeyStore keystore) {
    this.server = server;
    this.keyStore = keystore;
  }

  public AppleServer getServer() {
    return server;
  }

  private KeyStore getKeystore() {
    return keyStore;
  }

  public void setKeystore(final KeyStore ks) {
    this.keyStore = ks;
  }

  /**
   * Generic SSLSocketFactory builder
   *
   * @param trustManagers
   * @return SSLSocketFactory
   * @throws KeystoreException
   */
  private SSLSocketFactory createSSLSocketFactoryWithTrustManagers(final TrustManager[] trustManagers) throws KeystoreException {
    logger.debug("Creating SSLSocketFactory");
    // Get a KeyManager and initialize it
    try {
      final KeyStore keystore = getKeystore();
      final KeyManagerFactory kmf = KeyManagerFactory.getInstance(ALGORITHM);
      try {
        final char[] password = KeystoreManager.getKeystorePasswordForSSL(server);
        kmf.init(keystore, password);
      } catch (Exception e) {
        e = KeystoreManager.wrapKeystoreException(e);
        throw e;
      }

      // Get the SSLContext to help create SSLSocketFactory
      final SSLContext sslc = SSLContext.getInstance(PROTOCOL);
      sslc.init(kmf.getKeyManagers(), trustManagers, null);

      return sslc.getSocketFactory();
    } catch (final Exception e) {
      throw new KeystoreException("Keystore exception: " + e.getMessage(), e);
    }
  }

  public abstract String getServerHost();

  protected abstract int getServerPort();

  /**
   * Return a SSLSocketFactory for creating sockets to communicate with Apple.
   *
   * @return SSLSocketFactory
   * @throws KeystoreException
   */
  private SSLSocketFactory createSSLSocketFactory() throws KeystoreException {
    return createSSLSocketFactoryWithTrustManagers(new TrustManager[]{new ServerTrustingTrustManager()});
  }

  private SSLSocketFactory getSSLSocketFactory() throws KeystoreException {
    if (socketFactory == null) {
      socketFactory = createSSLSocketFactory();
    }
    return socketFactory;
  }

  /**
   * Create a SSLSocket which will be used to send data to Apple
   *
   * @return the SSLSocket
   * @throws KeystoreException
   * @throws CommunicationException
   */
  public SSLSocket getSSLSocket() throws KeystoreException, CommunicationException {
    final SSLSocketFactory sslSocketFactory = getSSLSocketFactory();
    logger.debug("Creating SSLSocket to " + getServerHost() + ":" + getServerPort());

    try {
      if (ProxyManager.isUsingProxy(server)) {
        return tunnelThroughProxy(sslSocketFactory);
      } else {
        return (SSLSocket) sslSocketFactory.createSocket(getServerHost(), getServerPort());
      }
    } catch (final Exception e) {
      throw new CommunicationException("Communication exception: " + e, e);
    }
  }

  private SSLSocket tunnelThroughProxy(final SSLSocketFactory socketFactory) throws IOException {
    final SSLSocket socket;

    // If a proxy was set, tunnel through the proxy to create the connection
    final String tunnelHost = ProxyManager.getProxyHost(server);
    final Integer tunnelPort = ProxyManager.getProxyPort(server);

    final Socket tunnel = new Socket(tunnelHost, tunnelPort);
    doTunnelHandshake(tunnel, getServerHost(), getServerPort());

    /* overlay the tunnel socket with SSL */
    socket = (SSLSocket) socketFactory.createSocket(tunnel, getServerHost(), getServerPort(), true);

    /* register a callback for handshaking completion event */
    socket.addHandshakeCompletedListener(event -> {
      logger.debug("Handshake finished!");
      logger.debug("\t CipherSuite:" + event.getCipherSuite());
      logger.debug("\t SessionId " + event.getSession());
      logger.debug("\t PeerHost " + event.getSession().getPeerHost());
    });

    return socket;
  }

  private void doTunnelHandshake(final Socket tunnel, final String host, final int port) throws IOException {

    final OutputStream out = tunnel.getOutputStream();

    final String msg = "CONNECT " + host + ":" + port + " HTTP/1.0\n" + "User-Agent: BoardPad Server" + "\r\n\r\n";
    byte b[];
    try { //We really do want ASCII7 -- the http protocol doesn't change with locale.
      b = msg.getBytes("ASCII7");
    } catch (final UnsupportedEncodingException ignored) { //If ASCII7 isn't there, something serious is wrong, but Paranoia Is Good (tm)
      b = msg.getBytes();
    }
    out.write(b);
    out.flush();

    // We need to store the reply so we can create a detailed error message to the user.
    final byte[] reply = new byte[200];
    int replyLen = 0;
    int newlinesSeen = 0;
    boolean headerDone = false; //Done on first newline

    final InputStream in = tunnel.getInputStream();

    while (newlinesSeen < 2) {
      final int i = in.read();
      if (i < 0) {
        throw new IOException("Unexpected EOF from proxy");
      }
      if (i == '\n') {
        headerDone = true;
        ++newlinesSeen;
      } else if (i != '\r') {
        newlinesSeen = 0;
        if (!headerDone && replyLen < reply.length) {
          reply[replyLen++] = (byte) i;
        }
      }
    }

    /*
     * Converting the byte array to a string is slightly wasteful
     * in the case where the connection was successful, but it's
     * insignificant compared to the network overhead.
     */
    String replyStr;
    try {
      replyStr = new String(reply, 0, replyLen, "ASCII7");
    } catch (final UnsupportedEncodingException ignored) {
      replyStr = new String(reply, 0, replyLen);
    }

    /* We check for Connection Established because our proxy returns HTTP/1.1 instead of 1.0 */
    if (!replyStr.toLowerCase().contains("200 connection established")) {
      throw new IOException("Unable to tunnel through. Proxy returns \"" + replyStr + "\"");
    }

    /* tunneling Handshake was successful! */
  }
}
