package javapns.notification;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A Newsstand-specific payload compatible with the Apple Push Notification Service.
 *
 * @author Sylvain Pedneault
 */
public class NewsstandNotificationPayload extends Payload {
  /* The application Dictionnary */
  private final JSONObject apsDictionary;

  /**
   * Create a default payload with a blank "aps" dictionary.
   */
  private NewsstandNotificationPayload() {
    super();
    this.apsDictionary = new JSONObject();
    try {
      final JSONObject payload = getPayload();
      payload.put("aps", this.apsDictionary);
    } catch (final JSONException e) {
      e.printStackTrace();
    }
  }

  /**
   * Create a pre-defined payload with a content-available property set to 1.
   *
   * @return a ready-to-send newsstand payload
   */
  public static NewsstandNotificationPayload contentAvailable() {
    final NewsstandNotificationPayload payload = complex();
    try {
      payload.addContentAvailable();
    } catch (final JSONException e) {
      // empty
    }
    return payload;
  }

  /**
   * Create an empty payload which you can configure later (most users should not use this).
   * This method is usually used to create complex or custom payloads.
   * Note: the payload actually contains the default "aps"
   * dictionary required by Newsstand.
   *
   * @return a blank payload that can be customized
   */
  private static NewsstandNotificationPayload complex() {
    final NewsstandNotificationPayload payload = new NewsstandNotificationPayload();
    return payload;
  }

  private void addContentAvailable() throws JSONException {
    addContentAvailable(1);
  }

  private void addContentAvailable(final int contentAvailable) throws JSONException {
    logger.debug("Adding ContentAvailable [" + contentAvailable + "]");
    this.apsDictionary.put("content-available", contentAvailable);
  }

}
