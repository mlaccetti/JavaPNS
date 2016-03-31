package javapns;

import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.Devices;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.feedback.AppleFeedbackServer;
import javapns.feedback.AppleFeedbackServerBasicImpl;
import javapns.feedback.FeedbackServiceManager;
import javapns.notification.*;
import javapns.notification.transmission.NotificationThread;
import javapns.notification.transmission.NotificationThreads;
import javapns.notification.transmission.PushQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Vector;

/**
 * <p>Main class for easily interacting with the Apple Push Notification System</p>
 *
 * <p>This is the best starting point for pushing simple or custom notifications,
 * or for contacting the Feedback Service to cleanup your list of devices.</p>
 *
 * <p>The <b>JavaPNS</b> library also includes more advanced options such as
 * multithreaded transmission, special payloads, and more.
 * See the library's documentation at <a href="http://code.google.com/p/javapns/">http://code.google.com/p/javapns/</a>
 * for more information.</p>
 *
 * @author Sylvain Pedneault
 * @see NotificationThreads
 */
public class Push {

  private static final Logger logger = LoggerFactory.getLogger(Push.class);

  private Push() {
    // empty
  }

  /**
   * Push a simple alert to one or more devices.
   *
   * @param message    the alert message to push.
   * @param keystore   a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password   the keystore's password.
   * @param production true to use Apple's production servers, false to use the sandbox servers.
   * @param devices    a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List} {@link java.lang.String},  {@link javapns.devices.Device Device[]}, {@link java.util.List} {@link javapns.devices.Device},  {@link java.lang.String} or {@link javapns.devices.Device}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  public static PushedNotifications alert(final String message, final Object keystore, final String password, final boolean production, final Object devices) throws CommunicationException, KeystoreException {
    return sendPayload(PushNotificationPayload.alert(message), keystore, password, production, devices);
  }

  /**
   * Push a simple badge number to one or more devices.
   *
   * @param badge      the badge number to push.
   * @param keystore   a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password   the keystore's password.
   * @param production true to use Apple's production servers, false to use the sandbox servers.
   * @param devices    a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List} {@link java.lang.String},  {@link javapns.devices.Device Device[]}, {@link java.util.List} {@link javapns.devices.Device},  {@link java.lang.String} or {@link javapns.devices.Device}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  public static PushedNotifications badge(final int badge, final Object keystore, final String password, final boolean production, final Object devices) throws CommunicationException, KeystoreException {
    return sendPayload(PushNotificationPayload.badge(badge), keystore, password, production, devices);
  }

  /**
   * Push a simple sound name to one or more devices.
   *
   * @param sound      the sound name (stored in the client app) to push.
   * @param keystore   a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password   the keystore's password.
   * @param production true to use Apple's production servers, false to use the sandbox servers.
   * @param devices    a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List} {@link java.lang.String},  {@link javapns.devices.Device Device[]}, {@link java.util.List} {@link javapns.devices.Device},  {@link java.lang.String} or {@link javapns.devices.Device}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  public static PushedNotifications sound(final String sound, final Object keystore, final String password, final boolean production, final Object devices) throws CommunicationException, KeystoreException {
    return sendPayload(PushNotificationPayload.sound(sound), keystore, password, production, devices);
  }

  /**
   * Push a notification combining an alert, a badge and a sound.
   *
   * @param message    the alert message to push (set to null to skip).
   * @param badge      the badge number to push (set to -1 to skip).
   * @param sound      the sound name to push (set to null to skip).
   * @param keystore   a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password   the keystore's password.
   * @param production true to use Apple's production servers, false to use the sandbox servers.
   * @param devices    a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List} {@link java.lang.String},  {@link javapns.devices.Device Device[]}, {@link java.util.List} {@link javapns.devices.Device},  {@link java.lang.String} or {@link javapns.devices.Device}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  public static PushedNotifications combined(final String message, final int badge, final String sound, final Object keystore, final String password, final boolean production, final Object devices) throws CommunicationException, KeystoreException {
    return sendPayload(PushNotificationPayload.combined(message, badge, sound), keystore, password, production, devices);
  }

  /**
   * Push a content-available notification for Newsstand.
   *
   * @param keystore   a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password   the keystore's password.
   * @param production true to use Apple's production servers, false to use the sandbox servers.
   * @param devices    a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List} {@link java.lang.String},  {@link javapns.devices.Device Device[]}, {@link java.util.List} {@link javapns.devices.Device},  {@link java.lang.String} or {@link javapns.devices.Device}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  public static PushedNotifications contentAvailable(final Object keystore, final String password, final boolean production, final Object devices) throws CommunicationException, KeystoreException {
    return sendPayload(NewsstandNotificationPayload.contentAvailable(), keystore, password, production, devices);
  }

  /**
   * Push a special test notification with an alert message containing useful debugging information.
   *
   * @param keystore   a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password   the keystore's password.
   * @param production true to use Apple's production servers, false to use the sandbox servers.
   * @param devices    a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List} {@link java.lang.String},  {@link javapns.devices.Device Device[]}, {@link java.util.List} {@link javapns.devices.Device},  {@link java.lang.String} or {@link javapns.devices.Device}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  public static PushedNotifications test(final Object keystore, final String password, final boolean production, final Object devices) throws CommunicationException, KeystoreException {
    return sendPayload(PushNotificationPayload.test(), keystore, password, production, devices);
  }

  /**
   * Push a preformatted payload to a list of devices.
   *
   * @param payload    a simple or complex payload to push.
   * @param keystore   a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password   the keystore's password.
   * @param production true to use Apple's production servers, false to use the sandbox servers.
   * @param devices    a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List} {@link java.lang.String},  {@link javapns.devices.Device Device[]}, {@link java.util.List} {@link javapns.devices.Device},  {@link java.lang.String} or {@link javapns.devices.Device}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  public static PushedNotifications payload(final Payload payload, final Object keystore, final String password, final boolean production, final Object devices) throws CommunicationException, KeystoreException {
    return sendPayload(payload, keystore, password, production, devices);
  }

  /**
   * Push a preformatted payload to a list of devices.
   *
   * @param payload    a simple or complex payload to push.
   * @param keystore   a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password   the keystore's password.
   * @param production true to use Apple's production servers, false to use the sandbox servers.
   * @param devices    a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List} {@link java.lang.String},  {@link javapns.devices.Device Device[]}, {@link java.util.List} {@link javapns.devices.Device},  {@link java.lang.String} or {@link javapns.devices.Device}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  private static PushedNotifications sendPayload(final Payload payload, final Object keystore, final String password, final boolean production, final Object devices) throws CommunicationException, KeystoreException {
    final PushedNotifications notifications = new PushedNotifications();
    if (payload == null) {
      return notifications;
    }
    final PushNotificationManager pushManager = new PushNotificationManager();
    try {
      final AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
      pushManager.initializeConnection(server);
      final List<Device> deviceList = Devices.asDevices(devices);
      notifications.setMaxRetained(deviceList.size());
      for (final Device device : deviceList) {
        try {
          BasicDevice.validateTokenFormat(device.getToken());
          final PushedNotification notification = pushManager.sendNotification(device, payload, false);
          notifications.add(notification);
        } catch (final InvalidDeviceTokenFormatException e) {
          notifications.add(new PushedNotification(device, payload, e));
        }
      }
    } finally {
      try {
        pushManager.stopConnection();
      } catch (final Exception e) {
        logger.error(e.getMessage(), e);
      }
    }
    return notifications;
  }

  /**
   * Push a preformatted payload to a list of devices using multiple simulatenous threads (and connections).
   *
   * @param payload         a simple or complex payload to push.
   * @param keystore        a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password        the keystore's password.
   * @param production      true to use Apple's production servers, false to use the sandbox servers.
   * @param numberOfThreads the number of parallel threads to use to push the notifications
   * @param devices         a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List} {@link java.lang.String},  {@link javapns.devices.Device Device[]}, {@link java.util.List} {@link javapns.devices.Device},  {@link java.lang.String} or {@link javapns.devices.Device}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws Exception thrown if any critical exception occurs
   */
  public static PushedNotifications payload(final Payload payload, final Object keystore, final String password, final boolean production, final int numberOfThreads, final Object devices) throws Exception {
    if (numberOfThreads <= 0) {
      return sendPayload(payload, keystore, password, production, devices);
    }
    final AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
    final List<Device> deviceList = Devices.asDevices(devices);
    final NotificationThreads threads = new NotificationThreads(server, payload, deviceList, numberOfThreads);
    threads.start();
    try {
      threads.waitForAllThreads(true);
    } catch (final InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
    return threads.getPushedNotifications();
  }

  /**
   * Build and start an asynchronous queue for sending notifications later without opening and closing connections.
   * The returned queue is not started, meaning that underlying threads and connections are not initialized.
   * The queue will start if you invoke its start() method or one of the add() methods.
   * Once the queue is started, its underlying thread(s) and connection(s) will remain active until the program ends.
   *
   * @param keystore        a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password        the keystore's password.
   * @param production      true to use Apple's production servers, false to use the sandbox servers.
   * @param numberOfThreads the number of parallel threads to use to push the notifications
   * @return a live queue to which you can add notifications to be sent asynchronously
   * @throws KeystoreException thrown if an error occurs when loading the keystore
   */
  public static PushQueue queue(final Object keystore, final String password, final boolean production, final int numberOfThreads) throws KeystoreException {
    final AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
    final PushQueue queue = numberOfThreads <= 1 ? new NotificationThread(server) : new NotificationThreads(server, numberOfThreads);
    return queue;
  }

  /**
   * Push a different preformatted payload for each device.
   *
   * @param keystore           a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password           the keystore's password.
   * @param production         true to use Apple's production servers, false to use the sandbox servers.
   * @param payloadDevicePairs a list or an array of PayloadPerDevice: {@link java.util.List} {@link javapns.notification.PayloadPerDevice},  {@link javapns.notification.PayloadPerDevice PayloadPerDevice[]} or {@link javapns.notification.PayloadPerDevice}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  public static PushedNotifications payloads(final Object keystore, final String password, final boolean production, final Object payloadDevicePairs) throws CommunicationException, KeystoreException {
    return sendPayloads(keystore, password, production, payloadDevicePairs);
  }

  /**
   * Push a different preformatted payload for each device using multiple simulatenous threads (and connections).
   *
   * @param keystore           a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password           the keystore's password.
   * @param production         true to use Apple's production servers, false to use the sandbox servers.
   * @param numberOfThreads    the number of parallel threads to use to push the notifications
   * @param payloadDevicePairs a list or an array of PayloadPerDevice: {@link java.util.List} {@link javapns.notification.PayloadPerDevice},  {@link javapns.notification.PayloadPerDevice PayloadPerDevice[]} or {@link javapns.notification.PayloadPerDevice}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws Exception thrown if any critical exception occurs
   */
  public static PushedNotifications payloads(final Object keystore, final String password, final boolean production, final int numberOfThreads, final Object payloadDevicePairs) throws Exception {
    if (numberOfThreads <= 0) {
      return sendPayloads(keystore, password, production, payloadDevicePairs);
    }
    final AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
    final List<PayloadPerDevice> payloadPerDevicePairs = Devices.asPayloadsPerDevices(payloadDevicePairs);
    final NotificationThreads threads = new NotificationThreads(server, payloadPerDevicePairs, numberOfThreads);
    threads.start();
    try {
      threads.waitForAllThreads(true);
    } catch (final InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
    return threads.getPushedNotifications();
  }

  /**
   * Push a different preformatted payload for each device.
   *
   * @param keystore           a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password           the keystore's password.
   * @param production         true to use Apple's production servers, false to use the sandbox servers.
   * @param payloadDevicePairs a list or an array of PayloadPerDevice: {@link java.util.List} {@link javapns.notification.PayloadPerDevice},  {@link javapns.notification.PayloadPerDevice PayloadPerDevice[]} or {@link javapns.notification.PayloadPerDevice}
   * @return a list of pushed notifications, each with details on transmission results and error (if any)
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  private static PushedNotifications sendPayloads(final Object keystore, final String password, final boolean production, final Object payloadDevicePairs) throws CommunicationException, KeystoreException {
    final PushedNotifications notifications = new PushedNotifications();
    if (payloadDevicePairs == null) {
      return notifications;
    }
    final PushNotificationManager pushManager = new PushNotificationManager();
    try {
      final AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
      pushManager.initializeConnection(server);
      final List<PayloadPerDevice> pairs = Devices.asPayloadsPerDevices(payloadDevicePairs);
      notifications.setMaxRetained(pairs.size());
      for (final PayloadPerDevice ppd : pairs) {
        final Device device = ppd.getDevice();
        final Payload payload = ppd.getPayload();
        try {
          final PushedNotification notification = pushManager.sendNotification(device, payload, false);
          notifications.add(notification);
        } catch (final Exception e) {
          notifications.add(new PushedNotification(device, payload, e));
        }
      }
    } finally {
      try {
        pushManager.stopConnection();
      } catch (final Exception e) {
        logger.error(e.getMessage(), e);
      }
    }
    return notifications;
  }

  /**
   * <p>Retrieve a list of devices that should be removed from future notification lists.</p>
   *
   * <p>Devices in this list are ones that you previously tried to push a notification to,
   * but to which Apple could not actually deliver because the device user has either
   * opted out of notifications, has uninstalled your application, or some other conditions.</p>
   *
   * <p>Important: Apple's Feedback Service always resets its list of inactive devices
   * after each time you contact it.  Calling this method twice will not return the same
   * list of devices!</p>
   *
   * <p>Please be aware that Apple does not specify precisely when a device will be listed
   * by the Feedback Service.  More specifically, it is unlikely that the device will
   * be  listed immediately if you uninstall the application during testing.  It might
   * get listed after some number of notifications couldn't reach it, or some amount of
   * time has elapsed, or a combination of both.</p>
   *
   * <p>Further more, if you are using Apple's sandbox servers, the Feedback Service will
   * probably not list your device if you uninstalled your app and it was the last one
   * on your device that was configured to receive notifications from the sandbox.
   * See the library's wiki for more information.</p>
   *
   * @param keystore   a keystore containing your private key and the certificate signed by Apple ({@link java.io.File}, {@link java.io.InputStream}, byte[], {@link java.security.KeyStore} or {@link java.lang.String} for a file path)
   * @param password   the keystore's password.
   * @param production true to use Apple's production servers, false to use the sandbox servers.
   * @return a list of devices that are inactive.
   * @throws KeystoreException      thrown if an error occurs when loading the keystore
   * @throws CommunicationException thrown if an unrecoverable error occurs while trying to communicate with Apple servers
   */
  public static List<Device> feedback(final Object keystore, final String password, final boolean production) throws CommunicationException, KeystoreException {
    final List<Device> devices = new Vector<>();
    final FeedbackServiceManager feedbackManager = new FeedbackServiceManager();
    final AppleFeedbackServer server = new AppleFeedbackServerBasicImpl(keystore, password, production);
    devices.addAll(feedbackManager.getDevices(server));
    return devices;
  }
}
