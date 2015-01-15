package javapns.communication;

import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.KeyStore;

/**
 * <h1>Class representing an abstract connection to an Apple server</h1>
 * <p/>
 * Communication protocol differences between Notification and Feedback servers are
 * implemented in {@link javapns.notification.ConnectionToNotificationServer} and {@link javapns.feedback.ConnectionToFeedbackServer}.
 *
 * @author Sylvain Pedneault
 */
public abstract class ConnectionToAppleServer {
  protected static final Logger logger = LoggerFactory.getLogger(ConnectionToAppleServer.class);

  /* PKCS12 */
  public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";

  /* The algorithm used by KeyManagerFactory */
  private static final String ALGORITHM = "sunx509";

  /* The protocol used to create the SSLSocket */
  private static final String PROTOCOL = "TLS";

  private static final String LOCAL_KEYSTORE_TYPE_PROPERTY = "javapns.communication.keystoreType";

  private KeyStore         keyStore;
  private SSLSocketFactory socketFactory;
  private AppleServer      server;

  /**
   * Builds a connection to an Apple server.
   *
   * @param server connection details
   * @throws KeystoreException thrown if an error occurs when loading the keystore
   */
  public ConnectionToAppleServer(AppleServer server) throws KeystoreException {
    this.server = server;
    keyStore = KeystoreManager.loadKeystore(server);
  }

  /**
   * Builds a connection to an Apple server.
   *
   * @param server   connection details
   * @param keystore
   */
  public ConnectionToAppleServer(AppleServer server, KeyStore keystore) {
    this.server = server;
    keyStore = keystore;
  }

  public static String getKeystoreType() {
    String type = System.getProperty(LOCAL_KEYSTORE_TYPE_PROPERTY, KEYSTORE_TYPE_PKCS12);
    if (type == null || type.length() == 0) {
      type = KEYSTORE_TYPE_PKCS12;
    }
    return type;
  }

  public AppleServer getServer() {
    return server;
  }

  public KeyStore getKeystore() {
    return keyStore;
  }

  public void setKeystore(KeyStore ks) {
    keyStore = ks;
  }

  /**
   * Generic SSLSocketFactory builder
   *
   * @param trustManagers Trust managers for the SSLContext
   * @return SSLSocketFactory
   * @throws KeystoreException
   */
  protected SSLSocketFactory createSSLSocketFactoryWithTrustManagers(TrustManager[] trustManagers) throws KeystoreException {
    logger.debug("Creating SSLSocketFactory");
    // Get a KeyManager and initialize it
    try {
      KeyStore keystore = getKeystore();
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(ALGORITHM);
      try {
        char[] password = KeystoreManager.getKeystorePasswordForSSL(server);
        kmf.init(keystore, password);
      } catch (Exception e) {
        e = KeystoreManager.wrapKeystoreException(e);
        throw e;
      }

      // Get the SSLContext to help create SSLSocketFactory
      SSLContext sslc = SSLContext.getInstance(PROTOCOL);
      sslc.init(kmf.getKeyManagers(), trustManagers, null);

      return sslc.getSocketFactory();
    } catch (Exception e) {
      throw new KeystoreException("Keystore exception: " + e.getMessage(), e);
    }
  }

  public abstract String getServerHost();

  public abstract int getServerPort();

  /**
   * Return a SSLSocketFactory for creating sockets to communicate with Apple.
   *
   * @return SSLSocketFactory
   * @throws KeystoreException
   */
  public SSLSocketFactory createSSLSocketFactory() throws KeystoreException {
    return createSSLSocketFactoryWithTrustManagers(new TrustManager[]{new ServerTrustingTrustManager()});
  }

  public SSLSocketFactory getSSLSocketFactory() throws KeystoreException {
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
    SSLSocketFactory socketFactory = getSSLSocketFactory();
    logger.debug("Creating SSLSocket to " + getServerHost() + ":" + getServerPort());

    try {
      if (ProxyManager.isUsingProxy(server)) {
        return tunnelThroughProxy(socketFactory);
      } else {
        return (SSLSocket) socketFactory.createSocket(getServerHost(), getServerPort());
      }
    } catch (Exception e) {
      throw new CommunicationException("Communication exception: " + e, e);
    }
  }

  private SSLSocket tunnelThroughProxy(SSLSocketFactory socketFactory) throws IOException {
    SSLSocket socket;

    // If a proxy was set, tunnel through the proxy to create the connection
    String tunnelHost = ProxyManager.getProxyHost(server);
    Integer tunnelPort = ProxyManager.getProxyPort(server);

    Socket tunnel = new Socket(tunnelHost, tunnelPort);
    doTunnelHandshake(tunnel, getServerHost(), getServerPort());

    /* overlay the tunnel socket with SSL */
    socket = (SSLSocket) socketFactory.createSocket(tunnel, getServerHost(), getServerPort(), true);

    /* register a callback for handshaking completion event */
    socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
      public void handshakeCompleted(HandshakeCompletedEvent event) {
        logger.debug("Handshake finished!");
        logger.debug("\t CipherSuite:" + event.getCipherSuite());
        logger.debug("\t SessionId " + event.getSession());
        logger.debug("\t PeerHost " + event.getSession().getPeerHost());
      }
    });

    return socket;
  }

  private void doTunnelHandshake(Socket tunnel, String host, int port) throws IOException {

    OutputStream out = tunnel.getOutputStream();

    StringBuilder header = new StringBuilder();
    header.append("CONNECT " + host + ":" + port + " HTTP/1.0\n");
    header.append("User-Agent: BoardPad Server\n");
    String authorization = ProxyManager.getProxyAuthorization(server);
    if (authorization != null && authorization.length() > 0) {
      header.append("Proxy-Authorization: " + authorization + "\n");
    }

    header.deleteCharAt(header.lastIndexOf("\n"));
    header.append("\r\n\r\n");

    String msg = header.toString();//"CONNECT " + host + ":" + port + " HTTP/1.0\n" + "User-Agent: BoardPad Server" + "\r\n\r\n";
    byte b[] = null;
    try { //We really do want ASCII7 -- the http protocol doesn't change with locale.
      b = msg.getBytes("ASCII7");
    } catch (UnsupportedEncodingException ignored) { //If ASCII7 isn't there, something serious is wrong, but Paranoia Is Good (tm)
      b = msg.getBytes();
    }
    out.write(b);
    out.flush();

    // We need to store the reply so we can create a detailed error message to the user.
    byte reply[] = new byte[200];
    int replyLen = 0;
    int newlinesSeen = 0;
    boolean headerDone = false; //Done on first newline

    InputStream in = tunnel.getInputStream();

    while (newlinesSeen < 2) {
      int i = in.read();
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
    } catch (UnsupportedEncodingException ignored) {
      replyStr = new String(reply, 0, replyLen);
    }

    /* We check for Connection Established because our proxy returns HTTP/1.1 instead of 1.0 */
    if (replyStr.toLowerCase().indexOf("200") == -1) {
      throw new IOException("Unable to tunnel through. Proxy returns \"" + replyStr + "\"");
    }
    /* tunneling Handshake was successful! */
  }
}