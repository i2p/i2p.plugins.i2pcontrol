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
import net.i2p.data.router.RouterIdentity;
import net.i2p.data.router.RouterInfo;
import net.i2p.data.SigningPrivateKey;
import net.i2p.data.SigningPublicKey;
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
        if(context.isRouterContext()) {
            return (RouterContext) context;
        }else {
            throw new Exception("No RouterContext available!");
        }
    }
}
