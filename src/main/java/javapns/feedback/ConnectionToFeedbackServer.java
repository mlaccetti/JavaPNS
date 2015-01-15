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

  public ConnectionToFeedbackServer(AppleFeedbackServer feedbackServer) throws KeystoreException {
    super(feedbackServer);
  }

  public ConnectionToFeedbackServer(AppleNotificationServer server, KeyStore keystore) {
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
