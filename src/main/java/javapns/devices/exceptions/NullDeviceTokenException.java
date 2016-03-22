package javapns.devices.exceptions;

/**
 * Thrown when the given token is null
 *
 * @author Maxime Peron
 */

public class NullDeviceTokenException extends Exception {

  private static final long serialVersionUID = 208339461070934305L;
  /* Custom message for this exception */
  private final String message;

  /**
   * Constructor
   */
  public NullDeviceTokenException() {
    this.message = "Client already exists";
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public NullDeviceTokenException(final String message) {
    this.message = message;
  }

  /**
   * String representation
   */
  public String toString() {
    return this.message;
  }
}
