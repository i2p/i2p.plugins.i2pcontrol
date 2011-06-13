package net.i2p.i2pcontrol;
/*
 *  Copyright 2010 zzz (zzz@mail.i2p)
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import net.i2p.I2PAppContext;
import net.i2p.data.Base32;
import net.i2p.data.DataHelper;
import net.i2p.data.Destination;
import net.i2p.data.PrivateKeyFile;
import net.i2p.util.FileUtil;
import net.i2p.util.I2PAppThread;
import net.i2p.util.Log;
import net.i2p.i2ptunnel.TunnelController;
import net.i2p.apps.systray.UrlLauncher;

import org.mortbay.http.HttpContext;
import org.mortbay.jetty.Server;

/**
 * This handles the starting and stopping of an eepsite tunnel and jetty
 * from a single static class so it can be called via clients.config.
 *
 * This makes installation of a new eepsite a turnkey operation -
 * the user is not required to configure a new tunnel in i2ptunnel manually.
 *
 * Usage: ZzzOTController -d $PLUGIN [start|stop]
 *
 * @author zzz
 */
public class I2PControlController {
    private static final Log _log = I2PAppContext.getGlobalContext().logManager().getLog(I2PControlController.class);
    private static Server _server;
    private static TunnelController _tunnel;
    private static ZzzOT _zzzot;
    private static Object _lock = new Object();

    public static void main(String args[]) {
        if (args.length != 3 || (!"-d".equals(args[0])))
            throw new IllegalArgumentException("Usage: PluginController -d $PLUGIN [start|stop]");
        if ("start".equals(args[2]))
            start(args);
        else if ("stop".equals(args[2]))
            stop();
        else
            throw new IllegalArgumentException("Usage: PluginController -d $PLUGIN [start|stop]");
    }

    public static Torrents getTorrents() {
        synchronized(_lock) {
            if (_zzzot == null)
                _zzzot = new ZzzOT();
        }
        return _zzzot.getTorrents();
    }

    private static void start(String args[]) {
        File pluginDir = new File(args[1]);
        if (!pluginDir.exists())
            throw new IllegalArgumentException("Plugin directory " + pluginDir.getAbsolutePath() + " does not exist");

        // We create the private key file in advance, so that we can
        // create the help.html file from the templates
        // without waiting for i2ptunnel to create it AND build the tunnels before returning.
		_log.error("NOTICE: I2PControl started");		
		/*        
		Destination dest = null;
        File key = new File(pluginDir, "eepPriv.dat");
        if (!key.exists()) {
            PrivateKeyFile pkf = new PrivateKeyFile(new File(pluginDir, "eepPriv.dat"));
            try {
                dest = pkf.createIfAbsent();
            } catch (Exception e) {
                _log.error("Unable to create " + key.getAbsolutePath() + ' ' + e);
                throw new IllegalArgumentException("Unable to create " + key.getAbsolutePath() + ' ' + e);
            }
            _log.error("NOTICE: ZzzOT: New eepsite keys created in " + key.getAbsolutePath());
            _log.error("NOTICE: ZzzOT: You should back up this file!");
            String b32 = Base32.encode(dest.calculateHash().getData()) + ".b32.i2p";
            String b64 = dest.toBase64();
            _log.error("NOTICE: ZzzOT: Your base 32 address is " + b32);
            _log.error("NOTICE: ZzzOT: Your base 64 address is " + b64);
        }
		*/
        // Don't startJetty(pluginDir, dest);
        // Don't startI2PTunnel(pluginDir, dest);
    }


    private static void startI2PTunnel(File pluginDir, Destination dest) {
        File i2ptunnelConfig = new File(pluginDir, "i2ptunnel.config");
        Properties i2ptunnelProps = new Properties();
        try {
            DataHelper.loadProps(i2ptunnelProps, i2ptunnelConfig);
        } catch (IOException ioe) {
            _log.error("Cannot open " + i2ptunnelConfig.getAbsolutePath() + ' ' + ioe);
            throw new IllegalArgumentException("Cannot open " + i2ptunnelConfig.getAbsolutePath() + ' ' + ioe);
        }
        TunnelController tun = new TunnelController(i2ptunnelProps, "tunnel.0.");
        // start in foreground so we can get the destination
        //tun.startTunnelBackground();
        tun.startTunnel();
        if (dest != null) {
            List msgs = tun.clearMessages();
            for (Object s : msgs) {
                 _log.error("NOTICE: ZzzOT Tunnel message: " + s);
            }
        }
        _tunnel = tun;
    }

