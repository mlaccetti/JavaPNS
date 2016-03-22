package javapns.notification.management;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An MDM payload for Wi-Fi.
 *
 * @author Sylvain Pedneault
 */
class WiFiPayload extends MobileConfigPayload {
  public WiFiPayload(final int payloadVersion, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName, final String SSID_STR, final boolean hiddenNetwork, final String encryptionType) throws JSONException {
    super(payloadVersion, "com.apple.wifi.managed", payloadOrganization, payloadIdentifier, payloadDisplayName);
    final JSONObject payload = getPayload();
    payload.put("SSID_STR", SSID_STR);
    payload.put("HIDDEN_NETWORK", hiddenNetwork);
    payload.put("EncryptionType", encryptionType);
  }

  public void setPassword(final String value) throws JSONException {
    getPayload().put("Password", value);
  }

  public JSONObject addEAPClientConfiguration() throws JSONException {
    final JSONObject object = new JSONObject();
    getPayload().put("EAPClientConfiguration", object);
    return object;
  }
}
