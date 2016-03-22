package javapns.test;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.*;
import javapns.notification.transmission.NotificationThread;
import javapns.notification.transmission.NotificationThreads;
import javapns.notification.transmission.PushQueue;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Specific test cases intended for the project's developers.
 *
 * @author Sylvain Pedneault
 */
class SpecificNotificationTests extends TestFoundation {
  private SpecificNotificationTests() {
    // empty
  }

  /**
   * Execute this class from the command line to run tests.
   *
   * @param args
   */
  public static void main(final String[] args) {
    /* Verify that the test is being invoked  */
    if (!verifyCorrectUsage(NotificationTest.class, args, "keystore-path", "keystore-password", "device-token", "[production|sandbox]", "[test-name]")) {
      return;
    }

    /* Initialize Log4j to print logs to console */
    configureBasicLogging();

    /* Push an alert */
    runTest(args);
  }

  /**
   * Push a test notification to a device, given command-line parameters.
   *
   * @param args
   */
  private static void runTest(final String[] args) {
    final String keystore = args[0];
    final String password = args[1];
    final String token = args[2];
    final boolean production = args.length >= 4 && args[3].equalsIgnoreCase("production");

    String testName = args.length >= 5 ? args[4] : null;
    if (testName == null || testName.length() == 0) {
      testName = "default";
    }

    try {
      SpecificNotificationTests.class.getDeclaredMethod("test_" + testName, String.class, String.class, String.class, boolean.class).invoke(null, keystore, password, token, production);
    } catch (final NoSuchMethodException e) {
      System.out.println(String.format("Error: test '%s' not found.  Test names are case-sensitive", testName));
    } catch (final Exception e) {
      (e.getCause() != null ? e.getCause() : e).printStackTrace();
    }
  }

  private static void test_PushHelloWorld(final String keystore, final String password, final String token, final boolean production) throws CommunicationException, KeystoreException {
    final List<PushedNotification> notifications = Push.alert("Hello World!", keystore, password, production, token);
    NotificationTest.printPushedNotifications(notifications);
  }

