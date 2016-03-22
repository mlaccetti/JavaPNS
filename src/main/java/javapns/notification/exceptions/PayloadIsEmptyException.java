package javapns.notification.exceptions;

/**
 * Thrown when a payload is empty.
 *
 * @author Sylvain Pedneault
 */

public class PayloadIsEmptyException extends Exception {

  private static final long serialVersionUID = 8142083854784121700L;

  public PayloadIsEmptyException() {
    super("Payload is empty");
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public PayloadIsEmptyException(final String message) {
    super(message);
  }

}
