package javapns.notification.exceptions;

import org.json.JSONException;

/**
 * Thrown when a payload exceeds the maximum size allowed.
 *
 * @author Sylvain Pedneault
 */

public class PayloadAlertAlreadyExistsException extends JSONException {

  private static final long serialVersionUID = -4514511954076864373L;

  /**
   * Default constructor
   */
  public PayloadAlertAlreadyExistsException() {
    super("Payload alert already exists");
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public PayloadAlertAlreadyExistsException(final String message) {
    super(message);
  }

}
