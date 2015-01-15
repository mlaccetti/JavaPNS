package javapns.devices;

import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.PayloadPerDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Devices {

  public static final  Logger logger                 = LoggerFactory.getLogger(Devices.class);
  private static final int    UNEFFICIENCY_THRESHOLD = 500;

  @SuppressWarnings("unchecked")
  public static List<Device> asDevices(Object rawList) {
    List<Device> list = new Vector<Device>();
    if (rawList == null) return list;

    if (rawList instanceof List) {
      List devices = (List) rawList;
      if (devices == null || devices.size() == 0) return list;
      Object firstDevice = devices.get(0);
      if (firstDevice instanceof Device) return devices;
      else if (firstDevice instanceof String) {
        for (Object token : devices) {
          BasicDevice device = new BasicDevice();
          device.setToken((String) token);
          list.add(device);
        }
      }
    } else if (rawList instanceof String[]) {
      String[] tokens = (String[]) rawList;
      for (String token : tokens) {
        BasicDevice device = new BasicDevice();
        device.setToken(token);
        list.add(device);
      }
    } else if (rawList instanceof Device[]) {
      Device[] dvs = (Device[]) rawList;
      return Arrays.asList(dvs);
    } else if (rawList instanceof String) {
      BasicDevice device = new BasicDevice();
      device.setToken((String) rawList);
      list.add(device);
    } else if (rawList instanceof Device) {
      list.add((Device) rawList);
    } else
      throw new IllegalArgumentException("Device list type not supported. Supported types are: String[], List<String>, Device[], List<Device>, String and Device");
    return list;
  }

  @SuppressWarnings("unchecked")
  public static List<PayloadPerDevice> asPayloadsPerDevices(Object rawList) {
    List<PayloadPerDevice> list = new Vector<PayloadPerDevice>();
    if (rawList == null) return list;
    if (rawList instanceof List) {
      List devices = (List) rawList;
      if (devices == null || devices.size() == 0) return list;
      return devices;
    } else if (rawList instanceof PayloadPerDevice[]) {
      PayloadPerDevice[] dvs = (PayloadPerDevice[]) rawList;
      return Arrays.asList(dvs);
    } else if (rawList instanceof PayloadPerDevice) {
      list.add((PayloadPerDevice) rawList);
    } else
      throw new IllegalArgumentException("PayloadPerDevice list type not supported. Supported types are: PayloadPerDevice[], List<PayloadPerDevice> and PayloadPerDevice");
    return list;
  }

  public static <T> void evaluateEfficiency(List<T> list) {
    int size = list.size();
    if (size > Devices.UNEFFICIENCY_THRESHOLD) {
      Devices.logger.warn("DEVICES WARNING: Pushing more than " + Devices.UNEFFICIENCY_THRESHOLD + " notifications (your attempt: " + size + ") is not recommended with this method.\n You should consider using javapns.notification.transmission.NotificationThreads for increased reliability and efficiency.");
    }
  }

}
