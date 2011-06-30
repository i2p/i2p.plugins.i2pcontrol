package net.i2p.i2pcontrol.security;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.Builder;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.x509.X509Util;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

public class KeyStoreFactory {
	public static final String DEFAULT_SIGNATURE_ALGORITHM = "SHA1withRSA";
	public static final String DEFAULT_KEYSTORE_TYPE = "JKS";
	public static final String DEFAULT_KEYSTORE_PROVIDER = "SUN";
	public static final String DEFAULT_KEYSTORE_LOCATION = "key.store";
	public static final String DEFAULT_KEYSTORE_PASSWORD = "nut'nfancy";
	public static final String DEFAULT_KEYSTORE_ALGORITHM  = "SunX509";
	private static KeyStore _keystore = null;
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public static KeyPair generateKeyPair() {
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			keyGen.initialize(1024, random);

			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey privKey = pair.getPrivate();
			PublicKey pubKey = pair.getPublic();

			return pair;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static X509Certificate generateCACertificate(String caName, KeyPair keyPair, int validNbrDays){
		Date startDate = new Date(System.currentTimeMillis()); // time from which certificate is valid
		
		Calendar expiry = Calendar.getInstance();
		expiry.add(Calendar.DAY_OF_YEAR, validNbrDays);
		Date expiryDate = expiry.getTime();	// time after which certificate is not valid
		
		byte[] bigNum = new byte[1024];
		(new SecureRandom()).nextBytes(bigNum);
		BigInteger serialNumber = new BigInteger(bigNum);
		serialNumber = serialNumber.abs();	// serial number for certificate
		
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		X500Principal dname = new X500Principal("CN=" + caName);

		certGen.setSerialNumber(serialNumber);
		certGen.setIssuerDN(dname);
		certGen.setNotBefore(startDate);
		certGen.setNotAfter(expiryDate);
		certGen.setSubjectDN(dname);
		certGen.setPublicKey(keyPair.getPublic());
		certGen.setSignatureAlgorithm(DEFAULT_SIGNATURE_ALGORITHM);


		try {
			X509Certificate cert = certGen.generate(keyPair.getPrivate(), "BC"); // BC == BouncyCastle
			return cert;
		} catch (CertificateEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   // note: private key of CA
		return null;
	}

	public static X509Certificate generateCertificate(X509Certificate caCert, PrivateKey caPrivKey, String certName, KeyPair certKeyPair, int validNbrDays) {

			Date startDate = new Date(System.currentTimeMillis()); // time from which certificate is valid
			
			Calendar expiry = Calendar.getInstance();
			expiry.add(Calendar.DAY_OF_YEAR, validNbrDays);
			Date expiryDate = expiry.getTime();               // time after which certificate is not valid
			
			byte[] bigNum = new byte[1024];
			(new SecureRandom()).nextBytes(bigNum);
			BigInteger serialNumber = new BigInteger(bigNum);
			serialNumber = serialNumber.abs();	// serial number for certificate
			
			X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
			X500Principal subjectName = new X500Principal("CN=" + certName);

			certGen.setSerialNumber(serialNumber);
			certGen.setIssuerDN(caCert.getSubjectX500Principal());
			certGen.setNotBefore(startDate);
			certGen.setNotAfter(expiryDate);
			certGen.setSubjectDN(subjectName);
			certGen.setPublicKey(certKeyPair.getPublic());
			certGen.setSignatureAlgorithm(DEFAULT_SIGNATURE_ALGORITHM);

			try {
				certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
				certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(certKeyPair.getPublic()));
				X509Certificate cert = certGen.generate(caPrivKey, "BC");   // note: private key of CA
				return cert;
			} catch (CertificateParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SignatureException e) {
				// TODO Auto-generated catch block
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
	
	
	public static X509CertificateObject signWithCa(X509Certificate x509Issuer, RSAPrivateCrtKeyParameters caKeyParameters, TBSCertificateStructure tbsCert) {
		
		try{
	        SHA1Digest digester = new SHA1Digest();
	
	        AsymmetricBlockCipher rsa = new PKCS1Encoding(new RSAEngine());
	        ByteArrayOutputStream  bOut = new ByteArrayOutputStream();
	        DEROutputStream dOut = new DEROutputStream(bOut);
	        
			DERObjectIdentifier sigOID = X509Util.getAlgorithmOID("SHA1WithRSAEncryption");
			AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(sigOID, new DERNull());
	
	        dOut.writeObject(tbsCert);
	
	        byte[] signature;
	
	        byte[] certBlock = bOut.toByteArray();
	
	        // first create digest
	        digester.update(certBlock, 0, certBlock.length);
	        byte[] hash = new byte[digester.getDigestSize()];
	        digester.doFinal(hash, 0);
	
	        // and sign that
	        rsa.init(true, caKeyParameters);
	        DigestInfo dInfo = new DigestInfo( new AlgorithmIdentifier(X509ObjectIdentifiers.id_SHA1, null), hash);
	        byte[] digest = dInfo.getEncoded(ASN1Encodable.DER);
	        signature = rsa.processBlock(digest, 0, digest.length);
	
	        ASN1EncodableVector v = new ASN1EncodableVector();
	        v.add(tbsCert);
	        v.add(sigAlgId);
	        v.add(new DERBitString(signature));
	        X509CertificateObject clientCert = new X509CertificateObject(new X509CertificateStructure(new DERSequence(v))); 
	        
	        
			try {
				clientCert.verify(x509Issuer.getPublicKey());
			} catch (Exception e) {
				System.out.println("Failed to verify client certificate against caCert");
				e.printStackTrace();
			}
	        return clientCert;
        
		} catch(IOException e){
			e.printStackTrace();
		} catch (InvalidCipherTextException e) {
			e.printStackTrace();
		} catch (CertificateParsingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PKCS10CertificationRequest createCSR(String name, KeyPair csrKeyPair){
		X500Principal subjectName = new X500Principal("CN=" + name);

		try {
			PKCS10CertificationRequest kpGen = new PKCS10CertificationRequest(
				DEFAULT_SIGNATURE_ALGORITHM,
				subjectName,
				csrKeyPair.getPublic(),
				null,
				csrKeyPair.getPrivate());
			return kpGen;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static KeyStore writeCACertToKeyStore(KeyStore keyStore, String keyPassword, String alias, PrivateKey caPrivKey, X509Certificate caCert){
        try {
        	
        PKCS12BagAttributeCarrier bagCert = (X509CertificateObject) caCert;
        bagCert.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
                new DERBMPString(alias));
        
        bagCert.setBagAttribute(
                PKCSObjectIdentifiers.pkcs_9_at_localKeyId,
                new SubjectKeyIdentifierStructure(caCert.getPublicKey()));

        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = caCert;

        keyStore.setKeyEntry(alias, caPrivKey, keyPassword.toCharArray(), chain);
        keyStore.store(new FileOutputStream(DEFAULT_KEYSTORE_LOCATION), DEFAULT_KEYSTORE_PASSWORD.toCharArray());
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
	
	public static KeyStore writeCertToStore(KeyStore keyStore, String keyPassword, String alias, PrivateKey userPrivKey, X509Certificate clientCert, X509Certificate caCert){
        
		try {
			clientCert.verify(caCert.getPublicKey());
		} catch (Exception e) {
			System.out.println("Failed to verify client certificate against caCert");
			e.printStackTrace();
		}
        
        try {
        	
        PKCS12BagAttributeCarrier bagCert = (X509CertificateObject) clientCert;
        bagCert.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
                new DERBMPString(alias));
        
        bagCert.setBagAttribute(
                PKCSObjectIdentifiers.pkcs_9_at_localKeyId,
                new SubjectKeyIdentifierStructure(clientCert.getPublicKey()));


        X509Certificate[] chain = new X509Certificate[2];

        // first the client, then the CA certificate
        chain[0] = clientCert;
        chain[1] = caCert;

       

        keyStore.setKeyEntry(alias, userPrivKey, keyPassword.toCharArray(), chain);
        keyStore.store(new FileOutputStream(DEFAULT_KEYSTORE_LOCATION), DEFAULT_KEYSTORE_PASSWORD.toCharArray());
        try {
        	clientCert.verify(chain[1].getPublicKey());
		} catch (InvalidKeyException e) {
			System.out.println("Unable to verify clientCert against caCert");
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			System.out.println("Unable to verify clientCert against caCert");
			e.printStackTrace();
		} catch (SignatureException e) {
			System.out.println("Unable to verify clientCert against caCert");
			e.printStackTrace();
		}
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
			KeyStore ks = null;
			try {
				ks = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);
				if ((new File(DEFAULT_KEYSTORE_LOCATION)).exists()){
					InputStream is = new FileInputStream(DEFAULT_KEYSTORE_LOCATION);
					ks.load(is, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
					return ks;
				} else {
					throw new IOException("KeyStore file " + DEFAULT_KEYSTORE_LOCATION + "wasn't readable");
				}
			} catch (Exception e) {
				// Ignore. Not an issue. Let's just create a new keystore instead.
			}
			try {
				ks = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);
				ks.load(null, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
				ks.store(new FileOutputStream(DEFAULT_KEYSTORE_LOCATION), DEFAULT_KEYSTORE_PASSWORD.toCharArray());
				return ks;
			} catch (Exception e){
				// Log perhaps?
			}
			return null;
		} else {
			return _keystore;
		}
	}
}