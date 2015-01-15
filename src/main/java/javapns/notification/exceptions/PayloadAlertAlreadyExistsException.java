package javapns.notification.exceptions;

import javapns.json.JSONException;

/**
 * Specific exception indicating that the library detected an attempt to insert two alert messages
 * or objects into the same payload.
 * <p/>
 * Methods in the PushNotification class which cause an alert entry to be added to the payload
 * (addAlert, addCustomAlert__, etc.) will throw this exception (upcasted as a JSONException) if
 * the library detects that your action will cause two alert keys to be added to the payload.
 * <p/>
 * You do not need to catch this exception specifically, as catching its parent JSONException will
 * catch a variety of payload construction-related exeptions including this one.
 *
 * @author Sylvain Pedneault
 */
@SuppressWarnings("serial")
public class PayloadAlertAlreadyExistsException extends JSONException {

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
  public PayloadAlertAlreadyExistsException(String message) {
    super(message);
  }

}
