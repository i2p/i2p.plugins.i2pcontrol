package net.i2p.i2pcontrol.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import net.i2p.i2pcontrol.I2PControlController;

import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class KeyStoreFactory {
    private static final float JAVA_VERSION = Float.valueOf(System.getProperty("java.version").charAt(0) + "." + System.getProperty("java.version").charAt(2));
    public static final ObjectIdentifier DEFAULT_CERTIFICATE_ALGORITHM = AlgorithmId.sha512WithRSAEncryption_oid;
    public static final String DEFAULT_CERTIFICATE_ALGORITHM_STRING = "SHA512WithRSA";
    public static final String DEFAULT_KEYSTORE_TYPE = "JKS";
    public static final String DEFAULT_KEYSTORE_PROVIDER = "SUN";
    public static final String DEFAULT_KEYSTORE_NAME = "key.store";
    public static final String DEFAULT_KEYSTORE_PASSWORD = "nut'nfancy";
    public static final String DEFAULT_KEYSTORE_ALGORITHM  = "SunX509";
    private static KeyStore _keystore = null;


    public static KeyPair generateKeyPair() {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);

            KeyPair pair = keyGen.generateKeyPair();
            return pair;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Create a self-signed X.509 Certificate
     * @param dn the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param pair the KeyPair
     * @param days how many days from now the Certificate is valid for
     * @param algorithm the signing algorithm, eg "SHA1withRSA"
     */ 
    static X509Certificate generateCACertificate(String dn, KeyPair pair, int days) {
        try {
            PrivateKey privkey = pair.getPrivate();
            X509CertInfo info = new X509CertInfo();
            Date from = new Date();
            Date to = new Date(from.getTime() + days * 86400000l);
            CertificateValidity interval = new CertificateValidity(from, to);
            BigInteger sn = new BigInteger(64, new SecureRandom());
            X500Name owner = new X500Name("CN=" + dn);

            info.set(X509CertInfo.VALIDITY, interval);
            info.set(X509CertInfo.SERIAL_NUMBER,
                    new CertificateSerialNumber(sn));
            if (JAVA_VERSION <  1.8){
                info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
                info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
            } else {
                info.set(X509CertInfo.SUBJECT, owner);
                info.set(X509CertInfo.ISSUER, owner);
            }
            info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
            info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            AlgorithmId algo = new AlgorithmId(DEFAULT_CERTIFICATE_ALGORITHM);
            info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

            // Sign the cert to identify the algorithm that's used.
            X509CertImpl cert = new X509CertImpl(info);
            cert.sign(privkey, DEFAULT_CERTIFICATE_ALGORITHM_STRING);

            // Update the algorithm, and resign.
            algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
            info.set(CertificateAlgorithmId.NAME + "."
                    + CertificateAlgorithmId.ALGORITHM, algo);
            cert = new X509CertImpl(info);
            cert.sign(privkey, DEFAULT_CERTIFICATE_ALGORITHM_STRING);
            return cert;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static X509Certificate readCert(KeyStore ks, String certAlias, String password){
        try{
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
        } catch (KeyStoreException e){
            e.printStackTrace();
        }
        return null;
    }

    public static X509Certificate readCert(File keyStoreFile, String certAlias, String password){
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
        } catch (IOException e){
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

    public static PrivateKey readPrivateKey(KeyStore ks, String alias, String password){
        try {
            // load the key entry from the keystore
            Key key = ks.getKey(alias, password.toCharArray());

            if (key == null) {
                throw new RuntimeException("Got null key from keystore!");
            }

            PrivateKey privKey = (PrivateKey) key;
            return privKey;
        } catch (UnrecoverableKeyException e){
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch (KeyStoreException e){
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey readPrivateKey(String alias, File keyStoreFile, String keyStorePassword, String keyPassword){
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

    public static KeyStore writeCACertToKeyStore(KeyStore keyStore, String keyPassword, String alias, PrivateKey caPrivKey, X509Certificate caCert){
        try {
            X509Certificate[] chain = new X509Certificate[1];
            chain[0] = caCert;

            keyStore.setKeyEntry(alias, caPrivKey, keyPassword.toCharArray(), chain);
            File keyStoreFile = new File(I2PControlController.getPluginDir()+File.separator+DEFAULT_KEYSTORE_NAME);
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

    public static synchronized KeyStore getDefaultKeyStore(){
        if (_keystore == null){
            File keyStoreFile = new File(I2PControlController.getPluginDir()+File.separator+DEFAULT_KEYSTORE_NAME);

            try {
                _keystore = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);
                if (keyStoreFile.exists()){
                    InputStream is = new FileInputStream(keyStoreFile);
                    _keystore.load(is, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
                    return _keystore;
                } else {
                    throw new IOException("KeyStore file " + keyStoreFile.getAbsolutePath() + " wasn't readable");
                }
            } catch (Exception e) {
                // Ignore. Not an issue. Let's just create a new keystore instead.
            }
            try {
                _keystore = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);
                _keystore.load(null, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
                _keystore.store(new FileOutputStream(keyStoreFile), DEFAULT_KEYSTORE_PASSWORD.toCharArray());
                return _keystore;
            } catch (Exception e){
                // Log perhaps?
            }
            return null;
        } else {
            return _keystore;
        }
    }

    public static String getKeyStoreLocation(){
        File keyStoreFile = new File(I2PControlController.getPluginDir()+File.separator+DEFAULT_KEYSTORE_NAME);
        return keyStoreFile.getAbsolutePath();
    }
}
