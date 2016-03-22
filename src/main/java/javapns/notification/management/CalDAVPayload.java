package javapns.notification.management;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An MDM payload for CalDAV.
 *
 * @author Sylvain Pedneault
 */
class CalDAVPayload extends MobileConfigPayload {

  public CalDAVPayload(final int payloadVersion, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName, final String calDAVHostName, final String calDAVUsername, final boolean calDAVUseSSL) throws JSONException {
    super(payloadVersion, "com.apple.caldav.account", payloadOrganization, payloadIdentifier, payloadDisplayName);
    final JSONObject payload = getPayload();
    payload.put("CalDAVHostName", calDAVHostName);
    payload.put("CalDAVUsername", calDAVUsername);
    payload.put("CalDAVUseSSL", calDAVUseSSL);
  }

  public void setCalDAVAccountDescription(final String value) throws JSONException {
    getPayload().put("CalDAVAccountDescription", value);
  }

  public void setCalDAVPassword(final String value) throws JSONException {
    getPayload().put("CalDAVPassword", value);
  }

  public void setCalDAVPort(final int value) throws JSONException {
    getPayload().put("CalDAVPort", value);
  }

  public void setCalDAVPrincipalURL(final String value) throws JSONException {
    getPayload().put("CalDAVPrincipalURL", value);
  }

}
