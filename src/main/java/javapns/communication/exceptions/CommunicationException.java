package javapns.communication.exceptions;

public class CommunicationException extends Exception {
  private static final long serialVersionUID = 1286560293829685555L;

  public CommunicationException(final String message, final Exception cause) {
    super(message, cause);
  }
}
