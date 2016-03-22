package javapns.communication.exceptions;

/**
 * Thrown when we try to contact Apple with an invalid keystore format.
 *
 * @author Sylvain Pedneault
 */
public class InvalidKeystoreFormatException extends KeystoreException {

  private static final long serialVersionUID = 8822634206752412121L;

  /**
   * Constructor
   */
  public InvalidKeystoreFormatException() {
    super("Invalid keystore format!  Make sure it is PKCS12...");
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public InvalidKeystoreFormatException(final String message) {
    super(message);
  }

}
