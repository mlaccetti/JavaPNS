package javapns.test;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;

import java.util.List;

/**
 * A command-line test facility for the Feedback Service.
 * <p>Example:  <code>java -cp "[required libraries]" FeedbackTest keystore.p12 mypass</code></p>
 * <p>
 * <p>By default, this test uses the sandbox service.  To switch, add "production" as a third parameter:</p>
 * <p>Example:  <code>java -cp "[required libraries]" FeedbackTest keystore.p12 mypass production</code></p>
 *
 * @author Sylvain Pedneault
 */
class FeedbackTest extends TestFoundation {
  private FeedbackTest() {
    // empty
  }

  /**
   * Execute this class from the command line to run tests.
   *
   * @param args
   */
  public static void main(final String[] args) {
    /* Verify that the test is being invoked  */
    if (!verifyCorrectUsage(FeedbackTest.class, args, "keystore-path", "keystore-password", "[production|sandbox]")) {
      return;
    }

    /* Initialize Log4j to print logs to console */
    configureBasicLogging();

    /* Get a list of inactive devices */
    feedbackTest(args);
  }

  /**
   * Retrieves a list of inactive devices from the Feedback service.
   *
   * @param args
   */
  private static void feedbackTest(final String[] args) {
    final String keystore = args[0];
    final String password = args[1];
    final boolean production = args.length >= 3 && args[2].equalsIgnoreCase("production");
    try {
      final List<Device> devices = Push.feedback(keystore, password, production);

      for (final Device device : devices) {
        System.out.println("Inactive device: " + device.getToken());
      }
    } catch (final CommunicationException | KeystoreException e) {
      e.printStackTrace();
    }
  }

}
