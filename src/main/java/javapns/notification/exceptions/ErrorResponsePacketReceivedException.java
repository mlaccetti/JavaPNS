package javapns.notification.exceptions;

import javapns.notification.ResponsePacket;

/**
 * Thrown when an error response packet was received from an APNS server.
 *
 * @author Sylvain Pedneault
 */

public class ErrorResponsePacketReceivedException extends Exception {

  private static final long serialVersionUID = 5798868422603574079L;
  private final ResponsePacket packet;

  public ErrorResponsePacketReceivedException(final ResponsePacket packet) {
    super(String.format("An error response packet was received from the APNS server: %s", packet.getMessage()));
    this.packet = packet;
  }

  public ResponsePacket getPacket() {
    return packet;
  }

}
