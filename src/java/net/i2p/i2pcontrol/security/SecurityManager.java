package net.i2p.i2pcontrol.security;
/*
 *  Copyright 2011 hottuna (dev@robertfoss.se)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import sun.misc.BASE64Encoder;


import net.i2p.I2PAppContext;
import net.i2p.crypto.SHA256Generator;
import net.i2p.i2pcontrol.security.jbcrypt.BCrypt;
import net.i2p.i2pcontrol.servlets.configuration.ConfigurationManager;
import net.i2p.util.Log;

/**
 * Manage the password storing for I2PControl.
 */
public class SecurityManager {
	public final static String CERT_ALIAS = "CA";
	private final static String SSL_PROVIDER = "SunJSSE";
	private final static String DEFAULT_BCRYPT_SALT = "$2a$10$DEBg.V4E4RLrQRaY5IvEKe";
	private static String[] SSL_CIPHER_SUITES;
	private static KeyStore _ks;
	private static Log _log;
	
	static {
		_log = I2PAppContext.getGlobalContext().logManager().getLog(SecurityManager.class);
		SocketFactory SSLF = SSLSocketFactory.getDefault();
		try{
		SSL_CIPHER_SUITES =  ((SSLSocket)SSLF.createSocket()).getSupportedCipherSuites();
		} catch (Exception e){
			_log.log(Log.CRIT, "Unable to create SSLSocket used for fetching supported ssl cipher suites.", e);
		}
		_ks = KeyStoreInitializer.getKeyStore();
	}
	
	public static String[] getSupprtedSSLCipherSuites(){
		return SSL_CIPHER_SUITES;
	}
	
	public static String getSecurityProvider(){
		return SSL_PROVIDER;
	}
	
	public static String getKeyStoreLocation(){
		return KeyStoreFactory.DEFAULT_KEYSTORE_LOCATION;
	}
	
	public static String getKeyStorePassword(){
		return KeyStoreFactory.DEFAULT_KEYSTORE_PASSWORD;
	}
	
	public static String getKeyStoreType(){
		return KeyStoreFactory.DEFAULT_KEYSTORE_TYPE;
	}
	
	
	public static String getBase64Cert(){
		X509Certificate caCert = KeyStoreFactory.readCert(_ks,
				CERT_ALIAS, 
				KeyStoreFactory.DEFAULT_KEYSTORE_PASSWORD);
		return getBase64FromCert(caCert);
	}
	
	private static String getBase64FromCert(X509Certificate cert){
		BASE64Encoder encoder = new BASE64Encoder();
		try {
			return encoder.encodeBuffer(cert.getEncoded());
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	/**
	 * Hash input HASH_ITERATIONS times
	 * @return input hashed HASH_ITERATIONS times
	 */
	public static String getPasswdHash(String pwd){
		return BCrypt.hashpw(pwd, ConfigurationManager.getInstance().getConf("hashedPassword", DEFAULT_BCRYPT_SALT));
	}

	/**
	 * Hash input one time with SHA-256.
	 * @param string
	 * @return
	 */
	public static String getHash(String string) {
		SHA256Generator hashGen = new SHA256Generator(I2PAppContext.getGlobalContext());
		byte[] bytes = string.getBytes();
		bytes = hashGen.calculateHash(bytes).toByteArray();
		return new String(bytes);
	}
}
