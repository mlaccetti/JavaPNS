package javapns.notification;

import javapns.notification.exceptions.PayloadMaxSizeExceededException;
import javapns.notification.exceptions.PayloadMaxSizeProbablyExceededException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Abstract class representing a payload that can be transmitted to Apple.
 * <p>
 * By default, this class has no payload content at all.  Subclasses are
 * responsible for imposing specific content based on the specifications
 * they are intended to implement (such as the 'aps' dictionnary for APS
 * payloads).
 *
 * @author Sylvain Pedneault
 */
public abstract class Payload {
  static final Logger logger = LoggerFactory.getLogger(Payload.class);

  /* Character encoding specified by Apple documentation */
  private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
  private static final String ADDING_CUSTOM_DICTIONARY = "Adding custom Dictionary [";
  private static final String DELIMITER_START = "] = [";
  private static final String DELIMITED_END = "]";
  /* The root Payload */
  private final JSONObject payload;

  /* Character encoding to use for streaming the payload (should be UTF-8) */
  private String characterEncoding = DEFAULT_CHARACTER_ENCODING;

  /* Number of seconds after which this payload should expire */
  @SuppressWarnings("PointlessArithmeticExpression")
  private int expiry = 1 * 24 * 60 * 60;

  private boolean payloadSizeEstimatedWhenAdding = false;

  private int preSendConfiguration = 0;

  /**
   * Construct a Payload object with a blank root JSONObject
   */
  protected Payload() {
    super();
    this.payload = new JSONObject();
  }

  /**
   * Construct a Payload object from a JSON-formatted string
   *
   * @param rawJSON a JSON-formatted string (ex: {"aps":{"alert":"Hello World!"}} )
   * @throws JSONException thrown if a exception occurs while parsing the JSON string
   */
  Payload(final String rawJSON) throws JSONException {
    super();
    this.payload = new JSONObject(rawJSON);
  }

  /**
   * Get the actual JSON object backing this payload.
   *
   * @return a JSONObject
   */
  public JSONObject getPayload() {
    return this.payload;
  }

  /**
   * Add a custom dictionnary with a string value
   *
   * @param name
   * @param value
   * @throws JSONException
   */
  public void addCustomDictionary(final String name, final String value) throws JSONException {
    logger.debug(ADDING_CUSTOM_DICTIONARY + name + DELIMITER_START + value + DELIMITED_END);
    put(name, value, payload, false);
  }

  /**
   * Add a custom dictionnary with a int value
   *
   * @param name
   * @param value
   * @throws JSONException
   */
  public void addCustomDictionary(final String name, final int value) throws JSONException {
    logger.debug(ADDING_CUSTOM_DICTIONARY + name + DELIMITER_START + value + DELIMITED_END);
    put(name, value, payload, false);
  }

  /**
   * Add a custom dictionnary with multiple values
   *
   * @param name
   * @param values
   * @throws JSONException
   */
  public void addCustomDictionary(final String name, final List values) throws JSONException {
    logger.debug(ADDING_CUSTOM_DICTIONARY + name + "] = (list)");
    put(name, values, payload, false);
  }

  /**
   * Add a custom dictionnary with object
   *
   * @param name
   * @param value
   * @throws JSONException
   */
  public void addCustomDictionary(final String name, final Object value) throws JSONException {
    logger.debug(ADDING_CUSTOM_DICTIONARY + name + DELIMITER_START + value + DELIMITED_END);
    put(name, value, payload, false);
  }

  /**
   * Get the string representation
   */
  public String toString() {
    return this.payload.toString();
  }

  void verifyPayloadIsNotEmpty() {
    if (getPreSendConfiguration() != 0) {
      return;
    }
    if (toString().equals("{}")) {
      throw new IllegalArgumentException("Payload cannot be empty");
    }
  }

