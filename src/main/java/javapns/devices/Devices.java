package javapns.devices;

import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.PayloadPerDevice;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Devices {
  public static List<Device> asDevices(final Object rawList) {
    final List<Device> list = new Vector<>();
    if (rawList == null) {
      return list;
    }

    if (rawList instanceof List) {
      final List devices = (List) rawList;
      if (devices.isEmpty()) {
        return list;
      }

      final Object firstDevice = devices.get(0);
      if (firstDevice instanceof Device) {
        //noinspection unchecked
        return devices;
      } else if (firstDevice instanceof String) {
        for (final Object token : devices) {
          final BasicDevice device = new BasicDevice();
          device.setToken((String) token);
          list.add(device);
        }
      }
    } else if (rawList instanceof String[]) {
      final String[] tokens = (String[]) rawList;
      for (final String token : tokens) {
        final BasicDevice device = new BasicDevice();
        device.setToken(token);
        list.add(device);
      }
    } else if (rawList instanceof Device[]) {
      final Device[] dvs = (Device[]) rawList;
      return Arrays.asList(dvs);
    } else if (rawList instanceof String) {
      final BasicDevice device = new BasicDevice();
      device.setToken((String) rawList);
      list.add(device);
    } else if (rawList instanceof Device) {
      list.add((Device) rawList);
    } else {
      throw new IllegalArgumentException("Device list type not supported. Supported types are: String[], List<String>, Device[], List<Device>, String and Device");
    }
    return list;
  }

  public static List<PayloadPerDevice> asPayloadsPerDevices(final Object rawList) {
    final List<PayloadPerDevice> list = new Vector<>();
    if (rawList == null) {
      return list;
    }
    if (rawList instanceof List) {
      final List devices = (List) rawList;
      if (devices.isEmpty()) {
        return list;
      }
      //noinspection unchecked
      return devices;
    } else if (rawList instanceof PayloadPerDevice[]) {
      final PayloadPerDevice[] dvs = (PayloadPerDevice[]) rawList;
      return Arrays.asList(dvs);
    } else if (rawList instanceof PayloadPerDevice) {
      list.add((PayloadPerDevice) rawList);
    } else {
      throw new IllegalArgumentException("PayloadPerDevice list type not supported. Supported types are: PayloadPerDevice[], List<PayloadPerDevice> and PayloadPerDevice");
    }
    return list;
  }
}
