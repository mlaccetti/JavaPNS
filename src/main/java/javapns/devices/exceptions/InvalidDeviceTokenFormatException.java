package javapns.devices.exceptions;

/**
 * Specific exception indicating that the library detected a malformed device token.
 * <p/>
 * JavaPNS accepts 64-bytes device tokens in hexadecimal form.
 * The 32-bytes native form is NOT accepted.
 * <p/>
 * The BasicDevice constructor throws this exception if you attempt to construct
 * one using a malformed device token.
 * <p/>
 * Otherwise, you do not typically need to catch this exception specifically,
 * as it will be put in a PushedNotification object as the exception that caused
 * a push notification to having failed.
 *
 * @author Sylvain Pedneault
 */
@SuppressWarnings("serial")
public class InvalidDeviceTokenFormatException extends Exception {

  public InvalidDeviceTokenFormatException(String message) {
    super(message);
  }

  public InvalidDeviceTokenFormatException(String token, String problem) {
    super(String.format("Device token cannot be parsed, most likely because it contains invalid hexadecimal characters: %s in %s", problem, token));
  }

}
