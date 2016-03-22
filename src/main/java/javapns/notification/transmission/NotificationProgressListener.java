package javapns.notification.transmission;

/**
 * <h1>An event listener for monitoring progress of NotificationThreads</h1>
 *
 * @author Sylvain Pedneault
 */
public interface NotificationProgressListener {
  void eventAllThreadsStarted(NotificationThreads notificationThreads);

  void eventThreadStarted(NotificationThread notificationThread);

  void eventThreadFinished(NotificationThread notificationThread);

  void eventConnectionRestarted(NotificationThread notificationThread);

  void eventAllThreadsFinished(NotificationThreads notificationThreads);

  void eventCriticalException(NotificationThread notificationThread, Exception exception);
}
