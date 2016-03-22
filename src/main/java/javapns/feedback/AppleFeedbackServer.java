package javapns.feedback;

import javapns.communication.AppleServer;

/**
 * Interface representing a connection to an Apple Feedback Server
 *
 * @author Sylvain Pedneault
 */
public interface AppleFeedbackServer extends AppleServer {
  String PRODUCTION_HOST = "feedback.push.apple.com";
  int PRODUCTION_PORT = 2196;

  String DEVELOPMENT_HOST = "feedback.sandbox.push.apple.com";
  int DEVELOPMENT_PORT = 2196;

  String getFeedbackServerHost();

  int getFeedbackServerPort();
}
