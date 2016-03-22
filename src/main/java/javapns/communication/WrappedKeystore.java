package javapns.communication;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Special wrapper for a KeyStore.
 *
 * @author Sylvain Pedneault
 */
class WrappedKeystore extends InputStream {
  private final KeyStore keystore;

  WrappedKeystore(final KeyStore keystore) {
    this.keystore = keystore;
  }

  public KeyStore getKeystore() {
    return keystore;
  }

  @Override
  public int read() throws IOException {
    return 0;
  }
}
