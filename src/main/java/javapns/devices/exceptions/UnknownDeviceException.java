package javapns.devices.exceptions;

/**
 * Thrown when we try to retrieve a device that doesn't exist
 *
 * @author Maxime Peron
 */

public class UnknownDeviceException extends Exception {

  private static final long serialVersionUID = -322193098126184434L;
  /* Custom message for this exception */
  private final String message;

  /**
   * Constructor
   */
  public UnknownDeviceException() {
    this.message = "Unknown client";
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public UnknownDeviceException(final String message) {
    this.message = message;
  }

  /**
   * String representation
   */
  public String toString() {
    return this.message;
  }
}
