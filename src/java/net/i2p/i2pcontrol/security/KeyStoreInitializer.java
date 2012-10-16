package net.i2p.i2pcontrol.security;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

import net.i2p.I2PAppContext;
import net.i2p.i2pcontrol.I2PControlController;
import net.i2p.util.Log;


public class KeyStoreInitializer {
    private static KeyStore _ks;
    private static final Log _log = I2PAppContext.getGlobalContext().logManager().getLog(I2PControlController.class);

    public synchronized static KeyStore getKeyStore(){
        try {
            if (_ks == null){
                _log.info("KeyStore wasn't loaded. Loading..");
                _ks = KeyStoreFactory.getDefaultKeyStore();
            }
            if (!_ks.containsAlias(SecurityManager.CERT_ALIAS)){
                _log.info("Keystore is missing a selfsigned cert, rebuilding now..");
                KeyPair cakp = KeyStoreFactory.generateKeyPair();
                X509Certificate caCert = KeyStoreFactory.generateCACertificate(SecurityManager.CERT_ALIAS,
                        cakp,
                        3650);
                KeyStoreFactory.writeCACertToKeyStore(_ks, 
                        KeyStoreFactory.DEFAULT_KEYSTORE_PASSWORD, 
                        SecurityManager.CERT_ALIAS, 
                        cakp.getPrivate(),
                        caCert);
                _log.info("Keystores loaded with new selfsigned cert.");
                return _ks;
            } else {
                return _ks;
            }
        } catch (KeyStoreException e){
            _log.error("Error reading keystore", e);
            e.printStackTrace();
        }
        return null;
    }
}
