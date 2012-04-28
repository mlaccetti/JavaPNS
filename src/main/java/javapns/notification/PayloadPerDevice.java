package javapns.notification;

import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.devices.implementations.basic.BasicDevice;

/**
 * A one-to-one link between a payload and device.
 * Provides support for a typical payload-per-device scenario.
 * 
 * @author Sylvain Pedneault
 */
public class PayloadPerDevice {
  private Payload payload;
  private Device device;

  public PayloadPerDevice(Payload payload, String token) throws InvalidDeviceTokenFormatException {
    super();
    this.payload = payload;
    this.device = new BasicDevice(token);
  }

  public PayloadPerDevice(Payload payload, Device device) {
    super();
    this.payload = payload;
    this.device = device;
  }

  public Payload getPayload() {
    return payload;
  }

  public Device getDevice() {
    return device;
  }
}