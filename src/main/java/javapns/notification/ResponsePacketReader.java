package javapns.notification;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Class for reading response packets from an APNS connection.
 * See Apple's documentation on enhanced notification format.
 *
 * @author Sylvain Pedneault
 */
class ResponsePacketReader {
  /* The number of seconds to wait for a response */
  private static final int TIMEOUT = 5 * 1000;

  /**
   * Read response packets from the current APNS connection and process them.
   *
   * @param notificationManager
   * @return the number of response packets received and processed
   */
  public static int processResponses(final PushNotificationManager notificationManager) {
    final List<ResponsePacket> responses = readResponses(notificationManager.getActiveSocket());
    handleResponses(responses, notificationManager);
    return responses.size();
  }

  /**
   * Read raw response packets from the provided socket.
   * <p>
   * Note: this method automatically sets the socket's timeout
   * to TIMEOUT, so not to block the socket's input stream.
   *
   * @param socket
   * @return
   */
  private static List<ResponsePacket> readResponses(final Socket socket) {
    final List<ResponsePacket> responses = new Vector<>();
    int previousTimeout = 0;
    try {
      /* Set socket timeout to avoid getting stuck on read() */
      try {
        previousTimeout = socket.getSoTimeout();
        socket.setSoTimeout(TIMEOUT);
      } catch (final Exception e) {
        // empty
      }
      final InputStream input = socket.getInputStream();
      while (true) {
        final ResponsePacket packet = readResponsePacketData(input);
        if (packet != null) {
          responses.add(packet);
        } else {
          break;
        }
      }

    } catch (final Exception e) {
      /* Ignore exception, as we are expecting timeout exceptions because Apple might not reply anything */
      //System.out.println(e);
    }
    /* Reset socket timeout, just in case */
    try {
      socket.setSoTimeout(previousTimeout);
    } catch (final Exception e) {
      // empty
    }
    //System.out.println("Received "+responses.size()+" response packets");
    return responses;
  }

  private static void handleResponses(final List<ResponsePacket> responses, final PushNotificationManager notificationManager) {
    final Map<Integer, PushedNotification> envelopes = notificationManager.getPushedNotifications();
    for (final ResponsePacket response : responses) {
      response.linkToPushedNotification(notificationManager);
    }
  }

  private static ResponsePacket readResponsePacketData(final InputStream input) throws IOException {
    final int command = input.read();
    if (command < 0) {
      return null;
    }
    final int status = input.read();
    if (status < 0) {
      return null;
    }

    final int identifier_byte1 = input.read();
    if (identifier_byte1 < 0) {
      return null;
    }
    final int identifier_byte2 = input.read();
    if (identifier_byte2 < 0) {
      return null;
    }
    final int identifier_byte3 = input.read();
    if (identifier_byte3 < 0) {
      return null;
    }
    final int identifier_byte4 = input.read();
    if (identifier_byte4 < 0) {
      return null;
    }
    final int identifier = (identifier_byte1 << 24) + (identifier_byte2 << 16) + (identifier_byte3 << 8) + (identifier_byte4);
    return new ResponsePacket(command, status, identifier);
  }
}
