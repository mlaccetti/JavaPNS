package javapns.devices.implementations.basic;

import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;

import java.sql.Timestamp;

/**
 * This class is used to represent a Device (iPhone)
 *
 * @author Maxime Peron
 */
public class BasicDevice implements Device {

  /*
   * An id representing a particular device.
   *
   * Note that this is a local reference to the device,
   * which is not related to the actual device UUID or
   * other device-specific identification. Most of the
   * time, this deviceId should be the same as the token.
   */
  private String deviceId;

  /* The device token given by Apple Server, hexadecimal form, 64bits length */
  private String token;

  /* The last time a device registered */
  private Timestamp lastRegister;

  public BasicDevice() {
    // empty
  }

  /**
   * Default constructor.
   *
   * @param token The device token
   */
  public BasicDevice(final String token) throws InvalidDeviceTokenFormatException {
    this(token, true);
  }

  private BasicDevice(final String token, final boolean validate) throws InvalidDeviceTokenFormatException {
    super();
    this.deviceId = token;
    this.token = token;
    try {
      this.lastRegister = new Timestamp(System.currentTimeMillis());
    } catch (final Exception e) {
      // empty
    }

    if (validate) {
      validateTokenFormat(token);
    }
  }

  /**
   * Constructor
   *
   * @param id    The device id
   * @param token The device token
   */
  BasicDevice(final String id, final String token, final Timestamp register) throws InvalidDeviceTokenFormatException {
    super();
    this.deviceId = id;
    this.token = token;
    this.lastRegister = register;

    validateTokenFormat(token);

  }

  public static void validateTokenFormat(final String token) throws InvalidDeviceTokenFormatException {
    if (token == null) {
      throw new InvalidDeviceTokenFormatException("Device Token is null, and not the required 64 bytes...");
    }

    if (token.getBytes().length != 64) {
      throw new InvalidDeviceTokenFormatException("Device Token has a length of [" + token.getBytes().length + "] and not the required 64 bytes!");
    }
  }

  public void validateTokenFormat() throws InvalidDeviceTokenFormatException {
    validateTokenFormat(token);
  }

  /**
   * Getter
   *
   * @return the device id
   */
  public String getDeviceId() {
    return deviceId;
  }

  /**
   * Setter
   *
   * @param id the device id
   */
  public void setDeviceId(final String id) {
    this.deviceId = id;
  }

  /**
   * Getter
   *
   * @return the device token
   */
  public String getToken() {
    return token;
  }

  /**
   * Setter the device token
   *
   * @param token
   */
  public void setToken(final String token) {
    this.token = token;
  }

  /**
   * Getter
   *
   * @return the last register
   */
  public Timestamp getLastRegister() {
    return lastRegister;
  }

  public void setLastRegister(final Timestamp lastRegister) {
    this.lastRegister = lastRegister;
  }
}
