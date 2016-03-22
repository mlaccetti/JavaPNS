package javapns.notification;

import org.json.JSONException;

public class PushNotificationBigPayload extends PushNotificationPayload {
  /* Maximum total length (serialized) of a payload */
  private static final int MAXIMUM_PAYLOAD_LENGTH = 2048;

  private PushNotificationBigPayload() {
    super();
  }

  private PushNotificationBigPayload(final String rawJSON) throws JSONException {
    super(rawJSON);
  }

  public static PushNotificationBigPayload complex() {
    return new PushNotificationBigPayload();
  }

  public static PushNotificationBigPayload fromJSON(final String rawJSON) throws JSONException {
    return new PushNotificationBigPayload(rawJSON);
  }

  /**
   * Return the maximum payload size in bytes.
   * For APNS payloads, since iOS8, this method returns 2048.
   *
   * @return the maximum payload size in bytes (2048)
   */
  @Override
  public int getMaximumPayloadSize() {
    return MAXIMUM_PAYLOAD_LENGTH;
  }
}
