package javapns.feedback;

import javapns.communication.ConnectionToAppleServer;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.AppleNotificationServer;

import java.security.KeyStore;

/**
 * Class representing a connection to a specific Feedback Server.
 *
 * @author Sylvain Pedneault
 */
public class ConnectionToFeedbackServer extends ConnectionToAppleServer {
  public ConnectionToFeedbackServer(final AppleFeedbackServer feedbackServer) throws KeystoreException {
    super(feedbackServer);
  }

  public ConnectionToFeedbackServer(final AppleNotificationServer server, final KeyStore keystore) throws KeystoreException {
    super(server, keystore);
  }

  @Override
  public String getServerHost() {
    return ((AppleFeedbackServer) getServer()).getFeedbackServerHost();
  }

  @Override
  public int getServerPort() {
    return ((AppleFeedbackServer) getServer()).getFeedbackServerPort();
  }
}