    private static void startJetty(File pluginDir, Destination dest) {
        if (_server != null)
            throw new IllegalArgumentException("Jetty already running!");
        migrateJettyXML(pluginDir);
        I2PAppContext context = I2PAppContext.getGlobalContext();
        File tmpdir = new File(context.getTempDir().getAbsolutePath(), "/zzzot-work");
        tmpdir.mkdir();
        File jettyXml = new File(pluginDir, "jetty.xml");
        try {
            Server serv = new Server(jettyXml.getAbsolutePath());
            HttpContext[] hcs = serv.getContexts();
            for (int i = 0; i < hcs.length; i++)
                 hcs[i].setTempDirectory(tmpdir);
            serv.start();
            _server = serv;
        } catch (Throwable t) {
            _log.error("ZzzOT jetty start failed", t);
            throw new IllegalArgumentException("Jetty start failed " + t);
        }
        if (dest != null)
            launchHelp(pluginDir, dest);
    }

    private static void stop() {
        stopI2PTunnel();
        stopJetty();
        if (_zzzot != null)
            _zzzot.stop();
    }

    private static void stopI2PTunnel() {
        if (_tunnel == null)
            return;
        try {
            _tunnel.stopTunnel();
        } catch (Throwable t) {
            _log.error("ZzzOT tunnel stop failed", t);
            throw new IllegalArgumentException("Tunnel stop failed " + t);
        }
        _tunnel = null;
    }

    private static void stopJetty() {
        if (_server == null)
            return;
        try {
            _server.stop();
        } catch (Throwable t) {
            _log.error("ZzzOT jetty stop failed", t);
            throw new IllegalArgumentException("Jetty stop failed " + t);
        }
        _server = null;
    }

    /** put the directory in the jetty.xml file */
    private static void migrateJettyXML(File pluginDir) {
        File outFile = new File(pluginDir, "jetty.xml");
        if (outFile.exists())
            return;
        File fileTmpl = new File(pluginDir, "templates/jetty.xml");
        try {
            String props = FileUtil.readTextFile(fileTmpl.getAbsolutePath(), 250, true);
            if (props == null)
                throw new IOException(fileTmpl.getAbsolutePath() + " open failed");
            props = props.replace("$PLUGIN", pluginDir.getAbsolutePath());
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(props.getBytes("UTF-8"));
            os.close();
        } catch (IOException ioe) {
            _log.error("jetty.xml migrate failed", ioe);
        }
    }

    /** put the directory, base32, and base64 info in the help.html file and launch a browser window to display it */
    private static void launchHelp(File pluginDir, Destination dest) {
        File fileTmpl = new File(pluginDir, "templates/help.html");
        File outFile = new File(pluginDir, "eepsite/docroot/help.html");
        String b32 = Base32.encode(dest.calculateHash().getData()) + ".b32.i2p";
        String b64 = dest.toBase64();
        try {
            String html = FileUtil.readTextFile(fileTmpl.getAbsolutePath(), 100, true);
            if (html == null)
                throw new IOException(fileTmpl.getAbsolutePath() + " open failed");
            html = html.replace("$PLUGIN", pluginDir.getAbsolutePath());
            html = html.replace("$B32", b32);
            html = html.replace("$B64", b64);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(html.getBytes("UTF-8"));
            os.close();
            Thread t = new I2PAppThread(new Launcher(), "ZzzOTHelp", true);
            t.start();
        } catch (IOException ioe) {
            _log.error("ZzzOT help launch failed", ioe);
        }
    }

    private static class Launcher implements Runnable {
        public void run() {
            UrlLauncher.main(new String[] { "http://127.0.0.1:7662/help.html" } );
        }
    }
}
