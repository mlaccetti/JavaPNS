package javapns.notification;

import javapns.communication.ConnectionToAppleServer;
import javapns.communication.exceptions.KeystoreException;

import java.security.KeyStore;

/**
 * Connection details specific to the Notification Service.
 *
 * @author Sylvain Pedneault
 */
public class ConnectionToNotificationServer extends ConnectionToAppleServer {
  public ConnectionToNotificationServer(final AppleNotificationServer server) throws KeystoreException {
    super(server);
  }

  public ConnectionToNotificationServer(final AppleNotificationServer server, final KeyStore keystore) throws KeystoreException {
    super(server, keystore);
  }

  @Override
  public String getServerHost() {
    return ((AppleNotificationServer) getServer()).getNotificationServerHost();
  }

  @Override
  public int getServerPort() {
    return ((AppleNotificationServer) getServer()).getNotificationServerPort();
  }

}
