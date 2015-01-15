package javapns.communication.exceptions;

/**
 * Specific exception indicating that the library was not able to connect to APNS servers,
 * most likely because the certificate chain contained in the keystore you provided is invalid.
 * <p/>
 * This is ALMOST ALWAYS caused by not having followed precisely the certificate
 * preparation procedure described on the JavaPNS project web site.
 * <p/>
 * It should be noted that this exception is JavaPNS' best interpretation of Java SSL's
 * sometimes cryptic exceptions.  The true source of the error might be more complex,
 * but this exception is our best guess at what is going on based on past support cases.
 * <p/>
 * You do not need to catch this exception specifically, as you should already be catching
 * its parent KeystoreException (which is thrown by most push methods).
 *
 * @author Sylvain Pedneault
 */
public class InvalidCertificateChainException extends KeystoreException {
  private static final long serialVersionUID = 8627618822526468032L;

  /**
   * Constructor
   */
  public InvalidCertificateChainException() {
    super("Invalid certificate chain!  Verify that the keystore you provided was produced according to specs...");
  }

  /**
   * Constructor with custom message
   *
   * @param message The custom message
   */
  public InvalidCertificateChainException(String message) {
    super(String.format("Invalid certificate chain (%s)!  Verify that the keystore you provided was produced according to specs...", message));
  }

  /**
   * Constructor with custom message and upstream cause
   *
   * @param message The custom message
   * @param cause   The upstream cause
   */
  public InvalidCertificateChainException(String message, Exception cause) {
    super(String.format("Invalid certificate chain (%s)!  Verify that the keystore you provided was produced according to specs...", message), cause);
  }
}