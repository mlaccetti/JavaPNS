package javapns.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>A list of PushedNotification objects.</p>
 * <p>
 * <p>This list can be configured to retain a maximum number of objects.  When that maximum is reached, older objects are removed from the list before new ones are added.</p>
 * <p>
 * <p>Internally, this list extends Vector.</p>
 *
 * @author Sylvain Pedneault
 */
public class PushedNotifications extends ArrayList<PushedNotification> implements List<PushedNotification> {
  private static final long serialVersionUID = 1418782231076330494L;
  private int maxRetained = 1000;

  /**
   * Construct an empty list of PushedNotification objects.
   */
  public PushedNotifications() {
  }

  /**
   * Construct an empty list of PushedNotification objects with a suggested initial capacity.
   *
   * @param capacity
   */
  public PushedNotifications(final int capacity) {
    super(capacity);
  }

  /**
   * Construct an empty list of PushedNotification objects, and copy the maxRetained property from the provided parent list.
   *
   * @param parent
   */
  private PushedNotifications(final PushedNotifications parent) {
    this.maxRetained = parent.getMaxRetained();
  }

  /**
   * Filter a list of pushed notifications and return only the ones that were successful.
   *
   * @return a filtered list containing only notifications that were succcessful
   */
  public PushedNotifications getSuccessfulNotifications() {
    final PushedNotifications filteredList = new PushedNotifications(this);
    for (final PushedNotification notification : this) {
      if (notification.isSuccessful()) {
        filteredList.add(notification);
      }
    }
    return filteredList;
  }

  /**
   * Filter a list of pushed notifications and return only the ones that failed.
   *
   * @return a filtered list containing only notifications that were <b>not</b> successful
   */
  public PushedNotifications getFailedNotifications() {
    final PushedNotifications filteredList = new PushedNotifications(this);
    for (final PushedNotification notification : this) {
      if (!notification.isSuccessful()) {
        filteredList.add(notification);
      }
    }
    return filteredList;
  }

  @Override
  public synchronized boolean add(final PushedNotification notification) {
    prepareAdd(1);
    return super.add(notification);
  }

  @Override
  public synchronized boolean addAll(final Collection<? extends PushedNotification> notifications) {
    prepareAdd(notifications.size());
    return super.addAll(notifications);
  }

  private void prepareAdd(final int n) {
    final int size = size();
    if (size + n > maxRetained) {
      for (int i = 0; i < n; i++) {
        remove(0);
      }
    }
  }

  /**
   * Get the maximum number of objects that this list retains.
   *
   * @return the maximum number of objects that this list retains
   */
  private int getMaxRetained() {
    return maxRetained;
  }

  /**
   * Set the maximum number of objects that this list retains.
   * When this maximum is reached, older objects are removed from the list before new ones are added.
   *
   * @param maxRetained the maxRetained value currently configured (default is 1000)
   */
  public void setMaxRetained(final int maxRetained) {
    this.maxRetained = maxRetained;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    PushedNotifications that = (PushedNotifications) o;

    return maxRetained == that.maxRetained;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + maxRetained;
    return result;
  }
}
