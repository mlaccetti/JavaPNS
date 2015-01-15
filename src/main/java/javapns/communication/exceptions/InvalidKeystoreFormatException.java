package javapns.communication.exceptions;

/**
 * Specific exception indicating that the library was not able to connect to APNS servers,
 * most likely because the keystore you provided might not be in the required format.
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
   * @param message The custom message
   */
  public InvalidKeystoreFormatException(String message) {
    super(message);
  }
}