package javapns.notification.transmission;

import javapns.devices.Device;
import javapns.devices.Devices;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.notification.*;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * <h1>Pushes a payload to a large number of devices using multiple threads</h1>
 * <p>
 * <p>The list of devices is spread evenly into multiple {@link javapns.notification.transmission.NotificationThread}s.</p>
 * <p>
 * <p>Usage: once a NotificationThreads is created, invoke {@code start()} to start all {@link javapns.notification.transmission.NotificationThread} threads.</p>
 * <p>You can provide a {@link javapns.notification.transmission.NotificationProgressListener} to receive events about the work being done.</p>
 *
 * @author Sylvain Pedneault
 * @see NotificationThread.MODE
 * @see NotificationThread
 */
public class NotificationThreads extends ThreadGroup implements PushQueue {
  private static final long DEFAULT_DELAY_BETWEEN_THREADS = 500; // the number of milliseconds to wait between each thread startup

  private final Object finishPoint = new Object();

  private List<NotificationThread> threads = new Vector<>();
  private NotificationProgressListener listener;

  private boolean started = false;
  private int threadsRunning = 0;
  private int nextThread = 0;
  private long delayBetweenThreads = DEFAULT_DELAY_BETWEEN_THREADS;

  /**
   * Create the specified number of notification threads and spread the devices evenly between the threads.
   *
   * @param server          the server to push to
   * @param payload         the payload to push
   * @param devices         a very large list of devices
   * @param numberOfThreads the number of threads to create to share the work
   */
  public NotificationThreads(final AppleNotificationServer server, final Payload payload, final List<Device> devices, final int numberOfThreads) {
    super("javapns notification threads (" + numberOfThreads + " threads)");
    threads.addAll(makeGroups(devices, numberOfThreads).stream().map(deviceGroup -> new NotificationThread(this, new PushNotificationManager(), server, payload, deviceGroup)).collect(Collectors.toList()));
  }

  /**
   * Create the specified number of notification threads and spread the messages evenly between the threads.
   *
   * @param server          the server to push to
   * @param messages        a very large list of payload/device pairs
   * @param numberOfThreads the number of threads to create to share the work
   */
  public NotificationThreads(final AppleNotificationServer server, final List<PayloadPerDevice> messages, final int numberOfThreads) {
    super("javapns notification threads (" + numberOfThreads + " threads)");
    threads.addAll(makeGroups(messages, numberOfThreads).stream().map(deviceGroup -> new NotificationThread(this, new PushNotificationManager(), server, deviceGroup)).collect(Collectors.toList()));
  }

  /**
   * Create the specified number of notification threads and spread the devices evenly between the threads.
   * Internally, this constructor uses a AppleNotificationServerBasicImpl to encapsulate the provided keystore, password and production parameters.
   *
   * @param keystore        the keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
   * @param password        the keystore's password
   * @param production      true to use Apple's production servers, false to use the sandbox
   * @param payload         the payload to push
   * @param devices         a very large list of devices
   * @param numberOfThreads the number of threads to create to share the work
   * @throws Exception
   */
  public NotificationThreads(final Object keystore, final String password, final boolean production, final Payload payload, final List<Device> devices, final int numberOfThreads) throws Exception {
    this(new AppleNotificationServerBasicImpl(keystore, password, production), payload, devices, numberOfThreads);
  }

  /**
   * Spread the devices evenly between the provided threads.
   *
   * @param server  the server to push to
   * @param payload the payload to push
   * @param devices a very large list of devices
   * @param threads a list of pre-built threads
   */
  @SuppressWarnings("unchecked")
  private NotificationThreads(final AppleNotificationServer server, final Payload payload, final List<Device> devices, final List<NotificationThread> threads) {
    super("javapns notification threads (" + threads.size() + " threads)");
    this.threads = threads;
    final List<List<?>> groups = makeGroups(devices, threads.size());
    for (int i = 0; i < groups.size(); i++) {
      threads.get(i).setDevices((List<Device>) groups.get(i));
    }
  }

