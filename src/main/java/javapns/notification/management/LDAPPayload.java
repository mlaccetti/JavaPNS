package javapns.notification.management;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An MDM payload for LDAP.
 *
 * @author Sylvain Pedneault
 */
class LDAPPayload extends MobileConfigPayload {
  public LDAPPayload(final int payloadVersion, final String payloadOrganization, final String payloadIdentifier, final String payloadDisplayName, final String ldapAccountHostName, final boolean ldapAccountUseSSL) throws JSONException {
    super(payloadVersion, "com.apple.webClip.managed", payloadOrganization, payloadIdentifier, payloadDisplayName);
    final JSONObject payload = getPayload();
    payload.put("LDAPAccountHostName", ldapAccountHostName);
    payload.put("LDAPAccountUseSSL", ldapAccountUseSSL);
  }

  public void setLDAPAccountDescription(final boolean value) throws JSONException {
    getPayload().put("LDAPAccountDescription", value);
  }

  public void setLDAPAccountUserName(final boolean value) throws JSONException {
    getPayload().put("LDAPAccountUserName", value);
  }

  public void setLDAPAccountPassword(final boolean value) throws JSONException {
    getPayload().put("LDAPAccountPassword", value);
  }

  public JSONObject addSearchSettings(final String ldapSearchSettingSearchBase, final String ldapSearchSettingScope) throws JSONException {
    return addSearchSettings(ldapSearchSettingSearchBase, ldapSearchSettingScope, null);
  }

  public JSONObject addSearchSettings(final String ldapSearchSettingSearchBase, final int ldapSearchSettingScope) throws JSONException {
    return addSearchSettings(ldapSearchSettingSearchBase, ldapSearchSettingScope, null);
  }

  private JSONObject addSearchSettings(final String ldapSearchSettingSearchBase, final int ldapSearchSettingScope, final String ldapSearchSettingDescription) throws JSONException {
    return addSearchSettings(ldapSearchSettingSearchBase, ldapSearchSettingScope == 0 ? "LDAPSearchSettingScopeBase" : ldapSearchSettingScope == 1 ? "LDAPSearchSettingScopeBase" : "LDAPSearchSettingScopeSubtree", ldapSearchSettingDescription);
  }

  private JSONObject addSearchSettings(final String ldapSearchSettingSearchBase, final String ldapSearchSettingScope, final String ldapSearchSettingDescription) throws JSONException {
    final JSONObject payload = getPayload();
    final JSONObject searchSettings = new JSONObject();
    payload.put("LDAPSearchSettings", searchSettings);
    searchSettings.put("LDAPSearchSettingSearchBase", ldapSearchSettingSearchBase);
    searchSettings.put("LDAPSearchSettingScope", ldapSearchSettingScope);
    if (ldapSearchSettingDescription != null) {
      searchSettings.put("LDAPSearchSettingDescription", ldapSearchSettingDescription);
    }
    return searchSettings;
  }
}
