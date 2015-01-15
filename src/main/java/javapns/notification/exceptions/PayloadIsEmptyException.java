package javapns.notification.exceptions;

/**
 * Specific exception indicating that you attempted to push a payload that contained
 * no property whatsoever.
 * <p/>
 * This may happen if for example you create an empty payload using PushNotificationPayload.complex()
 * or if you instantiate a payload object directly and do not invoke any of its methods to
 * specify what the payload should contain (like addAlert).
 * <p/>
 * You do not need to catch this exception specifically, as it will be put in a
 * PushedNotification object as the exception that caused a push notification to having failed.
 *
 * @author Sylvain Pedneault
 */
@SuppressWarnings("serial")
public class PayloadIsEmptyException extends Exception {

  public PayloadIsEmptyException() {
    super("Payload is empty");
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public PayloadIsEmptyException(String message) {
    super(message);
  }

}
