package javapns.notification.transmission;

import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.Devices;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.notification.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * <h1>Pushes payloads asynchroneously using a dedicated thread.</h1>
 * <p>
 * <p>A NotificationThread is created with one of two modes:  LIST or QUEUE.
 * In LIST mode, the thread is given a predefined list of devices and pushes all notifications as soon as it is started.  Its work is complete and the thread ends as soon as all notifications have been sent.
 * In QUEUE mode, the thread is started with no notification to send.  It opens a connection and waits for messages to be added to its queue using the addMessageToQueue(..) method.  This lifecyle is useful for creating connection pools.</p>
 * <p>
 * <p>No more than {@code maxNotificationsPerConnection} are pushed over a single connection.
 * When that maximum is reached, the connection is restarted automatically and push continues.
 * This is intended to avoid an undocumented notification-per-connection limit observed
 * occasionnally with Apple servers.</p>
 * <p>
 * <p>Usage (LIST): once a NotificationThread is created using any LIST-mode constructor, invoke {@code start()} to push the payload to all devices in a separate thread.</p>
 * <p>
 * <p>Usage (QUEUE): once a NotificationThread is created using any QUEUE-mode constructor, invoke {@code start()} to open a connection and wait for notifications to be queued.</p>
 *
 * @author Sylvain Pedneault
 * @see NotificationThread.MODE
 * @see NotificationThreads
 */
public class NotificationThread implements Runnable, PushQueue {
  private static final int DEFAULT_MAXNOTIFICATIONSPERCONNECTION = 200;
  private static final String JAVA_PNS = "JavaPNS";
  private static final String STANDALONE = " standalone";
  private static final String GROUPED = " grouped";

  private final Thread thread;
  private final AppleNotificationServer server;
  private final PushNotificationManager notificationManager;
  private final PushedNotifications notifications = new PushedNotifications();

  private boolean started = false;
  private int maxNotificationsPerConnection = DEFAULT_MAXNOTIFICATIONSPERCONNECTION;
  private long sleepBetweenNotifications = 0;
  private NotificationProgressListener listener;
  private int threadNumber = 1;
  private int nextMessageIdentifier = 1;

  private MODE mode = MODE.LIST;
  private boolean busy = false;

  /* Single payload to multiple devices */
  private Payload payload;

  private List<Device> devices;
  /* Individual payload per device */

  private List<PayloadPerDevice> messages = new ArrayList<>();

  private Exception exception;

  /**
   * Create a grouped thread in LIST mode for pushing a single payload to a list of devices
   * and coordinating with a parent NotificationThreads object.
   *
   * @param threads             the parent NotificationThreads object that is coordinating multiple threads
   * @param notificationManager the notification manager to use
   * @param server              the server to communicate with
   * @param payload             a payload to push
   * @param devices             a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List}<{@link java.lang.String}>, {@link javapns.devices.Device Device[]}, {@link java.util.List}<{@link javapns.devices.Device}>, {@link java.lang.String} or {@link javapns.devices.Device}
   */
  public NotificationThread(final NotificationThreads threads, final PushNotificationManager notificationManager, final AppleNotificationServer server, final Payload payload, final Object devices) {
    this.thread = new Thread(threads, this, JAVA_PNS + (threads != null ? GROUPED : STANDALONE) + " notification thread in LIST mode");
    this.notificationManager = notificationManager == null ? new PushNotificationManager() : notificationManager;
    this.server = server;
    this.payload = payload;
    this.devices = Devices.asDevices(devices);
    this.notifications.setMaxRetained(this.devices.size());
  }

  /**
   * Create a grouped thread in LIST mode for pushing individual payloads to a list of devices
   * and coordinating with a parent NotificationThreads object.
   *
   * @param threads             the parent NotificationThreads object that is coordinating multiple threads
   * @param notificationManager the notification manager to use
   * @param server              the server to communicate with
   * @param messages            a list or an array of PayloadPerDevice: {@link java.util.List}<{@link javapns.notification.PayloadPerDevice}>, {@link javapns.notification.PayloadPerDevice PayloadPerDevice[]} or {@link javapns.notification.PayloadPerDevice}
   */
  public NotificationThread(final NotificationThreads threads, final PushNotificationManager notificationManager, final AppleNotificationServer server, final Object messages) {
    this.thread = new Thread(threads, this, JAVA_PNS + (threads != null ? GROUPED : STANDALONE) + " notification thread in LIST mode");
    this.notificationManager = notificationManager == null ? new PushNotificationManager() : notificationManager;
    this.server = server;
    this.messages = Devices.asPayloadsPerDevices(messages);
    this.notifications.setMaxRetained(this.messages.size());
  }

