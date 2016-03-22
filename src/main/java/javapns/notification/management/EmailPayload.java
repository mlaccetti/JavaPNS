package javapns.notification.management;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An MDM payload for Email.
 *
 * @author Sylvain Pedneault
 */
class EmailPayload extends MobileConfigPayload {
  public EmailPayload(final int payloadVersion, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName, final String emailAccountType, final String emailAddress, final String incomingMailServerAuthentication, final String incomingMailServerHostName, final String incomingMailServerUsername, final String outgoingMailServerAuthentication, final String outgoingMailServerHostName, final String outgoingMailServerUsername) throws JSONException {
    super(payloadVersion, "com.apple.mail.managed", payloadOrganization, payloadIdentifier, payloadDisplayName);
    final JSONObject payload = getPayload();
    payload.put("EmailAccountType", emailAccountType);
    payload.put("EmailAddress", emailAddress);
    payload.put("IncomingMailServerAuthentication", incomingMailServerAuthentication);
    payload.put("IncomingMailServerHostName", incomingMailServerHostName);
    payload.put("IncomingMailServerUsername", incomingMailServerUsername);
    payload.put("OutgoingMailServerAuthentication", outgoingMailServerAuthentication);
    payload.put("OutgoingMailServerHostName", outgoingMailServerHostName);
    payload.put("OutgoingMailServerUsername", outgoingMailServerUsername);
  }

  public void setEmailAccountDescription(final String value) throws JSONException {
    getPayload().put("EmailAccountDescription", value);
  }

  public void setEmailAccountName(final String value) throws JSONException {
    getPayload().put("EmailAccountName", value);
  }

  public void setIncomingMailServerPortNumber(final int value) throws JSONException {
    getPayload().put("IncomingMailServerPortNumber", value);
  }

  public void setIncomingMailServerUseSSL(final boolean value) throws JSONException {
    getPayload().put("IncomingMailServerUseSSL", value);
  }

  public void setIncomingPassword(final String value) throws JSONException {
    getPayload().put("IncomingPassword", value);
  }

  public void setOutgoingPassword(final String value) throws JSONException {
    getPayload().put("OutgoingPassword", value);
  }

  public void setOutgoingPasswwordSameAsIncomingPassword(final boolean value) throws JSONException {
    getPayload().put("OutgoingPasswwordSameAsIncomingPassword", value);
  }

  public void setOutgoingMailServerPortNumber(final int value) throws JSONException {
    getPayload().put("OutgoingMailServerPortNumber", value);
  }

  public void setOutgoingMailServerUseSSL(final boolean value) throws JSONException {
    getPayload().put("OutgoingMailServerUseSSL", value);
  }
}