  /**
   * Get this payload as a byte array using the preconfigured character encoding.
   *
   * @return byte[] bytes ready to be streamed directly to Apple servers
   */
  public byte[] getPayloadAsBytes() throws Exception {
    final byte[] payloadBytesUnchecked = getPayloadAsBytesUnchecked();
    validateMaximumPayloadSize(payloadBytesUnchecked.length);
    return payloadBytesUnchecked;
  }

  /**
   * Get this payload as a byte array using the preconfigured character encoding.
   * This method does NOT check if the payload exceeds the maximum payload length.
   *
   * @return byte[] bytes ready to be streamed directly to Apple servers (but that might exceed the maximum size limit)
   */
  private byte[] getPayloadAsBytesUnchecked() throws Exception {
    byte[] bytes;
    try {
      bytes = toString().getBytes(characterEncoding);
    } catch (final Exception ex) {
      bytes = toString().getBytes();
    }
    return bytes;
  }

  /**
   * Get the number of bytes that the payload will occupy when streamed.
   *
   * @return a number of bytes
   * @throws Exception
   */
  public int getPayloadSize() throws Exception {
    return getPayloadAsBytesUnchecked().length;
  }

  /**
   * Check if the payload exceeds the maximum size allowed.
   * The maximum size allowed is returned by the getMaximumPayloadSize() method.
   *
   * @return true if the payload exceeds the maximum size allowed, false otherwise
   */
  private boolean isPayloadTooLong() {
    try {
      final byte[] bytes = getPayloadAsBytesUnchecked();
      if (bytes.length > getMaximumPayloadSize()) {
        return true;
      }
    } catch (final Exception e) {
      // empty
    }
    return false;
  }

  /**
   * Estimate the size that this payload will take after adding a given property.
   * For performance reasons, this estimate is not as reliable as actually adding
   * the property and checking the payload size afterwards.
   * <p>
   * Currently works well with strings and numbers.
   *
   * @param propertyName  the name of the property to use for calculating the estimation
   * @param propertyValue the value of the property to use for calculating the estimation
   * @return an estimated payload size if the property were to be added to the payload
   */
  private int estimatePayloadSizeAfterAdding(final String propertyName, final Object propertyValue) {
    try {
      int estimatedSize = getPayloadAsBytesUnchecked().length;
      if (propertyName != null && propertyValue != null) {
        estimatedSize += 6; // ,"":""
        estimatedSize += propertyName.getBytes(getCharacterEncoding()).length;
        int estimatedValueSize = 0;

        if (propertyValue instanceof String || propertyValue instanceof Number) {
          estimatedValueSize = propertyValue.toString().getBytes(getCharacterEncoding()).length;
        }

        estimatedSize += estimatedValueSize;
      }
      return estimatedSize;
    } catch (final Exception e) {
      try {
        return getPayloadSize();
      } catch (final Exception e1) {
        return 0;
      }
    }
  }

  /**
   * Validate if the estimated payload size after adding a given property will be allowed.
   * For performance reasons, this estimate is not as reliable as actually adding
   * the property and checking the payload size afterwards.
   *
   * @param propertyName  the name of the property to use for calculating the estimation
   * @param propertyValue the value of the property to use for calculating the estimation
   * @return true if the payload size is not expected to exceed the maximum allowed, false if it might be too big
   */
  public boolean isEstimatedPayloadSizeAllowedAfterAdding(final String propertyName, final Object propertyValue) {
    final int maximumPayloadSize = getMaximumPayloadSize();
    final int estimatedPayloadSize = estimatePayloadSizeAfterAdding(propertyName, propertyValue);
    return estimatedPayloadSize <= maximumPayloadSize;
  }

  /**
   * Validate that the payload does not exceed the maximum size allowed.
   * If the limit is exceeded, a PayloadMaxSizeExceededException is thrown.
   *
   * @param currentPayloadSize the total size of the payload in bytes
   * @throws PayloadMaxSizeExceededException if the payload exceeds the maximum size allowed
   */
  private void validateMaximumPayloadSize(final int currentPayloadSize) throws PayloadMaxSizeExceededException {
    final int maximumPayloadSize = getMaximumPayloadSize();
    if (currentPayloadSize > maximumPayloadSize) {
      throw new PayloadMaxSizeExceededException(maximumPayloadSize, currentPayloadSize);
    }
  }

