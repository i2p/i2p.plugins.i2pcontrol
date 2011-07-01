package net.i2p.i2pcontrol;
/*
 *  Copyright 2010 hottuna (dev@robertfoss.se)
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
import java.io.IOException;
import java.net.InetAddress;
import java.util.Calendar;

import net.i2p.I2PAppContext;
import net.i2p.i2pcontrol.security.KeyStoreInitializer;
import net.i2p.i2pcontrol.security.SecurityManager;
import net.i2p.util.Log;

import org.mortbay.http.SslListener;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;
import org.mortbay.util.InetAddrPort;

/**
 * This handles the starting and stopping of an eepsite tunnel and jetty
 * from a single static class so it can be called via clients.config.
 *
 * This makes installation of a new eepsite a turnkey operation -
 * the user is not required to configure a new tunnel in i2ptunnel manually.
 *
 * Usage: I2PControlController -d $PLUGIN [start|stop]
 *
 * @author hottuna
 */
public class I2PControlController{
    private static final Log _log = I2PAppContext.getGlobalContext().logManager().getLog(I2PControlController.class);
    private static Object _lock = new Object();
    private static Server _server;
    private static Settings _settings;
    
    
    public static void main(String args[]) {
        if (args.length != 3 || (!"-d".equals(args[0])))
            throw new IllegalArgumentException("Usage: PluginController -d $PLUGIN [start|stop]");
        if ("start".equals(args[2])){
            start(args);
        } else if ("stop".equals(args[2]))
            stop();
        else
            throw new IllegalArgumentException("Usage: PluginController -d $PLUGIN [start|stop]");
    }
    
    public static String getTestString(){
    	Calendar cal = Calendar.getInstance();
    	int hour = cal.get(Calendar.HOUR_OF_DAY);
    	int minute = cal.get(Calendar.MINUTE);
    	int second = cal.get(Calendar.SECOND);
    	int ms = cal.get(Calendar.MILLISECOND);
    	return hour+":"+minute+":"+second+":"+ms;
    }
    


    private static void start(String args[]) {
        //File pluginDir = new File(args[1]);
        //if (!pluginDir.exists())
        //    throw new IllegalArgumentException("Plugin directory " + pluginDir.getAbsolutePath() + " does not exist");
        
    	
    	System.out.println(net.i2p.i2pcontrol.security.jbcrypt.BCrypt.hashpw("itoopie","$2a$11$5aOLx2x/8i4fNaitoCSSWu"));
        _server = new Server();
        try {
        	SslListener ssl = new SslListener();
        	ssl.setProvider(SecurityManager.getSecurityProvider());
        	ssl.setCipherSuites(SecurityManager.getSupprtedSSLCipherSuites());
        	// FIXME: Change to use new ConrigurationManager.
        	ssl.setInetAddrPort(new InetAddrPort(Settings.getListenIP(), Settings.getListenPort()));
        	ssl.setWantClientAuth(false); // Don't care about client authetication.
        	ssl.setPassword(SecurityManager.getKeyStorePassword());
        	ssl.setKeyPassword(SecurityManager.getKeyStorePassword());
        	ssl.setKeystoreType(SecurityManager.getKeyStoreType());
        	ssl.setKeystore((new File(SecurityManager.getKeyStoreLocation())).getAbsolutePath());
        	ssl.setName("SSL Listener");
        	_server.addListener(ssl);
        	
	        ServletHttpContext context = (ServletHttpContext) _server.getContext("/");
	        context.addServlet("/", "net.i2p.i2pcontrol.servlets.SettingsServlet");
	        context.addServlet("/jsonrpc", "net.i2p.i2pcontrol.servlets.JSONRPC2Servlet");
	        context.addServlet("/history", "net.i2p.i2pcontrol.servlets.HistoryServlet");
			_server.start();
        } catch (IOException e) {
			_log.error("Unable to add listener " + Settings.getListenIP()+":"+Settings.getListenPort() + " - " + e.getMessage());
		} catch (ClassNotFoundException e) {
			_log.error("Unable to find class net.i2p.i2pcontrol.JSONRPCServlet: " + e.getMessage());
		} catch (InstantiationException e) {
			_log.error("Unable to instantiate class net.i2p.i2pcontrol.JSONRPCServlet: " + e.getMessage());
		} catch (IllegalAccessException e) {
			_log.error("Illegal access: " + e.getMessage());
		} catch (Exception e) {
			_log.error("Unable to start jetty server: " + e.getMessage());
		}
		

    }

    
    private static void stop() {
    	try {
			if (_server != null)
				_server.stop();
			_server = null;
		} catch (InterruptedException e) {
			_log.error("Stopping server" + e);
		}
    }
}
