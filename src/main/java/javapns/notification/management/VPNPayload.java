package javapns.notification.management;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An MDM payload for VPN.
 *
 * @author Sylvain Pedneault
 */
class VPNPayload extends MobileConfigPayload {
  public static final String VPNTYPE_L2TP = "L2TP";
  public static final String VPNTYPE_PPTP = "PPTP";
  public static final String VPNTYPE_IP_SEC = "IPSec";

  public VPNPayload(final int payloadVersion, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName, final String userDefinedName, final boolean overridePrimary, final String vpnType) throws JSONException {
    super(payloadVersion, "com.apple.vpn.managed", payloadOrganization, payloadIdentifier, payloadDisplayName);
    final JSONObject payload = getPayload();
    payload.put("UserDefinedName", userDefinedName);
    payload.put("OverridePrimary", overridePrimary);
    payload.put("VPNType", vpnType);
  }

  public JSONObject addPPP() throws JSONException {
    final JSONObject object = new JSONObject();
    getPayload().put("PPP", object);
    return object;
  }

  public JSONObject addIPSec() throws JSONException {
    final JSONObject object = new JSONObject();
    getPayload().put("IPSec", object);
    return object;
  }
}