  /**
   * Create a standalone thread in LIST mode for pushing a single payload to a list of devices.
   *
   * @param notificationManager the notification manager to use
   * @param server              the server to communicate with
   * @param payload             a payload to push
   * @param devices             a list or an array of tokens or devices: {@link java.lang.String String[]}, {@link java.util.List}<{@link java.lang.String}>, {@link javapns.devices.Device Device[]}, {@link java.util.List}<{@link javapns.devices.Device}>, {@link java.lang.String} or {@link javapns.devices.Device}
   */
  public NotificationThread(final PushNotificationManager notificationManager, final AppleNotificationServer server, final Payload payload, final Object devices) {
    this(null, notificationManager, server, payload, devices);
  }

  /**
   * Create a standalone thread in LIST mode for pushing individual payloads to a list of devices.
   *
   * @param notificationManager the notification manager to use
   * @param server              the server to communicate with
   * @param messages            a list or an array of PayloadPerDevice: {@link java.util.List}<{@link javapns.notification.PayloadPerDevice}>, {@link javapns.notification.PayloadPerDevice PayloadPerDevice[]} or {@link javapns.notification.PayloadPerDevice}
   */
  public NotificationThread(final PushNotificationManager notificationManager, final AppleNotificationServer server, final Object messages) {
    this(null, notificationManager, server, messages);
  }

  /**
   * Create a grouped thread in QUEUE mode, awaiting messages to push.
   *
   * @param threads             the parent NotificationThreads object that is coordinating multiple threads
   * @param notificationManager the notification manager to use
   * @param server              the server to communicate with
   */
  public NotificationThread(final NotificationThreads threads, final PushNotificationManager notificationManager, final AppleNotificationServer server) {
    this.thread = new Thread(threads, this, JAVA_PNS + (threads != null ? GROUPED : STANDALONE) + " notification thread in QUEUE mode");
    this.notificationManager = notificationManager == null ? new PushNotificationManager() : notificationManager;
    this.server = server;
    this.mode = MODE.QUEUE;
    this.thread.setDaemon(true);
  }

  /**
   * Create a standalone thread in QUEUE mode, awaiting messages to push.
   *
   * @param notificationManager the notification manager to use
   * @param server              the server to communicate with
   */
  public NotificationThread(final PushNotificationManager notificationManager, final AppleNotificationServer server) {
    this(null, notificationManager, server);
  }

  /**
   * Create a standalone thread in QUEUE mode, awaiting messages to push.
   *
   * @param server the server to communicate with
   */
  public NotificationThread(final AppleNotificationServer server) {
    this(null, new PushNotificationManager(), server);
  }

  /**
   * Start the transmission thread.
   * <p>
   * This method returns immediately, as the thread starts working on its own.
   */
  public synchronized NotificationThread start() {
    if (started) {
      return this;
    }
    started = true;
    try {
      this.thread.start();
    } catch (final IllegalStateException e) {
      // empty
    }
    return this;
  }

  /**
   * Run method for the thread; do not call this method directly.
   */
  public void run() {
    switch (mode) {
      case LIST:
        runList();
        break;
      case QUEUE:
        runQueue();
        break;
      default:
        break;
    }
  }

  private void runList() {
    if (listener != null) {
      listener.eventThreadStarted(this);
    }
    busy = true;
    try {
      final int total = size();
      notificationManager.initializeConnection(server);
      for (int i = 0; i < total; i++) {
        final Device device;
        final Payload payload;
        if (devices != null) {
          device = devices.get(i);
          payload = this.payload;
        } else {
          final PayloadPerDevice message = messages.get(i);
          device = message.getDevice();
          payload = message.getPayload();
        }
        final int message = newMessageIdentifier();
        final PushedNotification notification = notificationManager.sendNotification(device, payload, false, message);
        notifications.add(notification);
        try {
          if (sleepBetweenNotifications > 0) {
            Thread.sleep(sleepBetweenNotifications);
          }
        } catch (final InterruptedException e) {
          // empty
        }
        if (i != 0 && i % maxNotificationsPerConnection == 0) {
          if (listener != null) {
            listener.eventConnectionRestarted(this);
          }
          notificationManager.restartConnection(server);
        }
      }
      notificationManager.stopConnection();
    } catch (final KeystoreException | CommunicationException e) {
      this.exception = e;
      if (listener != null) {
        listener.eventCriticalException(this, e);
      }
    }
    busy = false;
    if (listener != null) {
      listener.eventThreadFinished(this);
    }
    /* Also notify the parent NotificationThreads, so that it can determine when all threads have finished working */
    if (this.thread.getThreadGroup() instanceof NotificationThreads) {
      ((NotificationThreads) this.thread.getThreadGroup()).threadFinished(this);
    }
  }