  /**
   * Spread the devices evenly between the provided threads.
   * Internally, this constructor uses a AppleNotificationServerBasicImpl to encapsulate the provided keystore, password and production parameters.
   *
   * @param keystore   the keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
   * @param password   the keystore's password
   * @param production true to use Apple's production servers, false to use the sandbox
   * @param payload    the payload to push
   * @param devices    a very large list of devices
   * @param threads    a list of pre-built threads
   * @throws Exception
   */
  public NotificationThreads(final Object keystore, final String password, final boolean production, final Payload payload, final List<Device> devices, final List<NotificationThread> threads) throws Exception {
    this(new AppleNotificationServerBasicImpl(keystore, password, production), payload, devices, threads);
  }

  /**
   * Use the provided threads which should already each have their group of devices to work with.
   *
   * @param server  the server to push to
   * @param payload the payload to push
   * @param threads a list of pre-built threads
   */
  private NotificationThreads(final AppleNotificationServer server, final Payload payload, final List<NotificationThread> threads) {
    super("javapns notification threads (" + threads.size() + " threads)");
    this.threads = threads;
  }

  /**
   * Use the provided threads which should already each have their group of devices to work with.
   * Internally, this constructor uses a AppleNotificationServerBasicImpl to encapsulate the provided keystore, password and production parameters.
   *
   * @param keystore   the keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
   * @param password   the keystore's password
   * @param production true to use Apple's production servers, false to use the sandbox
   * @param payload    the payload to push
   * @param threads    a list of pre-built threads
   *
   * @throws Exception If the keystore cannot be loaded
   */
  public NotificationThreads(final Object keystore, final String password, final boolean production, final Payload payload, final List<NotificationThread> threads) throws Exception {
    this(new AppleNotificationServerBasicImpl(keystore, password, production), payload, threads);
  }

  /**
   * Create a pool of notification threads in QUEUE mode.
   *
   * @param server          the server to push to
   * @param numberOfThreads the number of threads to create in the pool
   */
  public NotificationThreads(final AppleNotificationServer server, final int numberOfThreads) {
    super("javapns notification thread pool (" + numberOfThreads + " threads)");
    for (int i = 0; i < numberOfThreads; i++) {
      threads.add(new NotificationThread(this, new PushNotificationManager(), server));
    }
  }

  /**
   * Create groups of devices or payload/device pairs ready to be dispatched to worker threads.
   *
   * @param objects a large list of devices
   * @param threads the number of threads to group devices for
   * @return
   */
  private static List<List<?>> makeGroups(final List<?> objects, final int threads) {
    final List<List<?>> groups = new Vector<>(threads);
    final int total = objects.size();
    int devicesPerThread = (total / threads);
    if (total % threads > 0) {
      devicesPerThread++;
    }
    //System.out.println("Making "+threads+" groups of "+devicesPerThread+" devices out of "+total+" devices in total");
    for (int i = 0; i < threads; i++) {
      final int firstObject = i * devicesPerThread;
      if (firstObject >= total) {
        break;
      }
      int lastObject = firstObject + devicesPerThread - 1;
      if (lastObject >= total) {
        lastObject = total - 1;
      }
      lastObject++;
      //System.out.println("Grouping together "+(lastDevice-firstDevice)+" devices (#"+firstDevice+" to "+lastDevice+")");
      final List threadObjects = objects.subList(firstObject, lastObject);
      groups.add(threadObjects);
    }

    return groups;
  }

  public PushQueue add(final Payload payload, final String token) throws InvalidDeviceTokenFormatException {
    return add(new PayloadPerDevice(payload, token));
  }

  public PushQueue add(final Payload payload, final Device device) {
    return add(new PayloadPerDevice(payload, device));
  }

  public PushQueue add(final PayloadPerDevice message) {
    start(); // just in case start() was not invoked before
    final NotificationThread targetThread = getNextAvailableThread();
    targetThread.add(message);
    return targetThread;
  }

