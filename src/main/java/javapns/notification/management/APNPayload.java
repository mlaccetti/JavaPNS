package javapns.notification.management;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * An MDM payload for APN (Access Point Name).
 *
 * @author Sylvain Pedneault
 */
class APNPayload extends MobileConfigPayload {
  public APNPayload(final int payloadVersion, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName, final Map<String, String> defaultsData, final String defaultsDomainName, final Map<String, String>[] apns, final String apn, final String username) throws JSONException {
    super(payloadVersion, "com.apple.apn.managed", payloadOrganization, payloadIdentifier, payloadDisplayName);
    final JSONObject payload = getPayload();
    payload.put("DefaultsData", defaultsData);
    payload.put("defaultsDomainName", defaultsDomainName);
    for (final Map<String, String> apnsEntry : apns) {
      payload.put("apns", apnsEntry);
    }
    payload.put("apn", apn);
    payload.put("username", username);
  }

  public void setPassword(final APNPayload value) throws JSONException {
    getPayload().put("password", value);
  }

  public void setProxy(final String value) throws JSONException {
    getPayload().put("proxy", value);
  }

  public void setProxyPort(final int value) throws JSONException {
    getPayload().put("proxyPort", value);
  }

}
