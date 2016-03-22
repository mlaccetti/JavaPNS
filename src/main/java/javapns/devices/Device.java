package javapns.devices;

import java.sql.Timestamp;

/**
 * This is the common interface for all Devices.
 * It allows the DeviceFactory to support multiple
 * implementations of Device (in-memory, JPA-backed, etc.)
 *
 * @author Sylvain Pedneault
 */
public interface Device {
  /**
   * An id representing a particular device.
   * <p>
   * Note that this is a local reference to the device,
   * which is not related to the actual device UUID or
   * other device-specific identification. Most of the
   * time, this deviceId should be the same as the token.
   *
   * @return the device id
   */
  String getDeviceId();

  /**
   * An id representing a particular device.
   * <p>
   * Note that this is a local reference to the device,
   * which is not related to the actual device UUID or
   * other device-specific identification. Most of the
   * time, this deviceId should be the same as the token.
   *
   * @param id the device id
   */
  void setDeviceId(String id);

  /**
   * A device token.
   *
   * @return the device token
   */
  String getToken();

  /**
   * Set the device token
   *
   * @param token
   */
  void setToken(String token);

  /**
   * @return the last register
   */
  Timestamp getLastRegister();

  /**
   * @param lastRegister the last register
   */
  void setLastRegister(Timestamp lastRegister);
}