  /**
   * Get the next available thread.
   *
   * @return a thread potentially available to work
   */
  private NotificationThread getNextAvailableThread() {
    for (int i = 0; i < threads.size(); i++) {
      final NotificationThread thread = getNextThread();
      final boolean busy = thread.isBusy();
      if (!busy) {
        return thread;
      }
    }
    return getNextThread(); /* All threads are busy, return the next one regardless of its busy status */
  }

  /**
   * Get the next thread to use.
   *
   * @return a thread
   */
  private synchronized NotificationThread getNextThread() {
    if (nextThread >= threads.size()) {
      nextThread = 0;
    }
    return threads.get(nextThread++);
  }

  /**
   * Start all notification threads.
   * <p>
   * This method returns immediately, as all threads start working on their own.
   * To wait until all threads are finished, use the waitForAllThreads() method.
   */
  public synchronized NotificationThreads start() {
    if (started) {
      return this;
    }
    started = true;
    if (threadsRunning > 0) {
      throw new IllegalStateException("NotificationThreads already started (" + threadsRunning + " still running)");
    }
    assignThreadsNumbers();
    for (final NotificationThread thread : threads) {
      threadsRunning++;
      thread.start();
      try {
        /* Wait for a specific number of milliseconds to elapse so that not all threads start simultaenously. */
        Thread.sleep(delayBetweenThreads);
      } catch (final InterruptedException e) {
        // empty
      }
    }
    if (listener != null) {
      listener.eventAllThreadsStarted(this);
    }
    return this;
  }

  /**
   * Configure in all threads the maximum number of notifications per connection.
   * <p>
   * As soon as a thread reaches that maximum, it will automatically close the connection,
   * initialize a new connection and continue pushing more notifications.
   *
   * @param notifications the maximum number of notifications that threads will push in a single connection (default is 200)
   */
  public void setMaxNotificationsPerConnection(final int notifications) {
    for (final NotificationThread thread : threads) {
      thread.setMaxNotificationsPerConnection(notifications);
    }
  }

  /**
   * Configure in all threads the number of milliseconds that threads should wait between each notification.
   * <p>
   * This feature is intended to alleviate intense resource usage that can occur when
   * sending large quantities of notifications very quickly.
   *
   * @param milliseconds the number of milliseconds threads should sleep between individual notifications (default is 0)
   */
  public void setSleepBetweenNotifications(final long milliseconds) {
    for (final NotificationThread thread : threads) {
      thread.setSleepBetweenNotifications(milliseconds);
    }
  }

  /**
   * Get a list of threads created to push notifications.
   *
   * @return a list of threads
   */
  public List<NotificationThread> getThreads() {
    return threads;
  }

  /**
   * Get the progress listener, if any is attached.
   *
   * @return a progress listener
   */
  public NotificationProgressListener getListener() {
    return listener;
  }

  /**
   * Attach an event listener to this object as well as all linked threads.
   *
   * @param listener
   */
  public void setListener(final NotificationProgressListener listener) {
    this.listener = listener;
    for (final NotificationThread thread : threads) {
      thread.setListener(listener);
    }
  }

  /**
   * Worker threads invoke this method as soon as they have completed their work.
   * This method tracks the number of threads still running, allowing us
   * to detect when ALL threads have finished.
   * <p>
   * When all threads are done working, this method fires an AllThreadsFinished
   * event to the attached listener (if one is present) and wakes up any
   * object that is waiting for the waitForAllThreads() method to return.
   *
   * @param notificationThread
   */
  synchronized void threadFinished(final NotificationThread notificationThread) {
    threadsRunning--;
    if (threadsRunning == 0) {
      if (listener != null) {
        listener.eventAllThreadsFinished(this);
      }
      try {
        synchronized (finishPoint) {
          finishPoint.notifyAll();
        }
      } catch (final Exception e) {
        // empty
      }
    }
  }

