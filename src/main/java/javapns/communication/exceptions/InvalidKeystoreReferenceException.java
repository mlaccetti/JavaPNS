package javapns.communication.exceptions;

/**
 * Thrown when we try to contact Apple with an invalid keystore format.
 *
 * @author Sylvain Pedneault
 */

public class InvalidKeystoreReferenceException extends KeystoreException {

  private static final long serialVersionUID = 3144387163593035745L;

  /**
   * Constructor
   */
  public InvalidKeystoreReferenceException() {
    super("Invalid keystore parameter.  Must be InputStream, File, String (as a file path), or byte[].");
  }

  /**
   * Constructor with custom message
   *
   * @param keystore
   */
  public InvalidKeystoreReferenceException(final Object keystore) {
    super("Invalid keystore parameter (" + keystore + ").  Must be InputStream, File, String (as a file path), or byte[].");
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public InvalidKeystoreReferenceException(final String message) {
    super(message);
  }
}