  private void runQueue() {
    if (listener != null) {
      listener.eventThreadStarted(this);
    }
    try {
      notificationManager.initializeConnection(server);
      int notificationsPushed = 0;
      while (mode == MODE.QUEUE) {
        while (!messages.isEmpty()) {
          busy = true;
          final PayloadPerDevice message = messages.get(0);
          messages.remove(message);
          notificationsPushed++;
          final int messageId = newMessageIdentifier();
          final PushedNotification notification = notificationManager.sendNotification(message.getDevice(), message.getPayload(), false, messageId);
          notifications.add(notification);
          try {
            if (sleepBetweenNotifications > 0) {
              Thread.sleep(sleepBetweenNotifications);
            }
          } catch (final InterruptedException e) {
            // empty
          }
          if (notificationsPushed != 0 && notificationsPushed % maxNotificationsPerConnection == 0) {
            if (listener != null) {
              listener.eventConnectionRestarted(this);
            }
            notificationManager.restartConnection(server);
          }
          busy = false;
        }
        try {
          Thread.sleep(10 * 1000);
        } catch (final Exception e) {
          // empty
        }
      }
      notificationManager.stopConnection();
    } catch (final KeystoreException | CommunicationException e) {
      this.exception = e;
      if (listener != null) {
        listener.eventCriticalException(this, e);
      }
    }
    if (listener != null) {
      listener.eventThreadFinished(this);
    }
    /* Also notify the parent NotificationThreads, so that it can determine when all threads have finished working */
    if (this.thread.getThreadGroup() instanceof NotificationThreads) {
      ((NotificationThreads) this.thread.getThreadGroup()).threadFinished(this);
    }
  }

  public PushQueue add(final Payload payload, final String token) throws InvalidDeviceTokenFormatException {
    return add(new PayloadPerDevice(payload, token));
  }

  public PushQueue add(final Payload payload, final Device device) {
    return add(new PayloadPerDevice(payload, device));
  }

  public PushQueue add(final PayloadPerDevice message) {
    if (mode != MODE.QUEUE) {
      return this;
    }
    try {
      messages.add(message);
      this.thread.interrupt();
    } catch (final Exception e) {
      // empty
    }
    return this;
  }

  public int getMaxNotificationsPerConnection() {
    return maxNotificationsPerConnection;
  }

  /**
   * Set a maximum number of notifications that should be streamed over a continuous connection
   * to an Apple server.  When that maximum is reached, the thread automatically closes and
   * reopens a fresh new connection to the server and continues streaming notifications.
   * <p>
   * Default is 200 (recommended).
   *
   * @param maxNotificationsPerConnection
   */
  public void setMaxNotificationsPerConnection(final int maxNotificationsPerConnection) {
    this.maxNotificationsPerConnection = maxNotificationsPerConnection;
  }

  public long getSleepBetweenNotifications() {
    return sleepBetweenNotifications;
  }

  /**
   * Set a delay the thread should sleep between each notification.
   * This is sometimes useful when communication with Apple servers is
   * unreliable and notifications are streaming too fast.
   * <p>
   * Default is 0.
   *
   * @param milliseconds
   */
  public void setSleepBetweenNotifications(final long milliseconds) {
    this.sleepBetweenNotifications = milliseconds;
  }

  /**
   * Get the list of devices associated with this thread.
   *
   * @return a list of devices
   */
  public List<Device> getDevices() {
    return devices;
  }

  void setDevices(final List<Device> devices) {
    this.devices = devices;
  }

  /**
   * Get the number of devices that this thread pushes to.
   *
   * @return the number of devices registered with this thread
   */
  private int size() {
    return devices != null ? devices.size() : messages.size();
  }

