package javapns.notification.management;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An MDM payload for CalendarSubscription.
 *
 * @author Sylvain Pedneault
 */
class CalendarSubscriptionPayload extends MobileConfigPayload {

  public CalendarSubscriptionPayload(final int payloadVersion, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName, final String subCalAccountHostName, final boolean subCalAccountUseSSL) throws JSONException {
    super(payloadVersion, "com.apple.caldav.account", payloadOrganization, payloadIdentifier, payloadDisplayName);
    final JSONObject payload = getPayload();
    payload.put("SubCalAccountHostName", subCalAccountHostName);
    payload.put("SubCalAccountUseSSL", subCalAccountUseSSL);
  }

  public void setSubCalAccountDescription(final String value) throws JSONException {
    getPayload().put("SubCalAccountDescription", value);
  }

  public void setSubCalAccountUsername(final String value) throws JSONException {
    getPayload().put("SubCalAccountUsername", value);
  }

  public void setSubCalAccountPassword(final String value) throws JSONException {
    getPayload().put("SubCalAccountPassword", value);
  }

  public void setSubCalAccountUseSSL(final boolean value) throws JSONException {
    getPayload().put("SubCalAccountUseSSL", value);
  }

}
