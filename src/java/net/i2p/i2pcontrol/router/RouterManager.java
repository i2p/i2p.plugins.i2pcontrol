package net.i2p.i2pcontrol.router;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import net.i2p.I2PAppContext;
import net.i2p.data.Certificate;
import net.i2p.data.DataFormatException;
import net.i2p.data.PrivateKey;
import net.i2p.data.PublicKey;
import net.i2p.data.RouterIdentity;
import net.i2p.data.RouterInfo;
import net.i2p.data.SigningPrivateKey;
import net.i2p.data.SigningPublicKey;
import net.i2p.i2pcontrol.util.IsJar;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;
import net.i2p.util.Log;
import net.i2p.util.SecureFileOutputStream;

/**
 * Handle communications with the router instance.
 * @author mathias
 *
 */
public class RouterManager {
    private final static Log _log = new Log(RouterManager.class);
    private static I2PAppContext context = I2PAppContext.getCurrentContext();
    private static boolean startedTestRouter = false;
    private final static String I2P_DIR = "/home/hottuna/Apps/i2p/";
    private final static String I2P_CONFIG_FILE = "/home/hottuna/.i2p/router.config";


    public static I2PAppContext getAppContext() {
        return context;
    }

    public static RouterContext getRouterContext() throws Exception {
        // If not running as a plugin from within I2P.
        if (!IsJar.isRunningJar() && !startedTestRouter){
            context = buildMinimalRouter();
        }
        if(context.isRouterContext()) {
            return (RouterContext) context;
        }else {
            throw new Exception("No RouterContext available!");
        }
    }

    private static RouterContext buildMinimalRouter(){
        Properties prp = new Properties();
        prp.setProperty("i2p.dir.base", I2P_DIR);
        prp.setProperty("i2p.dir.config", I2P_DIR);
        prp.setProperty("router.pingFile", "testrouter.ping");
        prp.setProperty("router.configLocation", I2P_CONFIG_FILE);
        Router rtr = new  Router(prp);

        // Massive block stolen from CreateRouterInfoJob. 
        RouterInfo info = new RouterInfo();
        OutputStream fos1 = null;
        OutputStream fos2 = null;
        try {
            info.setAddresses(getRouterContext().commSystem().createAddresses());
            Properties stats = getRouterContext().statPublisher().publishStatistics();
            stats.setProperty(RouterInfo.PROP_NETWORK_ID, Router.NETWORK_ID+"");
            getRouterContext().router().addCapabilities(info);
            info.setOptions(stats);
            // not necessary, in constructor
            //info.setPeers(new HashSet());
            info.setPublished(context.clock().now());
            RouterIdentity ident = new RouterIdentity();
            Certificate cert = getRouterContext().router().createCertificate();
            ident.setCertificate(cert);
            PublicKey pubkey = null;
            PrivateKey privkey = null;
            SigningPublicKey signingPubKey = null;
            SigningPrivateKey signingPrivKey = null;
            Object keypair[] = getRouterContext().keyGenerator().generatePKIKeypair();
            pubkey = (PublicKey)keypair[0];
            privkey = (PrivateKey)keypair[1];
            Object signingKeypair[] = getRouterContext().keyGenerator().generateSigningKeypair();
            signingPubKey = (SigningPublicKey)signingKeypair[0];
            signingPrivKey = (SigningPrivateKey)signingKeypair[1];
            ident.setPublicKey(pubkey);
            ident.setSigningPublicKey(signingPubKey);
            info.setIdentity(ident);
            
            info.sign(signingPrivKey);

            if (!info.isValid())
                throw new DataFormatException("RouterInfo we just built is invalid: " + info);
            
            String infoFilename = getRouterContext().getProperty(Router.PROP_INFO_FILENAME, Router.PROP_INFO_FILENAME_DEFAULT);
            File ifile = new File(getRouterContext().getRouterDir(), infoFilename);
            fos1 = new BufferedOutputStream(new SecureFileOutputStream(ifile));
            info.writeBytes(fos1);
            
            String keyFilename = getRouterContext().getProperty(Router.PROP_KEYS_FILENAME, Router.PROP_KEYS_FILENAME_DEFAULT);
            File kfile = new File(getRouterContext().getRouterDir(), keyFilename);
            fos2 = new BufferedOutputStream(new SecureFileOutputStream(kfile));
            privkey.writeBytes(fos2);
            signingPrivKey.writeBytes(fos2);
            pubkey.writeBytes(fos2);
            signingPubKey.writeBytes(fos2);
            
            getRouterContext().keyManager().setKeys(pubkey, privkey, signingPubKey, signingPrivKey);
            
            _log.info("Router info created and stored at " + ifile.getAbsolutePath() + " with private keys stored at " + kfile.getAbsolutePath() + " [" + info + "]");
        } catch (DataFormatException dfe) {
            _log.log(Log.CRIT, "Error building the new router information", dfe);
        } catch (IOException ioe) {
            _log.log(Log.CRIT, "Error writing out the new router information", ioe);
        } catch (Exception e) {
            _log.log(Log.CRIT, "No RouterContext available!", e);
		} finally {
            if (fos1 != null) try { fos1.close(); } catch (IOException ioe) {}
            if (fos2 != null) try { fos2.close(); } catch (IOException ioe) {}
        }

        rtr.setRouterInfo(info);
        rtr.getContext().initAll();
        return rtr.getContext();
    }

    private class SimpleRouterContext extends RouterContext{

        public SimpleRouterContext(Router router) {
            super(router);
        }
        public SimpleRouterContext(Router router, Properties prop) {
            super(router, prop);
        }
    }

    /*
    private static boolean deletePingFile(){
        return (new File("/tmp/router.ping")).delete();
    }
    */

    public static void main(String[] args){
        try {
            getRouterContext();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
