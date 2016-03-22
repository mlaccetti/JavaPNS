package javapns.notification;

import javapns.communication.AppleServer;

/**
 * Interface representing a connection to an Apple Notification Server
 *
 * @author Sylvain Pedneault
 */
public interface AppleNotificationServer extends AppleServer {
  String PRODUCTION_HOST = "gateway.push.apple.com";
  int PRODUCTION_PORT = 2195;

  String DEVELOPMENT_HOST = "gateway.sandbox.push.apple.com";
  int DEVELOPMENT_PORT = 2195;

  String getNotificationServerHost();

  int getNotificationServerPort();
}
