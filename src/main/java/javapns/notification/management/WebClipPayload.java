package javapns.notification.management;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An MDM payload for WebClip.
 *
 * @author Sylvain Pedneault
 */
class WebClipPayload extends MobileConfigPayload {
  public WebClipPayload(final int payloadVersion, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName, final String url, final String label) throws JSONException {
    super(payloadVersion, "com.apple.webClip.managed", payloadOrganization, payloadIdentifier, payloadDisplayName);
    final JSONObject payload = getPayload();
    payload.put("URL", url);
    payload.put("Label", label);
  }

  public void setIcon(final Object data) throws JSONException {
    getPayload().put("Icon", data);
  }

  public void setIsRemovable(final boolean value) throws JSONException {
    getPayload().put("IsRemovable", value);
  }
}
