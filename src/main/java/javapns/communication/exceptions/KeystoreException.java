package javapns.communication.exceptions;

/**
 * Thrown when we try to contact Apple with an invalid keystore format.
 *
 * @author Sylvain Pedneault
 */

public class KeystoreException extends Exception {

  private static final long serialVersionUID = 2549063865160633139L;

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public KeystoreException(final String message) {
    super(message);
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public KeystoreException(final String message, final Exception cause) {
    super(message, cause);
  }

}
