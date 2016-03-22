package javapns.test;

import javapns.communication.KeystoreManager;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;

class TestFoundation {
  static boolean verifyCorrectUsage(final Class testClass, final String[] argsProvided, final String... argsRequired) {
    if (argsProvided == null) {
      return true;
    }
    final int numberOfArgsRequired = countArgumentsRequired(argsRequired);
    if (argsProvided.length < numberOfArgsRequired) {
      final String message = getUsageMessage(testClass, argsRequired);
      System.out.println(message);
      return false;
    }
    return true;
  }

  private static String getUsageMessage(final Class testClass, final String... argsRequired) {
    final StringBuilder message = new StringBuilder("Usage: ");
    message.append("java -cp \"<required libraries>\" ");
    message.append(testClass.getName());
    for (final String argRequired : argsRequired) {
      final boolean optional = argRequired.startsWith("[");
      if (optional) {
        message.append(" [");
        message.append(argRequired.substring(1, argRequired.length() - 1));
        message.append("]");
      } else {
        message.append(" <");
        message.append(argRequired);
        message.append(">");
      }
    }
    return message.toString();
  }

  private static int countArgumentsRequired(final String... argsRequired) {
    int numberOfArgsRequired = 0;
    for (final String argRequired : argsRequired) {
      if (argRequired.startsWith("[")) {
        break;
      }
      numberOfArgsRequired++;
    }
    return numberOfArgsRequired;
  }

  /**
   * Validate a keystore reference and print the results to the console.
   *
   * @param keystoreReference a reference to or an actual keystore
   * @param password          password for the keystore
   * @param production        service to use
   */
  static void verifyKeystore(final Object keystoreReference, final String password, final boolean production) {
    try {
      System.out.print("Validating keystore reference: ");
      KeystoreManager.validateKeystoreParameter(keystoreReference);
      System.out.println("VALID  (keystore was found)");
    } catch (final Exception e) {
      e.printStackTrace();
    }
    if (password != null) {
      try {
        System.out.print("Verifying keystore content: ");
        final AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystoreReference, password, production);
        KeystoreManager.verifyKeystoreContent(server, keystoreReference);
        System.out.println("VERIFIED  (no common mistakes detected)");
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
  }
}