  /**
   * Puts a property in a JSONObject, while possibly checking for estimated payload size violation.
   *
   * @param propertyName  the name of the property to use for calculating the estimation
   * @param propertyValue the value of the property to use for calculating the estimation
   * @param object        the JSONObject to put the property in
   * @param opt           true to use putOpt, false to use put
   * @throws JSONException
   */
  void put(final String propertyName, final Object propertyValue, final JSONObject object, final boolean opt) throws JSONException {
    try {
      if (isPayloadSizeEstimatedWhenAdding()) {
        final int maximumPayloadSize = getMaximumPayloadSize();
        final int estimatedPayloadSize = estimatePayloadSizeAfterAdding(propertyName, propertyValue);
        final boolean estimatedToExceed = estimatedPayloadSize > maximumPayloadSize;
        if (estimatedToExceed) {
          throw new PayloadMaxSizeProbablyExceededException(maximumPayloadSize, estimatedPayloadSize);
        }
      }
    } catch (final PayloadMaxSizeProbablyExceededException e) {
      throw e;
    } catch (final Exception e) {
      // empty
    }
    if (opt) {
      object.putOpt(propertyName, propertyValue);
    } else {
      object.put(propertyName, propertyValue);
    }
  }

  Object remove(final String propertyName, final JSONObject object) {
    return object.remove(propertyName);
  }

  /**
   * Indicates if payload size is estimated and controlled when adding properties (default is false).
   *
   * @return true to throw an exception if the estimated size is too big when adding a property, false otherwise
   */
  public boolean isPayloadSizeEstimatedWhenAdding() {
    return payloadSizeEstimatedWhenAdding;
  }

  /**
   * Indicate if payload size should be estimated and controlled when adding properties (default is false).
   *
   * @param checked true to throw an exception if the estimated size is too big when adding a property, false otherwise
   */
  public void setPayloadSizeEstimatedWhenAdding(final boolean checked) {
    this.payloadSizeEstimatedWhenAdding = checked;
  }

  /**
   * Return the maximum payload size in bytes.
   * By default, this method returns Integer.MAX_VALUE.
   * Subclasses should override this method to provide their own limit.
   *
   * @return the maximum payload size in bytes
   */
  int getMaximumPayloadSize() {
    return Integer.MAX_VALUE;
  }

  /**
   * Returns the character encoding that will be used by getPayloadAsBytes().
   * Default is UTF-8, as per Apple documentation.
   *
   * @return a character encoding
   */
  public String getCharacterEncoding() {
    return characterEncoding;
  }

  /**
   * Changes the character encoding for streaming the payload.
   * Character encoding is preset to UTF-8, as Apple documentation specifies.
   * Therefore, unless you are working on a special project, you should leave it as is.
   *
   * @param characterEncoding a valid character encoding that String.getBytes(encoding) will accept
   */
  public void setCharacterEncoding(final String characterEncoding) {
    this.characterEncoding = characterEncoding;
  }

  /**
   * Return the number of seconds after which this payload should expire.
   *
   * @return a number of seconds
   */
  public int getExpiry() {
    return expiry;
  }

  /**
   * Set the number of seconds after which this payload should expire.
   * Default is one (1) day.
   *
   * @param seconds
   */
  public void setExpiry(final int seconds) {
    this.expiry = seconds;
  }

  /**
   * Enables a special simulation mode which causes the library to behave
   * as usual *except* that at the precise point where the payload would
   * actually be streamed out to Apple, it is not.
   *
   * @return the same payload
   */
  public Payload asSimulationOnly() {
    setExpiry(919191);
    return this;
  }

  int getPreSendConfiguration() {
    return preSendConfiguration;
  }

  void setPreSendConfiguration(final int preSendConfiguration) {
    this.preSendConfiguration = preSendConfiguration;
  }

}
