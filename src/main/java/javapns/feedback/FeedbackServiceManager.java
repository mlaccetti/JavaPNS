package javapns.feedback;

import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.DeviceFactory;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.devices.implementations.basic.BasicDeviceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.LinkedList;

/**
 * Class for interacting with a specific Feedback Service.
 *
 * @author kljajo, dgardon, Sylvain Pedneault
 */
public class FeedbackServiceManager {
  protected static final Logger logger = LoggerFactory.getLogger(FeedbackServiceManager.class);

  /* Length of the tuple sent by Apple */
  private static final int FEEDBACK_TUPLE_SIZE = 38;
  /*
   * Number of milliseconds to use as socket timeout.
   * Set to -1 to leave the timeout to its default setting.
   */
  private              int sslSocketTimeout    = 30 * 1000;
  @Deprecated
  private DeviceFactory deviceFactory;

  /**
   * Constructs a FeedbackServiceManager with a supplied DeviceFactory.
   *
   * @deprecated The DeviceFactory-based architecture is deprecated.
   */
  @Deprecated
  public FeedbackServiceManager(DeviceFactory deviceFactory) {
    setDeviceFactory(deviceFactory);
  }

  /**
   * Constructs a FeedbackServiceManager with a default basic DeviceFactory.
   */
  @SuppressWarnings("deprecation")
  public FeedbackServiceManager() {
    setDeviceFactory(new BasicDeviceFactory());
  }

  /**
   * Retrieve all devices which have un-installed the application w/Path to keystore
   *
   * @param server Connection information for the Apple server
   * @return List of Devices
   * @throws IOException
   * @throws FileNotFoundException
   * @throws CertificateException
   * @throws NoSuchAlgorithmException
   * @throws KeyStoreException
   * @throws KeyManagementException
   * @throws UnrecoverableKeyException
   */
  /**
   * @throws KeystoreException
   * @throws CommunicationException
   */
  public LinkedList<Device> getDevices(AppleFeedbackServer server) throws KeystoreException, CommunicationException {
    ConnectionToFeedbackServer connectionHelper = new ConnectionToFeedbackServer(server);
    SSLSocket socket = connectionHelper.getSSLSocket();
    return getDevices(socket);
  }

  /**
   * Retrieves the list of devices from an established SSLSocket.
   *
   * @param socket
   * @return Devices
   * @throws CommunicationException
   */
  private LinkedList<Device> getDevices(SSLSocket socket) throws CommunicationException {

    // Compute
    LinkedList<Device> listDev = null;
    try {
      InputStream socketStream = socket.getInputStream();
      if (sslSocketTimeout > 0) socket.setSoTimeout(sslSocketTimeout);

      // Read bytes
      byte[] b = new byte[1024];
      ByteArrayOutputStream message = new ByteArrayOutputStream();
      int nbBytes = 0;
      // socketStream.available can return 0
      // http://forums.sun.com/thread.jspa?threadID=5428561
      while ((nbBytes = socketStream.read(b, 0, 1024)) != -1) {
        message.write(b, 0, nbBytes);
      }

      listDev = new LinkedList<Device>();
      byte[] listOfDevices = message.toByteArray();
      int nbTuples = listOfDevices.length / FeedbackServiceManager.FEEDBACK_TUPLE_SIZE;
      FeedbackServiceManager.logger.debug("Found: [" + nbTuples + "]");
      for (int i = 0; i < nbTuples; i++) {
        int offset = i * FeedbackServiceManager.FEEDBACK_TUPLE_SIZE;

        // Build date
        int index = 0;
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        long anUnsignedInt = 0;

        firstByte = 0x000000FF & (int) listOfDevices[offset];
        secondByte = 0x000000FF & (int) listOfDevices[offset + 1];
        thirdByte = 0x000000FF & (int) listOfDevices[offset + 2];
        fourthByte = 0x000000FF & (int) listOfDevices[offset + 3];
        index = index + 4;
        anUnsignedInt = (long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte) & 0xFFFFFFFFL;
        Timestamp timestamp = new Timestamp(anUnsignedInt * 1000);

        // Build device token length
        int deviceTokenLength = listOfDevices[offset + 4] << 8 | listOfDevices[offset + 5];

        // Build device token
        String deviceToken = "";
        int octet = 0;
        for (int j = 0; j < 32; j++) {
          octet = 0x000000FF & (int) listOfDevices[offset + 6 + j];
          deviceToken = deviceToken.concat(String.format("%02x", octet));
        }

        // Build device and add to list
        /* Create a basic device, as we do not want to go through the factory and create a device in the actual database... */
        Device device = new BasicDevice();
        device.setToken(deviceToken);
        device.setLastRegister(timestamp);
        listDev.add(device);
        FeedbackServiceManager.logger.info("FeedbackManager retrieves one device :  " + timestamp + ";" + deviceTokenLength + ";" + deviceToken + ".");
      }

      // Close the socket and return the list

    } catch (Exception e) {
      FeedbackServiceManager.logger.debug("Caught exception fetching devices from Feedback Service");
      throw new CommunicationException("Problem communicating with Feedback service", e);
    } finally {
      try {
        socket.close();
      } catch (Exception e) {
      }
    }
    return listDev;
  }

  /**
   * @return a device factory
   * @deprecated The DeviceFactory-based architecture is deprecated.
   */
  @Deprecated
  public DeviceFactory getDeviceFactory() {
    return deviceFactory;
  }

  /**
   * @param deviceFactory
   * @deprecated The DeviceFactory-based architecture is deprecated.
   */
  @Deprecated
  public void setDeviceFactory(DeviceFactory deviceFactory) {
    this.deviceFactory = deviceFactory;
  }

  /**
   * Get the SSL socket timeout currently in use.
   *
   * @return the current SSL socket timeout value.
   */
  public int getSslSocketTimeout() {
    return sslSocketTimeout;
  }

  /**
   * Set the SSL socket timeout to use.
   *
   * @param sslSocketTimeout
   */
  public void setSslSocketTimeout(int sslSocketTimeout) {
    this.sslSocketTimeout = sslSocketTimeout;
  }

}