  public NotificationProgressListener getListener() {
    return listener;
  }

  /**
   * Provide an event listener which will be notified of this thread's progress.
   *
   * @param listener any object implementing the NotificationProgressListener interface
   */
  public void setListener(final NotificationProgressListener listener) {
    this.listener = listener;
  }

  /**
   * Return the thread number assigned by the parent NotificationThreads object, if any.
   *
   * @return the unique number assigned to this thread by the parent group
   */
  public int getThreadNumber() {
    return threadNumber;
  }

  /**
   * Set the thread number so that generated message identifiers can be made
   * unique across all threads.
   *
   * @param threadNumber
   */
  void setThreadNumber(final int threadNumber) {
    this.threadNumber = threadNumber;
  }

  /**
   * Return a new sequential message identifier.
   *
   * @return a message identifier unique to all NotificationThread objects
   */
  private int newMessageIdentifier() {
    return (threadNumber << 24) | nextMessageIdentifier++;
  }

  /**
   * Returns the first message identifier generated by this thread.
   *
   * @return a message identifier unique to all NotificationThread objects
   */
  public int getFirstMessageIdentifier() {
    return (threadNumber << 24) | 1;
  }

  /**
   * Returns the last message identifier generated by this thread.
   *
   * @return a message identifier unique to all NotificationThread objects
   */
  public int getLastMessageIdentifier() {
    return (threadNumber << 24) | size();
  }

  /**
   * Returns list of all notifications pushed by this thread (successful or not).
   *
   * @return a list of pushed notifications
   */
  public PushedNotifications getPushedNotifications() {
    return notifications;
  }

  /**
   * Clear the internal list of PushedNotification objects.
   * You should invoke this method once you no longer need the list of PushedNotification objects so that memory can be reclaimed.
   */
  public void clearPushedNotifications() {
    notifications.clear();
  }

  /**
   * Returns list of all notifications that this thread attempted to push but that failed.
   *
   * @return a list of failed notifications
   */
  public PushedNotifications getFailedNotifications() {
    return getPushedNotifications().getFailedNotifications();
  }

  /**
   * Returns list of all notifications that this thread attempted to push and succeeded.
   *
   * @return a list of failed notifications
   */
  public PushedNotifications getSuccessfulNotifications() {
    return getPushedNotifications().getSuccessfulNotifications();
  }

  /**
   * Get the messages associated with this thread, if any.
   *
   * @return messages
   */
  public List<PayloadPerDevice> getMessages() {
    return messages;
  }

  /**
   * Set the messages associated with this thread.
   *
   * @param messages
   */
  void setMessages(final List<PayloadPerDevice> messages) {
    this.messages = messages;
  }

  /**
   * Determine if this thread is busy.
   *
   * @return if the thread is busy or not
   */
  public boolean isBusy() {
    return busy;
  }

  /**
   * If this thread experienced a critical exception (communication error, keystore issue, etc.), this method returns the exception.
   *
   * @return a critical exception, if one occurred in this thread
   */
  public Exception getCriticalException() {
    return exception;
  }

  /**
   * Wrap a critical exception (if any occurred) into a List to satisfy the NotificationQueue interface contract.
   *
   * @return a list containing a critical exception, if any occurred
   */
  public List<Exception> getCriticalExceptions() {
    final List<Exception> exceptions = new ArrayList<>(exception == null ? 0 : 1);
    if (exception != null) {
      exceptions.add(exception);
    }
    return exceptions;
  }

  /**
   * Working modes supported by Notification Threads.
   */
  public enum MODE {
    /**
     * In LIST mode, the thread is given a predefined list of devices and pushes all notifications as soon as it is started.
     * Its work is complete, the connection is closed and the thread ends as soon as all notifications have been sent.
     * This mode is appropriate when you have a large amount of notifications to send in one batch.
     */
    LIST,

    /**
     * In QUEUE mode, the thread is started with an open connection and no notification to send, and waits for notifications to be queued.
     * It opens a connection and waits for messages to be added to its queue using a queue(..) method.
     * This mode is appropriate when you need to periodically send random individual notifications and you do not wish to open and close connections to Apple all the time (which is something Apple warns against in their documentation).
     * Unless your software is constantly generating large amounts of random notifications and that you absolutely need to stream them over multiple threaded connections, you should not need to create more than one NotificationThread in QUEUE mode.
     */
    QUEUE
  }
}
