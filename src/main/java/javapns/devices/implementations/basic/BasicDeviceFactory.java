package javapns.devices.implementations.basic;

import javapns.devices.Device;
import javapns.devices.DeviceFactory;
import javapns.devices.exceptions.DuplicateDeviceException;
import javapns.devices.exceptions.NullDeviceTokenException;
import javapns.devices.exceptions.NullIdException;
import javapns.devices.exceptions.UnknownDeviceException;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements an in-memory DeviceFactory (backed by a Map).
 * Since this class does not persist Device objects, it should not be used in a production environment.
 * <p>
 * NB : Future Improvement :
 * - Add a method to find a device knowing his token
 * - Add a method to update a device (timestamp or token)
 * - method to compare two devices, and replace when the device token has changed
 *
 * @author Maxime Peron
 */
@Deprecated
public class BasicDeviceFactory implements DeviceFactory {
  /* synclock */
  private static final Object synclock = new Object();
  /* A map containing all the devices, identified with their id */
  private final Map<String, BasicDevice> devices;

  /**
   * Constructs a VolatileDeviceFactory
   */
  public BasicDeviceFactory() {
    this.devices = new HashMap<>();
  }

  /**
   * Add a device to the map
   *
   * @param id    The device id
   * @param token The device token
   * @throws DuplicateDeviceException
   * @throws NullIdException
   * @throws NullDeviceTokenException
   */
  public Device addDevice(final String id, String token) throws Exception {
    if ((id == null) || (id.trim().isEmpty())) {
      throw new NullIdException();
    } else if ((token == null) || (token.trim().isEmpty())) {
      throw new NullDeviceTokenException();
    } else {
      if (!this.devices.containsKey(id)) {
        final BasicDevice device = new BasicDevice(id, token.trim().replace(" ", ""), new Timestamp(Calendar.getInstance().getTime().getTime()));
        this.devices.put(id, device);
        return device;
      } else {
        throw new DuplicateDeviceException();
      }
    }
  }

  /**
   * Get a device according to his id
   *
   * @param id The device id
   * @return The device
   * @throws UnknownDeviceException
   * @throws NullIdException
   */
  public Device getDevice(final String id) throws UnknownDeviceException, NullIdException {
    if ((id == null) || (id.trim().isEmpty())) {
      throw new NullIdException();
    } else {
      if (this.devices.containsKey(id)) {
        return this.devices.get(id);
      } else {
        throw new UnknownDeviceException();
      }
    }
  }

  /**
   * Remove a device
   *
   * @param id The device id
   * @throws UnknownDeviceException
   * @throws NullIdException
   */
  public void removeDevice(final String id) throws UnknownDeviceException, NullIdException {
    if ((id == null) || (id.trim().isEmpty())) {
      throw new NullIdException();
    }
    if (this.devices.containsKey(id)) {
      this.devices.remove(id);
    } else {
      throw new UnknownDeviceException();
    }
  }
}
