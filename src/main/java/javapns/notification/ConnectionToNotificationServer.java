package javapns.notification;

import java.security.KeyStore;

import javapns.communication.ConnectionToAppleServer;
import javapns.communication.exceptions.KeystoreException;

/**
 * Connection details specific to the Notification Service.
 * 
 * @author Sylvain Pedneault
 */
public class ConnectionToNotificationServer extends ConnectionToAppleServer {
  public ConnectionToNotificationServer(AppleNotificationServer server) throws KeystoreException {
    super(server);
  }

  public ConnectionToNotificationServer(AppleNotificationServer server, KeyStore keystore) {
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