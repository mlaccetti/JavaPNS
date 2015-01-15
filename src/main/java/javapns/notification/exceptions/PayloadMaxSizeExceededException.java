package javapns.notification.exceptions;

/**
 * Specific exception indicating that a payload exceeds the maximum size allowed.
 * <p/>
 * The maximum size allowed for PushNotification objects is the actual number
 * explicitely documented in Apple's APNS specifications.
 * <p/>
 * You do not need to catch this exception specifically, as it will be put in a
 * PushedNotification object as the exception that caused a push notification to having failed.
 *
 * @author Sylvain Pedneault
 */
@SuppressWarnings("serial")
public class PayloadMaxSizeExceededException extends Exception {

  /**
   * Default constructor
   */
  public PayloadMaxSizeExceededException() {
    super("Total payload size exceeds allowed limit");
  }

  public PayloadMaxSizeExceededException(int maxSize) {
    super(String.format("Total payload size exceeds allowed limit (%s bytes max)", maxSize));
  }

  public PayloadMaxSizeExceededException(int maxSize, int currentSize) {
    super(String.format("Total payload size exceeds allowed limit (payload is %s bytes, limit is %s)", currentSize, maxSize));
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public PayloadMaxSizeExceededException(String message) {
    super(message);
  }

}
