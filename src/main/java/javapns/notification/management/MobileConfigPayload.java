package javapns.notification.management;

import javapns.notification.Payload;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A payload template compatible with Apple Mobile Device Management's Config Payload specification (beta version).
 *
 * @author Sylvain Pedneault
 */
abstract class MobileConfigPayload extends Payload {
  private static long serialuuid = 10000000;

  MobileConfigPayload(final int payloadVersion, final String payloadType, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName) throws JSONException {
    this(payloadVersion, generateUUID(), payloadType, payloadOrganization, payloadIdentifier, payloadDisplayName);
  }

  private MobileConfigPayload(final int payloadVersion, final String payloadUUID, final String payloadType, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName) throws JSONException {
    super();
    final JSONObject payload = getPayload();
    payload.put("PayloadVersion", payloadVersion);
    payload.put("PayloadUUID", payloadUUID);
    payload.put("PayloadType", payloadType);
    payload.put("PayloadOrganization", payloadOrganization);
    payload.put("PayloadIdentifier", payloadIdentifier);
    payload.put("PayloadDisplayName", payloadDisplayName);
  }

  private static String generateUUID() {
    return System.nanoTime() + "." + (++serialuuid);
  }

  public void setPayloadDescription(final String description) throws JSONException {
    getPayload().put("PayloadDescription", description);
  }

  public void setPayloadRemovalDisallowed(final boolean disallowed) throws JSONException {
    getPayload().put("PayloadRemovalDisallowed", disallowed);
  }
}
