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

  private final PushNotificationManager notificationManager;

  private int responsePacketsReceived;

  /**
   * NOT FULLY IMPLEMENTED, DO NOT USE
   *
   * @param notificationManager
   * @param monitorContinously
   */
  private ResponsePacketReader(PushNotificationManager notificationManager, boolean monitorContinously) {
    this.notificationManager = notificationManager;

    if (monitorContinously) {
      runAsThread();
    } else {
      responsePacketsReceived = ResponsePacketReader.processResponses(notificationManager);
    }
  }

  /**
   * Read response packets from the current APNS connection and process them.
   *
   * @param notificationManager
   * @return the number of response packets received and processed
   */
  public static int processResponses(PushNotificationManager notificationManager) {
    List<ResponsePacket> responses = ResponsePacketReader.readResponses(notificationManager.getActiveSocket());
    ResponsePacketReader.handleResponses(responses, notificationManager);
    return responses.size();
  }

  /**
   * Read raw response packets from the provided socket.
   * <p/>
   * Note: this method automatically sets the socket's timeout
   * to TIMEOUT, so not to block the socket's input stream.
   *
   * @param socket
   * @return
   */
  private static List<ResponsePacket> readResponses(Socket socket) {
    List<ResponsePacket> responses = new Vector<ResponsePacket>();
    int previousTimeout = 0;
    try {
      /* Set socket timeout to avoid getting stuck on read() */
      try {
        previousTimeout = socket.getSoTimeout();
        socket.setSoTimeout(ResponsePacketReader.TIMEOUT);
      } catch (Exception e) {
      }
      InputStream input = socket.getInputStream();
      while (true) {
        ResponsePacket packet = ResponsePacketReader.readResponsePacketData(input);
        if (packet != null) responses.add(packet);
        else break;
      }

    } catch (Exception e) {
			/* Ignore exception, as we are expecting timeout exceptions because Apple might not reply anything */
      //System.out.println(e);
    }
		/* Reset socket timeout, just in case */
    try {
      socket.setSoTimeout(previousTimeout);
    } catch (Exception e) {
    }
    //System.out.println("Received "+responses.size()+" response packets");
    return responses;
  }

  private static void handleResponses(List<ResponsePacket> responses, PushNotificationManager notificationManager) {
    Map<Integer, PushedNotification> envelopes = notificationManager.getPushedNotifications();
    for (ResponsePacket response : responses) {
      response.linkToPushedNotification(notificationManager);
    }
  }

  private static ResponsePacket readResponsePacketData(InputStream input) throws IOException {
    int command = input.read();
    if (command < 0) return null;
    int status = input.read();
    if (status < 0) return null;

    int identifier_byte1 = input.read();
    if (identifier_byte1 < 0) return null;
    int identifier_byte2 = input.read();
    if (identifier_byte2 < 0) return null;
    int identifier_byte3 = input.read();
    if (identifier_byte3 < 0) return null;
    int identifier_byte4 = input.read();
    if (identifier_byte4 < 0) return null;
    int identifier = (identifier_byte1 << 24) + (identifier_byte2 << 16) + (identifier_byte3 << 8) + identifier_byte4;
    return new ResponsePacket(command, status, identifier);
  }

  /**
   * NOT FULLY IMPLEMENTED, DO NOT USE
   */
  private void runAsThread() {
    Thread thread = new Thread() {
      public void run() {
        monitor();
      }

    };
    thread.setDaemon(true);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();

  }

  private void monitor() {
    while (notificationManager.getActiveSocket().isConnected()) {
      responsePacketsReceived += ResponsePacketReader.processResponses(notificationManager);

    }
  }

  public int getResponsePacketsReceived() {
    return responsePacketsReceived;
  }

}
