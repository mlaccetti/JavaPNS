package javapns.communication;

import javapns.communication.exceptions.InvalidKeystoreFormatException;
import javapns.communication.exceptions.InvalidKeystorePasswordException;
import javapns.communication.exceptions.InvalidKeystoreReferenceException;
import javapns.communication.exceptions.KeystoreException;

import java.io.*;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * Class responsible for dealing with keystores.
 *
 * @author Sylvain Pedneault
 */
public class KeystoreManager {
  private static final String REVIEW_MESSAGE = " Please review the procedure for generating a keystore for JavaPNS.";

  private KeystoreManager() {}

  /**
   * Loads a keystore.
   *
   * @param server The server the keystore is intended for
   * @return A loaded keystore
   * @throws KeystoreException
   */
  static KeyStore loadKeystore(final AppleServer server) throws KeystoreException {
    return loadKeystore(server, server.getKeystoreStream());
  }

  /**
   * Loads a keystore.
   *
   * @param server   the server the keystore is intended for
   * @param keystore a keystore containing your private key and the certificate signed by Apple (File, InputStream, byte[], KeyStore or String for a file path)
   * @return a loaded keystore
   * @throws KeystoreException
   */
  private static KeyStore loadKeystore(final AppleServer server, final Object keystore) throws KeystoreException {
    return loadKeystore(server, keystore, false);
  }

  /**
   * Loads a keystore.
   *
   * @param server         the server the keystore is intended for
   * @param keystore       a keystore containing your private key and the certificate signed by Apple (File, InputStream, byte[], KeyStore or String for a file path)
   * @param verifyKeystore whether or not to perform basic verifications on the keystore to detect common mistakes.
   * @return a loaded keystore
   * @throws KeystoreException
   */
  private static synchronized KeyStore loadKeystore(final AppleServer server, final Object keystore, final boolean verifyKeystore) throws KeystoreException {
    if (keystore instanceof KeyStore) {
      return (KeyStore) keystore;
    }

    try (final InputStream keystoreStream = streamKeystore(keystore)) {
      if (keystoreStream instanceof WrappedKeystore) {
        return ((WrappedKeystore) keystoreStream).getKeystore();
      }

      final KeyStore keyStore = KeyStore.getInstance(server.getKeystoreType());
      final char[] password = KeystoreManager.getKeystorePasswordForSSL(server);
      keyStore.load(keystoreStream, password);
      return keyStore;
    } catch (final Exception e) {
      throw wrapKeystoreException(e);
    }
  }

  /**
   * Make sure that the provided keystore will be reusable.
   *
   * @param server   the server the keystore is intended for
   * @param keystore a keystore containing your private key and the certificate signed by Apple (File, InputStream, byte[], KeyStore or String for a file path)
   * @return a reusable keystore
   * @throws KeystoreException
   */
  static Object ensureReusableKeystore(final AppleServer server, Object keystore) throws KeystoreException {
    if (keystore instanceof InputStream) {
      keystore = loadKeystore(server, keystore, false);
    }
    return keystore;
  }

  /**
   * Perform basic tests on a keystore to detect common user mistakes.
   * If a problem is found, a KeystoreException is thrown.
   * If no problem is found, this method simply returns without exceptions.
   *
   * @param server   the server the keystore is intended for
   * @param keystore a keystore containing your private key and the certificate signed by Apple (File, InputStream, byte[], KeyStore or String for a file path)
   * @throws KeystoreException
   */
  public static void verifyKeystoreContent(final AppleServer server, final Object keystore) throws KeystoreException {
    final KeyStore keystoreToValidate;
    if (keystore instanceof KeyStore) {
      keystoreToValidate = (KeyStore) keystore;
    } else {
      keystoreToValidate = loadKeystore(server, keystore);
    }
    verifyKeystoreContent(keystoreToValidate);
  }

  /**
   * Perform basic tests on a keystore to detect common user mistakes (experimental).
   * If a problem is found, a KeystoreException is thrown.
   * If no problem is found, this method simply returns without exceptions.
   *
   * @param keystore a keystore to verify
   * @throws KeystoreException thrown if a problem was detected
   */
  private static void verifyKeystoreContent(final KeyStore keystore) throws KeystoreException {
    try {
      int numberOfCertificates = 0;
      final Enumeration<String> aliases = keystore.aliases();
      while (aliases.hasMoreElements()) {
        final String alias = aliases.nextElement();
        final Certificate certificate = keystore.getCertificate(alias);
        if (certificate instanceof X509Certificate) {
          final X509Certificate xcert = (X509Certificate) certificate;
          numberOfCertificates++;

          /* Check validity dates */
          xcert.checkValidity();

          /* Check issuer */
          final boolean issuerIsApple = xcert.getIssuerDN().toString().contains("Apple");
          if (!issuerIsApple) {
            throw new KeystoreException("Certificate was not issued by Apple." + REVIEW_MESSAGE);
          }

          /* Check certificate key usage */
          final boolean[] keyUsage = xcert.getKeyUsage();
          if (!keyUsage[0]) {
            throw new KeystoreException("Certificate usage is incorrect." + REVIEW_MESSAGE);
          }

        }
      }
      if (numberOfCertificates == 0) {
        throw new KeystoreException("Keystore does not contain any valid certificate." + REVIEW_MESSAGE);
      }
      if (numberOfCertificates > 1) {
        throw new KeystoreException("Keystore contains too many certificates." + REVIEW_MESSAGE);
      }

    } catch (final KeystoreException e) {
      throw e;
    } catch (final CertificateExpiredException e) {
      throw new KeystoreException("Certificate is expired. A new one must be issued.", e);
    } catch (final CertificateNotYetValidException e) {
      throw new KeystoreException("Certificate is not yet valid. Wait until the validity period is reached or issue a new certificate.", e);
    } catch (final Exception e) {
      /* We ignore any other exception, as we do not want to interrupt the process because of an error we did not expect. */
    }
  }

