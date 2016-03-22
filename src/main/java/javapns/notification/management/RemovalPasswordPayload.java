package javapns.notification.management;

import org.json.JSONException;

/**
 * An MDM payload for RemovalPassword.
 *
 * @author Sylvain Pedneault
 */
class RemovalPasswordPayload extends MobileConfigPayload {
  public RemovalPasswordPayload(final int payloadVersion, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName) throws JSONException {
    super(payloadVersion, "com.apple.profileRemovalPassword", payloadOrganization, payloadIdentifier, payloadDisplayName);
  }

  public void setRemovalPasword(final String value) throws JSONException {
    getPayload().put("RemovalPassword", value);
  }
}
