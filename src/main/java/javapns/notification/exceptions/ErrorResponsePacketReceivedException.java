package javapns.notification.exceptions;

import javapns.notification.ResponsePacket;

/**
 * Specific exception indicating that the library received an error-response packet from Apple.
 * <p/>
 * You do not need to catch this exception specifically, as it will be put in a
 * PushedNotification object as the exception that caused a push notification to having failed.
 *
 * @author Sylvain Pedneault
 */
@SuppressWarnings("serial")
public class ErrorResponsePacketReceivedException extends Exception {

  private ResponsePacket packet;

  public ErrorResponsePacketReceivedException(ResponsePacket packet) {
    super(String.format("An error response packet was received from the APNS server: %s", packet.getMessage()));
    this.packet = packet;
  }

  public ResponsePacket getPacket() {
    return packet;
  }

}
