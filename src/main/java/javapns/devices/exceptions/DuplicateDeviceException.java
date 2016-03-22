package javapns.devices.exceptions;

/**
 * Thrown when a Device already exist and we try to add it a second time
 *
 * @author Maxime Peron
 */

public class DuplicateDeviceException extends Exception {

  private static final long serialVersionUID = -7116507420722667479L;
  /* Custom message for this exception */
  private final String message;

  /**
   * Constructor
   */
  public DuplicateDeviceException() {
    this.message = "Client already exists";
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public DuplicateDeviceException(final String message) {
    this.message = message;
  }

  /**
   * String representation
   */
  public String toString() {
    return this.message;
  }
}
