package javapns.notification.exceptions;

import javapns.json.JSONException;

/**
 * Specific exception indicating that a payload would probably exceed the maximum size allowed
 * if the current property addition were to complete, but thrown only if you explicitely invoked
 * payload.setPayloadSizeEstimatedWhenAdding(true) before calling that add___ method.
 * <p/>
 * Since this feature is not enabled by default, you do not need to worry about this
 * exception unless you decide to enable it manually.
 * <p/>
 * Further more, you do not need to catch this exception specifically, as catching its parent
 * JSONException will catch a variety of payload construction-related exeptions including this one.
 *
 * @author Sylvain Pedneault
 */
@SuppressWarnings("serial")
public class PayloadMaxSizeProbablyExceededException extends JSONException {

  /**
   * Default constructor
   */
  public PayloadMaxSizeProbablyExceededException() {
    super("Total payload size will most likely exceed allowed limit");
  }

  public PayloadMaxSizeProbablyExceededException(int maxSize) {
    super(String.format("Total payload size will most likely exceed allowed limit (%s bytes max)", maxSize));
  }

  public PayloadMaxSizeProbablyExceededException(int maxSize, int estimatedSize) {
    super(String.format("Total payload size will most likely exceed allowed limit (estimated to become %s bytes, limit is %s)", estimatedSize, maxSize));
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public PayloadMaxSizeProbablyExceededException(String message) {
    super(message);
  }

}