  private static void test_Issue74(final String keystore, final String password, final String token, final boolean production) {
    try {
      System.out.println("");
      System.out.println("TESTING 257-BYTES PAYLOAD WITH SIZE ESTIMATION ENABLED");
      /* Expected result: PayloadMaxSizeProbablyExceededException when the alert is added to the payload */
      pushSpecificPayloadSize(keystore, password, token, production, true, 257);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    try {
      System.out.println("");
      System.out.println("TESTING 257-BYTES PAYLOAD WITH SIZE ESTIMATION DISABLED");
      /* Expected result: PayloadMaxSizeExceededException when the payload is pushed */
      pushSpecificPayloadSize(keystore, password, token, production, false, 257);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    try {
      System.out.println("");
      System.out.println("TESTING 256-BYTES PAYLOAD");
      /* Expected result: no exception */
      pushSpecificPayloadSize(keystore, password, token, production, false, 256);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static void test_Issue75(final String keystore, final String password, final String token, final boolean production) {
    try {
      System.out.println("");
      System.out.println("TESTING 257-BYTES PAYLOAD WITH SIZE ESTIMATION ENABLED");
      final NewsstandNotificationPayload payload = NewsstandNotificationPayload.contentAvailable();
      debugPayload(payload);

      final List<PushedNotification> notifications = Push.payload(payload, keystore, password, production, token);
      NotificationTest.printPushedNotifications(notifications);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static void test_Issue82(final String keystore, final String password, final String token, final boolean production) {
    try {
      System.out.println("");
      final Payload payload = PushNotificationPayload.test();

      System.out.println("TESTING ISSUE #82 PART 1");
      final List<PushedNotification> notifications = Push.payload(payload, keystore, password, production, 1, token);
      NotificationTest.printPushedNotifications(notifications);
      System.out.println("ISSUE #82 PART 1 TESTED");

      System.out.println("TESTING ISSUE #82 PART2");
      final AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
      final NotificationThread thread = new NotificationThread(new PushNotificationManager(), server, payload, token);
      thread.setListener(NotificationTest.DEBUGGING_PROGRESS_LISTENER);
      thread.start();
      System.out.println("ISSUE #82 PART 2 TESTED");

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static void test_Issue87(final String keystore, final String password, final String token, final boolean production) {
    try {
      System.out.println("TESTING ISSUES #87 AND #88");

      final InputStream ks = new BufferedInputStream(new FileInputStream(keystore));
      final PushQueue queue = Push.queue(ks, password, false, 3);
      queue.start();
      queue.add(PushNotificationPayload.test(), token);
      queue.add(PushNotificationPayload.test(), token);
      queue.add(PushNotificationPayload.test(), token);
      queue.add(PushNotificationPayload.test(), token);
      Thread.sleep(10000);
      final List<Exception> criticalExceptions = queue.getCriticalExceptions();
      for (final Exception exception : criticalExceptions) {
        exception.printStackTrace();
      }
      Thread.sleep(10000);

      List<PushedNotification> pushedNotifications = queue.getPushedNotifications();
      NotificationTest.printPushedNotifications("BEFORE CLEAR:", pushedNotifications);

      queue.clearPushedNotifications();

      pushedNotifications = queue.getPushedNotifications();
      NotificationTest.printPushedNotifications("AFTER CLEAR:", pushedNotifications);

      Thread.sleep(50000);
      System.out.println("ISSUES #87 AND #88 TESTED");

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static void test_Issue88(final String keystore, final String password, final String token, final boolean production) {
    try {
      System.out.println("TESTING ISSUES #88");

      //      List<String> devices = new Vector<String>();
      //      for (int i = 0; i < 5; i++) {
      //        devices.add(token);
      //      }
      //      PushedNotifications notifications = Push.payload(PushNotificationPayload.test(), keystore, password, false, devices);
      final PushQueue queue = Push.queue(keystore, password, false, 1);
      queue.start();
      queue.add(PushNotificationPayload.test(), token);
      queue.add(PushNotificationPayload.test(), token);
      queue.add(PushNotificationPayload.test(), token);
      queue.add(PushNotificationPayload.test(), token);
      Thread.sleep(10000);

      final PushedNotifications notifications = queue.getPushedNotifications();
      NotificationTest.printPushedNotifications(notifications);

      Thread.sleep(5000);
      System.out.println("ISSUES #88 TESTED");

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static void test_Issue99(final String keystore, final String password, final String token, final boolean production) {
    try {
      System.out.println("");
      System.out.println("TESTING ISSUE #99");
      final PushNotificationPayload payload = PushNotificationPayload.complex();
      payload.addCustomAlertBody("Hello World!");
      payload.addCustomAlertActionLocKey(null);
      debugPayload(payload);

      final List<PushedNotification> notifications = Push.payload(payload, keystore, password, production, token);
      NotificationTest.printPushedNotifications(notifications);
      System.out.println("ISSUE #99 TESTED");
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static void test_Issue102(final String keystore, final String password, String token, final boolean production) {
    try {
      System.out.println("");
      System.out.println("TESTING ISSUE #102");
      final int devices = 10000;
      final int threads = 20;
      final boolean simulation = false;
      final String realToken = token;
      token = null;

      try {
        System.out.println("Creating PushNotificationManager and AppleNotificationServer");
        final AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
        System.out.println("Creating payload (simulation mode)");
        //Payload payload = PushNotificationPayload.alert("Hello World!");
        final Payload payload = PushNotificationPayload.test();

        System.out.println("Generating " + devices + " fake devices");
        final List<Device> deviceList = new ArrayList<>(devices);

        //noinspection Duplicates
        for (int i = 0; i < devices; i++) {
          String tokenToUse = token;
          if (tokenToUse == null || tokenToUse.length() != 64) {
            tokenToUse = "123456789012345678901234567890123456789012345678901234567" + (1000000 + i);
          }
          deviceList.add(new BasicDevice(tokenToUse));
        }
        deviceList.add(new BasicDevice(realToken));
        System.out.println("Creating " + threads + " notification threads");
        final NotificationThreads work = new NotificationThreads(server, simulation ? payload.asSimulationOnly() : payload, deviceList, threads);
        //work.setMaxNotificationsPerConnection(10000);
        //System.out.println("Linking notification work debugging listener");
        //work.setListener(DEBUGGING_PROGRESS_LISTENER);

        System.out.println("Starting all threads...");
        final long timestamp1 = System.currentTimeMillis();
        work.start();
        System.out.println("All threads started, waiting for them...");
        work.waitForAllThreads();
        final long timestamp2 = System.currentTimeMillis();
        System.out.println("All threads finished in " + (timestamp2 - timestamp1) + " milliseconds");

        NotificationTest.printPushedNotifications(work.getSuccessfulNotifications());

      } catch (final Exception e) {
        e.printStackTrace();
      }

      //      List<PushedNotification> notifications = Push.payload(payload, keystore, password, production, token);
      //      NotificationTest.printPushedNotifications(notifications);
      System.out.println("ISSUE #102 TESTED");
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static void test_ThreadPoolFeature(final String keystore, final String password, final String token, final boolean production) throws Exception {
    try {
      System.out.println("");
      System.out.println("TESTING THREAD POOL FEATURE");

      final AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
      final NotificationThreads pool = new NotificationThreads(server, 3).start();
      final Device device = new BasicDevice(token);

      System.out.println("Thread pool started and waiting...");

      System.out.println("Sleeping 5 seconds before queuing payloads...");
      Thread.sleep(5 * 1000);

      for (int i = 1; i <= 4; i++) {
        final Payload payload = PushNotificationPayload.alert("Test " + i);
        final NotificationThread threadForPayload = (NotificationThread) pool.add(new PayloadPerDevice(payload, device));
        System.out.println("Queued payload " + i + " to " + threadForPayload.getThreadNumber());
        System.out.println("Sleeping 10 seconds before queuing another payload...");
        Thread.sleep(10 * 1000);
      }
      System.out.println("Sleeping 10 more seconds let threads enough times to push the latest payload...");
      Thread.sleep(10 * 1000);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static void pushSpecificPayloadSize(final String keystore, final String password, final String token, final boolean production, final boolean checkWhenAdding, final int targetPayloadSize) throws CommunicationException, KeystoreException, JSONException {
    final StringBuilder buf = new StringBuilder();
    for (int i = 0; i < targetPayloadSize - 20; i++) {
      buf.append('x');
    }

    final String alertMessage = buf.toString();
    final PushNotificationPayload payload = PushNotificationPayload.complex();
    if (checkWhenAdding) {
      payload.setPayloadSizeEstimatedWhenAdding(true);
    }
    debugPayload(payload);

    final boolean estimateValid = payload.isEstimatedPayloadSizeAllowedAfterAdding("alert", alertMessage);
    System.out.println("Payload size estimated to be allowed: " + (estimateValid ? "yes" : "no"));
    payload.addAlert(alertMessage);
    debugPayload(payload);

    final List<PushedNotification> notifications = Push.payload(payload, keystore, password, production, token);
    NotificationTest.printPushedNotifications(notifications);
  }

  private static void debugPayload(final Payload payload) {
    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    try {
      System.out.println("Payload size: " + payload.getPayloadSize());
    } catch (final Exception e) {
      // empty
    }
    try {
      System.out.println("Payload representation: " + payload);
    } catch (final Exception e) {
      // empty
    }
    System.out.println(payload.isPayloadSizeEstimatedWhenAdding() ? "Payload size is estimated when adding properties" : "Payload size is only checked when it is complete");
    System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
  }

}
