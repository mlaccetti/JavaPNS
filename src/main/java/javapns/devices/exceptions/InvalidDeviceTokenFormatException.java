package javapns.devices.exceptions;

/**
 * Thrown when a device token cannot be parsed (invalid format).
 *
 * @author Sylvain Pedneault
 */

public class InvalidDeviceTokenFormatException extends Exception {

  private static final long serialVersionUID = -8571997399252125457L;

  public InvalidDeviceTokenFormatException(final String message) {
    super(message);
  }

  public InvalidDeviceTokenFormatException(final String token, final String problem) {
    super(String.format("Device token cannot be parsed, most likely because it contains invalid hexadecimal characters: %s in %s", problem, token));
  }

}
