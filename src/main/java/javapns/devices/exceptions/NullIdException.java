package javapns.devices.exceptions;

/**
 * Thrown when the given id is null
 *
 * @author Maxime Peron
 */

public class NullIdException extends Exception {

  private static final long serialVersionUID = -2842793759970312540L;
  /* Custom message for this exception */
  private final String message;

  /**
   * Constructor
   */
  public NullIdException() {
    this.message = "Client already exists";
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public NullIdException(final String message) {
    this.message = message;
  }

  /**
   * String representation
   */
  public String toString() {
    return this.message;
  }
}
