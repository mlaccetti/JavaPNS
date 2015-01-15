package javapns.notification.management;

import javapns.json.JSONException;
import javapns.json.JSONObject;
import javapns.notification.Payload;

/**
 * A payload template compatible with Apple Mobile Device Management's Config Payload specification (beta version).
 *
 * @author Sylvain Pedneault
 */
public abstract class MobileConfigPayload extends Payload {

  private static long serialuuid = 10000000;

  public MobileConfigPayload(int payloadVersion, String payloadType, String payloadOrganization, String payloadIdentifier, String payloadDisplayName) throws JSONException {
    this(payloadVersion, MobileConfigPayload.generateUUID(), payloadType, payloadOrganization, payloadIdentifier, payloadDisplayName);
  }

  public MobileConfigPayload(int payloadVersion, String payloadUUID, String payloadType, String payloadOrganization, String payloadIdentifier, String payloadDisplayName) throws JSONException {
    JSONObject payload = getPayload();
    payload.put("PayloadVersion", payloadVersion);
    payload.put("PayloadUUID", payloadUUID);
    payload.put("PayloadType", payloadType);
    payload.put("PayloadOrganization", payloadOrganization);
    payload.put("PayloadIdentifier", payloadIdentifier);
    payload.put("PayloadDisplayName", payloadDisplayName);
  }

  private static String generateUUID() {
    return System.nanoTime() + "." + ++serialuuid;
  }

  public void setPayloadDescription(String description) throws JSONException {
    getPayload().put("PayloadDescription", description);
  }

  public void setPayloadRemovalDisallowed(boolean disallowed) throws JSONException {
    getPayload().put("PayloadRemovalDisallowed", disallowed);
  }

}
