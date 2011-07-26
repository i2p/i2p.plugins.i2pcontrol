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
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.LogManager;

import net.i2p.I2PAppContext;
import net.i2p.i2pcontrol.router.RouterManager;
import net.i2p.i2pcontrol.security.KeyStoreFactory;
import net.i2p.i2pcontrol.security.KeyStoreInitializer;
import net.i2p.i2pcontrol.security.SecurityManager;
import net.i2p.i2pcontrol.servlets.JSONRPC2Servlet;
import net.i2p.i2pcontrol.servlets.configuration.ConfigurationManager;
import net.i2p.i2pcontrol.util.IsJar;
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
    private static String _pluginDir = "";
    private static ConfigurationManager _conf;
    private static Server _server;
    
    
    public static void main(String args[]) {
        if (args.length != 3 || (!"-d".equals(args[0])))
            throw new IllegalArgumentException("Usage: PluginController -d $PLUGIN [start|stop]");
        
        if ("start".equals(args[2])){
        	File pluginDir = new File(args[1]);
        	if (!pluginDir.exists())
        		throw new IllegalArgumentException("Plugin directory " + pluginDir.getAbsolutePath() + " does not exist");        	
        	_pluginDir = pluginDir.getAbsolutePath();
        	ConfigurationManager.setConfDir(pluginDir.getAbsolutePath());
        	_conf = ConfigurationManager.getInstance();
            start(args);
            
        } else if ("stop".equals(args[2]))
            stop();
        else
            throw new IllegalArgumentException("Usage: PluginController -d $PLUGIN [start|stop]");
    }  


    private static void start(String args[]) {
    	// Enables devtime settings
    	if (!IsJar.isRunningJar()){
    		System.out.println("Running from non-jar");
    		_conf.setConf("i2pcontrol.listen.address", "127.0.0.1");
    		_conf.setConf("i2pcontrol.listen.port", 5555);
    		I2PAppContext.getGlobalContext().logManager().setDefaultLimit(Log.STR_DEBUG);
    	}
    	I2PAppContext.getGlobalContext().logManager().getLog(JSONRPC2Servlet.class).setMinimumPriority(Log.DEBUG); // Delete me
    	
    	try{
    		_server = buildServer();
	    } catch (IOException e) {
			_log.error("Unable to add listener " + _conf.getConf("i2pcontrol.listen.address", "127.0.0.1")+":"+_conf.getConf("i2pcontrol.listen.port", 7560) + " - " + e.getMessage());
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
    
    
    /**
     * Builds a new server. Used for changing ports during operation and such.
     * @return Server - A new server built from current configuration.
     * @throws UnknownHostException
     * @throws Exception
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static Server buildServer() throws UnknownHostException, Exception, InstantiationException, IllegalAccessException{
        Server server = new Server();

    	SslListener ssl = new SslListener();
    	ssl.setProvider(SecurityManager.getSecurityProvider());
    	ssl.setCipherSuites(SecurityManager.getSupprtedSSLCipherSuites());
    	ssl.setInetAddrPort(new InetAddrPort(
    			_conf.getConf("i2pcontrol.listen.address", "127.0.0.1"),
    			_conf.getConf("i2pcontrol.listen.port", 7650)));
    	ssl.setWantClientAuth(false); // Don't care about client authentication.
    	ssl.setPassword(SecurityManager.getKeyStorePassword());
    	ssl.setKeyPassword(SecurityManager.getKeyStorePassword());
    	ssl.setKeystoreType(SecurityManager.getKeyStoreType());
    	ssl.setKeystore((new File(KeyStoreFactory.getKeyStoreLocation())).getAbsolutePath());
    	ssl.setName("SSL Listener");
    	server.addListener(ssl);
    	
        ServletHttpContext context = (ServletHttpContext) server.getContext("/");
        context.addServlet("/jsonrpc", "net.i2p.i2pcontrol.servlets.JSONRPC2Servlet");
		server.start();
		
		return server;
    }
    
    
    /**
     * Replaces the current server with a new one. Shuts down the current server after 60 seconds.
     */
    public static void setServer(final Server server){
    	(new Thread(){
    		@Override
    		public void run(){
    			try {
					Thread.sleep(60*1000);
				} catch (InterruptedException e1) { }
    			if (_server != null){
    				try {
    					_server.stop();
    					_server = server;
    				} catch (InterruptedException e) {}
    			}
    		}
    	}).start();
    }

    
    private static void stop() {
    	ConfigurationManager.writeConfFile();
    	try {
			if (_server != null)
				_server.stop();
			_server = null;
		} catch (InterruptedException e) {
			_log.error("Stopping server" + e);
		}
    }
    
    public static String getPluginDir(){
    	return _pluginDir;
    }

}
