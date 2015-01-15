package javapns.communication.exceptions;

/**
 * General exception indicating that the library experienced some problem related to
 * your keystore, usually when attempting to read and load it.
 * <p/>
 * This exception needs to be caught by developers using JavaPNS, as it is one of
 * the few that are explicitly thrown by most push methods.
 * <p/>
 * Although this is a general exception, much more details can be found out by
 * examining the cause exception that this general exception encapsulates.
 *
 * @author Sylvain Pedneault
 */
public class KeystoreException extends Exception {
  private static final long serialVersionUID = 2549063865160633139L;

  /**
   * Constructor with custom message
   *
   * @param message The custom message
   */
  public KeystoreException(String message) {
    super(message);
  }

  /**
   * Constructor with custom message and upstream exception
   *
   * @param message The custom message
   * @param cause The upstream cause
   */
  public KeystoreException(String message, Exception cause) {
    super(message, cause);
  }
}