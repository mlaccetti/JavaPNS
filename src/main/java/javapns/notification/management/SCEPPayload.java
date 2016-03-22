package javapns.notification.management;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An MDM payload for SCEP (Simple Certificate Enrollment Protocol).
 *
 * @author Sylvain Pedneault
 */
class SCEPPayload extends MobileConfigPayload {
  public SCEPPayload(final int payloadVersion, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName, final String url) throws JSONException {
    super(payloadVersion, "com.apple.encrypted-profile-service", payloadOrganization, payloadIdentifier, payloadDisplayName);
    final JSONObject payload = getPayload();
    payload.put("URL", url);
  }

  public void setName(final String value) throws JSONException {
    getPayload().put("Name", value);
  }

  public void setSubject(final String value) throws JSONException {
    final String[] parts = value.split("/");
    final List<String[]> list = new ArrayList<>();
    for (final String part : parts) {
      final String[] subparts = value.split("=");
      list.add(subparts);
    }
    final String[][] subject = list.toArray(new String[0][0]);
    setSubject(subject);
  }

  private void setSubject(final String[][] value) throws JSONException {
    getPayload().put("Subject", value);
  }

  public void setChallenge(final String value) throws JSONException {
    getPayload().put("Challenge", value);
  }

  public void setKeysize(final int value) throws JSONException {
    getPayload().put("Keysize", value);
  }

  public void setKeyType(final String value) throws JSONException {
    getPayload().put("Key Type", value);
  }

  public void setKeyUsage(final int value) throws JSONException {
    getPayload().put("Key Usage", value);
  }

  public JSONObject addSubjectAltName() throws JSONException {
    final JSONObject object = new JSONObject();
    getPayload().put("SubjectAltName", object);
    return object;
  }

  public JSONObject addGetCACaps() throws JSONException {
    final JSONObject object = new JSONObject();
    getPayload().put("GetCACaps", object);
    return object;
  }
}
