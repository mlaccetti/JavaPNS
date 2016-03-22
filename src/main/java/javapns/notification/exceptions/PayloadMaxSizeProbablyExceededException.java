package javapns.notification.exceptions;

import org.json.JSONException;

/**
 * Thrown when a payload is expected to exceed the maximum size allowed after adding a given property.
 * Invoke payload.setPayloadSizeEstimatedWhenAdding(false) to disable this automatic checking.
 *
 * @author Sylvain Pedneault
 */

public class PayloadMaxSizeProbablyExceededException extends JSONException {

  private static final long serialVersionUID = 580227446786047134L;

  /**
   * Default constructor
   */
  public PayloadMaxSizeProbablyExceededException() {
    super("Total payload size will most likely exceed allowed limit");
  }

  public PayloadMaxSizeProbablyExceededException(final int maxSize) {
    super(String.format("Total payload size will most likely exceed allowed limit (%s bytes max)", maxSize));
  }

  public PayloadMaxSizeProbablyExceededException(final int maxSize, final int estimatedSize) {
    super(String.format("Total payload size will most likely exceed allowed limit (estimated to become %s bytes, limit is %s)", estimatedSize, maxSize));
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public PayloadMaxSizeProbablyExceededException(final String message) {
    super(message);
  }

}
