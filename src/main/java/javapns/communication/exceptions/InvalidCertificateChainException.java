package javapns.communication.exceptions;

/**
 * Thrown when we try to contact Apple with an invalid keystore or certificate chain.
 *
 * @author Sylvain Pedneault
 */
public class InvalidCertificateChainException extends KeystoreException {

  private static final long serialVersionUID = -1978821654637371922L;

  /**
   * Constructor
   */
  public InvalidCertificateChainException() {
    super("Invalid certificate chain!  Verify that the keystore you provided was produced according to specs...");
  }

  /**
   * Constructor with custom message
   *
   * @param message
   */
  public InvalidCertificateChainException(final String message) {
    super("Invalid certificate chain (" + message + ")!  Verify that the keystore you provided was produced according to specs...");
  }

}