  /**
   * Wait for all threads to complete their work.
   * <p>
   * This method blocks and returns only when all threads are done.
   * When using this method, you need to check critical exceptions manually to make sure that all threads were able to do their work.
   * <p>
   * This method should not be used in QUEUE mode, as threads stay idle and never end.
   *
   * @throws InterruptedException
   */
  public void waitForAllThreads() throws InterruptedException {
    synchronized (finishPoint) {
      finishPoint.wait();
    }
  }

  /**
   * Wait for all threads to complete their work, but throw any critical exception that occurs in a thread.
   * <p>
   * This method blocks and returns only when all threads are done.
   * <p>
   * This method should not be used in QUEUE mode, as threads stay idle and never end.
   *
   * @param throwCriticalExceptions If true, this method will throw the first critical exception that occured in a thread (if any).  If false, critical exceptions will not be checked.
   * @throws Exception if throwCriticalExceptions is true and a critical exception did occur in a thread
   */
  public void waitForAllThreads(final boolean throwCriticalExceptions) throws Exception {
    waitForAllThreads();
    if (throwCriticalExceptions) {
      final List<Exception> exceptions = getCriticalExceptions();
      if (exceptions.size() > 0) {
        throw exceptions.get(0);
      }
    }
  }

  /**
   * Assign unique numbers to worker threads.
   * Thread numbers allow each thread to generate message identifiers that
   * are unique to all threads in the group.
   */
  private void assignThreadsNumbers() {
    int t = 1;
    for (final NotificationThread thread : threads) {
      thread.setThreadNumber(t++);
    }
  }

  /**
   * Get a list of all notifications pushed by all threads.
   *
   * @return a list of pushed notifications
   */
  public PushedNotifications getPushedNotifications() {
    int capacity = 0;
    for (final NotificationThread thread : threads) {
      capacity += thread.getPushedNotifications().size();
    }
    final PushedNotifications all = new PushedNotifications(capacity);
    all.setMaxRetained(capacity);
    for (final NotificationThread thread : threads) {
      all.addAll(thread.getPushedNotifications());
    }
    return all;
  }

  /**
   * Clear the internal list of PushedNotification objects maintained in each thread.
   * You should invoke this method once you no longer need the list of PushedNotification objects so that memory can be reclaimed.
   */
  public void clearPushedNotifications() {
    for (final NotificationThread thread : threads) {
      thread.clearPushedNotifications();
    }
  }

  /**
   * Get a list of all notifications that all threads attempted to push but that failed.
   *
   * @return a list of failed notifications
   */
  public PushedNotifications getFailedNotifications() {
    return getPushedNotifications().getFailedNotifications();
  }

  /**
   * Get a list of all notifications that all threads attempted to push and succeeded.
   *
   * @return a list of successful notifications
   */
  public PushedNotifications getSuccessfulNotifications() {
    return getPushedNotifications().getSuccessfulNotifications();
  }

  /**
   * Get a list of critical exceptions that threads experienced.
   * Critical exceptions include CommunicationException and KeystoreException.
   * Exceptions related to tokens, payloads and such are *not* included here,
   * as they are noted in individual PushedNotification objects.
   *
   * @return a list of critical exceptions
   */
  public List<Exception> getCriticalExceptions() {
    final List<Exception> exceptions = new Vector<>();
    for (final NotificationThread thread : threads) {
      final Exception exception = thread.getCriticalException();
      if (exception != null) {
        exceptions.add(exception);
      }
    }
    return exceptions;
  }

  /**
   * Get the amount of time that the library will wait after starting a thread and before starting the next one.
   *
   * @return the number of milliseconds currently configured
   */
  public long getDelayBetweenThreads() {
    return delayBetweenThreads;
  }

  /**
   * Set the amount of time that the library will wait after starting a thread and before starting the next one.
   * The default delay is 200 milliseconds.  This means that starting 10 threads will take 2 seconds to fully start.
   * As discussed in issue report #102, adding a delay improves reliability.
   *
   * @param delayBetweenThreads a number of milliseconds
   */
  public void setDelayBetweenThreads(final long delayBetweenThreads) {
    this.delayBetweenThreads = delayBetweenThreads;
  }

}