  static char[] getKeystorePasswordForSSL(final AppleServer server) {
    String password = server.getKeystorePassword();
    if (password == null) {
      password = "";
    }

    return password.toCharArray();
  }

  static KeystoreException wrapKeystoreException(final Exception e) {
    if (e != null) {
      final String msg = e.toString();
      if (msg.contains("javax.crypto.BadPaddingException")) {
        return new InvalidKeystorePasswordException();
      }
      if (msg.contains("DerInputStream.getLength(): lengthTag=127, too big")) {
        return new InvalidKeystoreFormatException();
      }
      if (msg.contains("java.lang.ArithmeticException: / by zero") || msg.contains("java.security.UnrecoverableKeyException: Get Key failed: / by zero")) {
        return new InvalidKeystorePasswordException("Blank passwords not supported (#38).  You must create your keystore with a non-empty password.");
      }
    }

    return new KeystoreException("Keystore exception: " + (e != null ? e.getMessage() : null), e);
  }

  /**
   * Given an object representing a keystore, returns an actual stream for that keystore.
   * Allows you to provide an actual keystore as an InputStream or a byte[] array,
   * or a reference to a keystore file as a File object or a String path.
   *
   * @param keystore a keystore containing your private key and the certificate signed by Apple (File, InputStream, byte[], KeyStore or String for a file path)
   * @return A stream to the keystore.
   * @throws InvalidKeystoreReferenceException
   */
  static InputStream streamKeystore(final Object keystore) throws InvalidKeystoreReferenceException {
    validateKeystoreParameter(keystore);
    try {
      if (keystore instanceof InputStream) {
        return (InputStream) keystore;
      } else if (keystore instanceof KeyStore) {
        return new WrappedKeystore((KeyStore) keystore);
      } else if (keystore instanceof File) {
        return new BufferedInputStream(new FileInputStream((File) keystore));
      } else if (keystore instanceof String) {
        return new BufferedInputStream(new FileInputStream((String) keystore));
      } else if (keystore instanceof byte[]) {
        return new ByteArrayInputStream((byte[]) keystore);
      } else {
        return null; // we should not get here since validateKeystore ensures that the reference is valid
      }
    } catch (final Exception e) {
      throw new InvalidKeystoreReferenceException("Invalid keystore reference: " + e.getMessage());
    }
  }

  /**
   * Ensures that a keystore parameter is actually supported by the KeystoreManager.
   *
   * @param keystore a keystore containing your private key and the certificate signed by Apple (File, InputStream, byte[], KeyStore or String for a file path)
   * @throws InvalidKeystoreReferenceException thrown if the provided keystore parameter is not supported
   */
  public static void validateKeystoreParameter(Object keystore) throws InvalidKeystoreReferenceException {
    if (keystore == null) {
      throw new InvalidKeystoreReferenceException((Object) null);
    }
    if (keystore instanceof KeyStore) {
      return;
    }
    if (keystore instanceof InputStream) {
      return;
    }
    if (keystore instanceof String) {
      keystore = new File((String) keystore);
    }
    if (keystore instanceof File) {
      final File file = (File) keystore;
      if (!file.exists()) {
        throw new InvalidKeystoreReferenceException("Invalid keystore reference.  File does not exist: " + file.getAbsolutePath());
      }
      if (!file.isFile()) {
        throw new InvalidKeystoreReferenceException("Invalid keystore reference.  Path does not refer to a valid file: " + file.getAbsolutePath());
      }
      if (file.length() <= 0) {
        throw new InvalidKeystoreReferenceException("Invalid keystore reference.  File is empty: " + file.getAbsolutePath());
      }
      return;
    }
    if (keystore instanceof byte[]) {
      final byte[] bytes = (byte[]) keystore;
      if (bytes.length == 0) {
        throw new InvalidKeystoreReferenceException("Invalid keystore reference. Byte array is empty");
      }
      return;
    }
    throw new InvalidKeystoreReferenceException(keystore);
  }

}
