package javapns.devices;

import javapns.devices.exceptions.DuplicateDeviceException;
import javapns.devices.exceptions.NullDeviceTokenException;
import javapns.devices.exceptions.NullIdException;
import javapns.devices.exceptions.UnknownDeviceException;

/**
 * This is the common interface for all DeviceFactories.
 * It allows the PushNotificationManager to support multiple
 * implementations of DeviceFactory (in-memory, JPA-backed, etc.)
 * 
 * @author Sylvain Pedneault
 * @deprecated Phasing out DeviceFactory because it has become irrelevant in the new library architecture 
 */
@Deprecated
public interface DeviceFactory {
  /**
   * Add a device to the map
   * @param id The local device id
   * @param token The device token
   * @return The device created
   * @throws DuplicateDeviceException
   * @throws NullIdException 
   * @throws NullDeviceTokenException 
   */
  public Device addDevice(String id, String token) throws DuplicateDeviceException, NullIdException, NullDeviceTokenException, Exception;

  /**
   * Get a device according to his id
   * @param id The local device id
   * @return The device
   * @throws UnknownDeviceException
   * @throws NullIdException 
   */
  public Device getDevice(String id) throws UnknownDeviceException, NullIdException;

  /**
   * Remove a device
   * @param id The local device id
   * @throws UnknownDeviceException
   * @throws NullIdException 
   */
  public void removeDevice(String id) throws UnknownDeviceException, NullIdException;
}