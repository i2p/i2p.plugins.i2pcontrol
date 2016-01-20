package net.i2p.i2pcontrol.security;

import net.i2p.crypto.KeyStoreUtil;
import net.i2p.i2pcontrol.I2PControlController;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class KeyStoreProvider {
    public static final String DEFAULT_CERTIFICATE_ALGORITHM_STRING = "RSA";
    public static final int DEFAULT_CERTIFICATE_KEY_LENGTH = 4096;
    public static final int DEFAULT_CERTIFICATE_VALIDITY = 365 * 10;
    public final static String DEFAULT_CERTIFICATE_DOMAIN = "net.i2p.i2pcontrol";
    public final static String DEFAULT_CERTIFICATE_ALIAS = "I2PControl CA";
    public static final String DEFAULT_KEYSTORE_NAME = "key.store";
    public static final String DEFAULT_KEYSTORE_PASSWORD = "nut'nfancy";
    private static KeyStore _keystore = null;


    public static void initialize() {
        KeyStoreUtil.createKeys(new File(getKeyStoreLocation()),
                                DEFAULT_KEYSTORE_PASSWORD,
                                DEFAULT_CERTIFICATE_ALIAS,
                                DEFAULT_CERTIFICATE_DOMAIN,
                                "i2pcontrol",
                                DEFAULT_CERTIFICATE_VALIDITY,
                                DEFAULT_CERTIFICATE_ALGORITHM_STRING,
                                DEFAULT_CERTIFICATE_KEY_LENGTH,
                                DEFAULT_KEYSTORE_PASSWORD);
    }

    public static X509Certificate readCert(KeyStore ks, String certAlias, String password) {
        try {
            X509Certificate cert = (X509Certificate) ks.getCertificate(certAlias);

            if (cert == null) {
                throw new RuntimeException("Got null cert from keystore!");
            }

            try {
                cert.verify(cert.getPublicKey());
                return cert;
            } catch (Exception e) {
                System.err.println("Failed to verify caCert certificate against caCert");
                e.printStackTrace();
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static X509Certificate readCert(File keyStoreFile, String certAlias, String password) {
        try {
            KeyStore ks = getDefaultKeyStore();
            ks.load(new FileInputStream(keyStoreFile), password.toCharArray());
            X509Certificate cert = (X509Certificate) ks.getCertificate(certAlias);

            if (cert == null) {
                throw new RuntimeException("Got null cert from keystore!");
            }

            try {
                cert.verify(cert.getPublicKey());
                return cert;
            } catch (Exception e) {
                System.err.println("Failed to verify caCert certificate against caCert");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("Couldn't read keystore from: " + keyStoreFile.toString());
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            System.err.println("No certificate with alias: " + certAlias + "  found.");
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey readPrivateKey(KeyStore ks, String alias, String password) {
        try {
            // load the key entry from the keystore
            Key key = ks.getKey(alias, password.toCharArray());

            if (key == null) {
                throw new RuntimeException("Got null key from keystore!");
            }

            PrivateKey privKey = (PrivateKey) key;
            return privKey;
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey readPrivateKey(String alias, File keyStoreFile, String keyStorePassword, String keyPassword) {
        try {
            KeyStore ks = getDefaultKeyStore();
            ks.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());
            return readPrivateKey(ks, alias, keyStorePassword);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Couldn't read keystore from: " + keyStoreFile.toString());
            e.printStackTrace();
        }
        return null;
    }

    public static KeyStore writeCACertToKeyStore(KeyStore keyStore, String keyPassword, String alias, PrivateKey caPrivKey, X509Certificate caCert) {
        try {
            X509Certificate[] chain = new X509Certificate[1];
            chain[0] = caCert;

            keyStore.setKeyEntry(alias, caPrivKey, keyPassword.toCharArray(), chain);
            File keyStoreFile = new File(I2PControlController.getPluginDir() + File.separator + DEFAULT_KEYSTORE_NAME);
            keyStore.store(new FileOutputStream(keyStoreFile), DEFAULT_KEYSTORE_PASSWORD.toCharArray());
            return keyStore;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static synchronized KeyStore getDefaultKeyStore() {
        if (_keystore == null) {
            File keyStoreFile = new File(getKeyStoreLocation());

            try {
                _keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                if (keyStoreFile.exists()) {
                    InputStream is = new FileInputStream(keyStoreFile);
                    _keystore.load(is, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
                    return _keystore;
                }

                initialize();
                _keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                if (keyStoreFile.exists()) {
                    InputStream is = new FileInputStream(keyStoreFile);
                    _keystore.load(is, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
                    return _keystore;
                } else {
                    throw new IOException("KeyStore file " + keyStoreFile.getAbsolutePath() + " wasn't readable");
                }
            } catch (Exception e) {
                // Ignore. Not an issue. Let's just create a new keystore instead.
            }
            return null;
        } else {
            return _keystore;
        }
    }

    public static String getKeyStoreLocation() {
        File keyStoreFile = new File(I2PControlController.getPluginDir() + File.separator + DEFAULT_KEYSTORE_NAME);
        return keyStoreFile.getAbsolutePath();
    }
}
