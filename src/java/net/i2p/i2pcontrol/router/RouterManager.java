package net.i2p.i2pcontrol.router;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.i2p.I2PAppContext;
import net.i2p.crypto.KeyGenerator;
import net.i2p.data.Certificate;
import net.i2p.data.DataFormatException;
import net.i2p.data.PrivateKey;
import net.i2p.data.PublicKey;
import net.i2p.data.RouterIdentity;
import net.i2p.data.RouterInfo;
import net.i2p.data.SigningPrivateKey;
import net.i2p.data.SigningPublicKey;
import net.i2p.data.SimpleDataStructure;
import net.i2p.i2pcontrol.util.IsJar;
import net.i2p.router.MultiRouter;
import net.i2p.router.MultiRouterBuilder;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;
import net.i2p.router.startup.CreateRouterInfoJob;
import net.i2p.router.startup.RebuildRouterInfoJob;
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
	
	private static Router getRouter() {
		try {
			return getRouterContext().router();
		} catch (Exception e) {
	        _log.error("Failed to get router. Why did we request it if no RouterContext is available?", e);
            return null;
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
        FileOutputStream fos1 = null;
        FileOutputStream fos2 = null;
        try {
            info.setAddresses(rtr.getContext().commSystem().createAddresses());
            Properties stats = rtr.getContext().statPublisher().publishStatistics();
            stats.setProperty(RouterInfo.PROP_NETWORK_ID, Router.NETWORK_ID+"");
            rtr.getContext().router().addCapabilities(info);
            info.setOptions(stats);
            // not necessary, in constructor
            //info.setPeers(new HashSet());
            //info.setPublished(rtr.getCurrentPublishDate(rtr.getContext()));
            RouterIdentity ident = new RouterIdentity();
            Certificate cert = rtr.getContext().router().createCertificate();
            ident.setCertificate(cert);
            PublicKey pubkey = null;
            PrivateKey privkey = null;
            SigningPublicKey signingPubKey = null;
            SigningPrivateKey signingPrivKey = null;
            Object keypair[] = rtr.getContext().keyGenerator().generatePKIKeypair();
            pubkey = (PublicKey)keypair[0];
            privkey = (PrivateKey)keypair[1];
            Object signingKeypair[] = rtr.getContext().keyGenerator().generateSigningKeypair();
            signingPubKey = (SigningPublicKey)signingKeypair[0];
            signingPrivKey = (SigningPrivateKey)signingKeypair[1];
            ident.setPublicKey(pubkey);
            ident.setSigningPublicKey(signingPubKey);
            info.setIdentity(ident);
            
            info.sign(signingPrivKey);

            if (!info.isValid())
                throw new DataFormatException("RouterInfo we just built is invalid: " + info);
            
            String infoFilename = rtr.getContext().getProperty(Router.PROP_INFO_FILENAME, Router.PROP_INFO_FILENAME_DEFAULT);
            File ifile = new File(rtr.getContext().getRouterDir(), infoFilename);
            fos1 = new SecureFileOutputStream(ifile);
            info.writeBytes(fos1);
            
            String keyFilename = rtr.getContext().getProperty(Router.PROP_KEYS_FILENAME, Router.PROP_KEYS_FILENAME_DEFAULT);
            File kfile = new File(rtr.getContext().getRouterDir(), keyFilename);
            fos2 = new SecureFileOutputStream(kfile);
            privkey.writeBytes(fos2);
            signingPrivKey.writeBytes(fos2);
            pubkey.writeBytes(fos2);
            signingPubKey.writeBytes(fos2);
            
            rtr.getContext().keyManager().setSigningPrivateKey(signingPrivKey);
            rtr.getContext().keyManager().setSigningPublicKey(signingPubKey);
            rtr.getContext().keyManager().setPrivateKey(privkey);
            rtr.getContext().keyManager().setPublicKey(pubkey);
            
            _log.info("Router info created and stored at " + ifile.getAbsolutePath() + " with private keys stored at " + kfile.getAbsolutePath());
        } catch (DataFormatException dfe) {
        	_log.error("Error building the new router information", dfe);
        } catch (IOException ioe) {
            _log.error("Error writing out the new router information", ioe);
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