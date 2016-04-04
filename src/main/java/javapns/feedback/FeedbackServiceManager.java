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

  private static final Logger logger = LoggerFactory.getLogger(FeedbackServiceManager.class);

  /* Length of the tuple sent by Apple */
  private static final int FEEDBACK_TUPLE_SIZE = 38;

  @Deprecated
  private DeviceFactory deviceFactory;

  /**
   * Constructs a FeedbackServiceManager with a supplied DeviceFactory.
   *
   * @deprecated The DeviceFactory-based architecture is deprecated.
   */
  @Deprecated
  private FeedbackServiceManager(final DeviceFactory deviceFactory) {
    this.setDeviceFactory(deviceFactory);
  }

  /**
   * Constructs a FeedbackServiceManager with a default basic DeviceFactory.
   */
  @SuppressWarnings("deprecation")
  public FeedbackServiceManager() {
    this.setDeviceFactory(new BasicDeviceFactory());
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
  public LinkedList<Device> getDevices(final AppleFeedbackServer server) throws KeystoreException, CommunicationException {
    final ConnectionToFeedbackServer connectionHelper = new ConnectionToFeedbackServer(server);
    final SSLSocket socket = connectionHelper.getSSLSocket();
    return getDevices(socket);
  }

  /**
   * Retrieves the list of devices from an established SSLSocket.
   *
   * @param socket
   * @return Devices
   * @throws CommunicationException
   */
  private LinkedList<Device> getDevices(final SSLSocket socket) throws CommunicationException {

    // Compute
    LinkedList<Device> listDev = null;
    try {
      final InputStream socketStream = socket.getInputStream();

      // Read bytes
      final byte[] b = new byte[1024];
      final ByteArrayOutputStream message = new ByteArrayOutputStream();
      int nbBytes;
      // socketStream.available can return 0
      // http://forums.sun.com/thread.jspa?threadID=5428561
      while ((nbBytes = socketStream.read(b, 0, 1024)) != -1) {
        message.write(b, 0, nbBytes);
      }

      listDev = new LinkedList<>();
      final byte[] listOfDevices = message.toByteArray();
      final int nbTuples = listOfDevices.length / FEEDBACK_TUPLE_SIZE;
      logger.debug("Found: [" + nbTuples + "]");
      for (int i = 0; i < nbTuples; i++) {
        final int offset = i * FEEDBACK_TUPLE_SIZE;

        // Build date
        final int firstByte;
        final int secondByte;
        final int thirdByte;
        final int fourthByte;
        final long anUnsignedInt;

        firstByte = (0x000000FF & ((int) listOfDevices[offset]));
        secondByte = (0x000000FF & ((int) listOfDevices[offset + 1]));
        thirdByte = (0x000000FF & ((int) listOfDevices[offset + 2]));
        fourthByte = (0x000000FF & ((int) listOfDevices[offset + 3]));
        anUnsignedInt = ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
        final Timestamp timestamp = new Timestamp(anUnsignedInt * 1000);

        // Build device token length
        final int deviceTokenLength = listOfDevices[offset + 4] << 8 | listOfDevices[offset + 5];

        // Build device token
        String deviceToken = "";
        int octet;
        for (int j = 0; j < 32; j++) {
          octet = (0x000000FF & ((int) listOfDevices[offset + 6 + j]));
          deviceToken = deviceToken.concat(String.format("%02x", octet));
        }

        // Build device and add to list
        /* Create a basic device, as we do not want to go through the factory and create a device in the actual database... */
        final Device device = new BasicDevice();
        device.setToken(deviceToken);
        device.setLastRegister(timestamp);
        listDev.add(device);
        logger.info("FeedbackManager retrieves one device :  " + timestamp + ";" + deviceTokenLength + ";" + deviceToken + ".");
      }

      // Close the socket and return the list

    } catch (final Exception e) {
      logger.debug("Caught exception fetching devices from Feedback Service");
      throw new CommunicationException("Problem communicating with Feedback service", e);
    } finally {
      try {
        socket.close();
      } catch (final Exception e) {
        // empty
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
  private void setDeviceFactory(final DeviceFactory deviceFactory) {
    this.deviceFactory = deviceFactory;
  }
}
