package javapns.communication.exceptions;

/**
 * Thrown when we try to contact Apple with an invalid password for the keystore.
 *
 * @author Sylvain Pedneault
 */

public class InvalidKeystorePasswordException extends KeystoreException {

  private static final long serialVersionUID = 5973743951334025887L;

  /**
   * Constructor
   */
  public InvalidKeystorePasswordException() {
    super("Invalid keystore password!  Verify settings for connecting to Apple...");
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public InvalidKeystorePasswordException(final String message) {
    super(message);
  }

}
