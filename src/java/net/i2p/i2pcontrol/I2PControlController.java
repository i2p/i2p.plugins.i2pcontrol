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
import java.net.UnknownHostException;

import net.i2p.I2PAppContext;
import net.i2p.i2pcontrol.security.KeyStoreFactory;
import net.i2p.i2pcontrol.security.SecurityManager;
import net.i2p.i2pcontrol.servlets.JSONRPC2Servlet;
import net.i2p.i2pcontrol.servlets.configuration.ConfigurationManager;
import net.i2p.i2pcontrol.util.IsJar;
import net.i2p.util.Log;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletHandler;


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
    private static SecurityManager _secMan;
    private static Server _server;


    public static void main(String args[]) {
        if (args.length != 3 || (!"-d".equals(args[0])))
            throw new IllegalArgumentException("Usage: PluginController -d $PLUGINDIR [start|stop]");

        if ("start".equals(args[2])){
            File pluginDir = new File(args[1]);
            if (!pluginDir.exists())
                throw new IllegalArgumentException("Plugin directory " + pluginDir.getAbsolutePath() + " does not exist");
            _pluginDir = pluginDir.getAbsolutePath();
            ConfigurationManager.setConfDir(pluginDir.getAbsolutePath());
            _conf = ConfigurationManager.getInstance();
            _secMan = SecurityManager.getInstance();
            start(args);
            //stop(); // Delete Me

        } else if ("stop".equals(args[2]))
            stop();
        else
            throw new IllegalArgumentException("Usage: PluginController -d $PLUGINDIR [start|stop]");
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

        SslSocketConnector ssl = buildSslListener(_conf.getConf("i2pcontrol.listen.address", "127.0.0.1"), 
                                                  _conf.getConf("i2pcontrol.listen.port", 7650));
        server.addConnector(ssl);

        ServletHandler sh = new ServletHandler();
        sh.addServletWithMapping(net.i2p.i2pcontrol.servlets.JSONRPC2Servlet.class, "/");
        server.getServer().setHandler(sh);
        server.start();

        return server;
    }


    /**
     * Creates a SSLListener with all the default options. The listener will use all the default options.
     * @param address - The address the listener will listen to.
     * @param port - The port the listener will listen to.
     * @return - Newly created listener
     * @throws UnknownHostException
     */
    public static SslSocketConnector buildSslListener(String address, int port) throws UnknownHostException{
        int listeners = 0;
        if (_server != null){
            listeners = _server.getConnectors().length;
        }

        SslSocketConnector ssl = new SslSocketConnector();
        ssl.setProvider(_secMan.getSecurityProvider());
        //ssl.setCipherSuites(_secMan.getSupprtedSSLCipherSuites()); Removed in Jetty 5->6 port.
        ssl.setHost(address);
        ssl.setPort(port);
        ssl.setWantClientAuth(false); // Don't care about client authentication.
        ssl.setPassword(_secMan.getKeyStorePassword());
        ssl.setKeyPassword(_secMan.getKeyStorePassword());
        ssl.setKeystoreType(_secMan.getKeyStoreType());
        ssl.setKeystore(KeyStoreFactory.getKeyStoreLocation());
        ssl.setName("SSL Listener-" + ++listeners);

        return ssl;
    }

    /**
     * Add a listener to the server.
     * @param listener
     * @throws Exception 
     */
    public static void addListener(Connector listener) throws Exception{
        if (_server != null){
            listener.start();
            _server.addConnector(listener);
        }
    }

    /**
     * Remove a listener from the server.
     * @param listener
     */
    public static void removeListener(Connector listener){
        if (_server != null){
            _server.removeConnector(listener);
        }
    }

    /**
     * Add a listener to the server
     * If a listener listening to the same port as the provided listener 
     * uses already exists within the server, replace the one already used by
     * the server with the provided listener.
     * @param listener
     * @throws Exception 
     */
    public static void replaceListener(Connector listener) throws Exception{
        if (_server != null){
            for (Connector currentListener : _server.getConnectors()){
                if (currentListener.getPort() == listener.getPort()){
                    _server.removeConnector(currentListener);
                    try {
                        currentListener.stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            listener.start();
            _server.addConnector(listener);
        }
    }

    /**
     * Get all listeners of the server.
     * @return
     */
    public static Connector[] getListeners(){
        if (_server != null){
            return _server.getConnectors();
        }
        return new Connector[0];
    }

    /**
     * Removes all listeners
     */
    public static void clearListeners(){
        if (_server != null){
            for (Connector listen : getListeners()){
                _server.removeConnector(listen);
            }
        }
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
                    } catch (InterruptedException e) {} catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    private static void stop() {
        ConfigurationManager.writeConfFile();
        _secMan.stopTimedEvents();

        try {
            if (_server != null){
                _server.stop();
                for (Connector listener : _server.getConnectors()){
                    listener.stop();
                }
                _server.destroy();
                _server = null;
            }
        } catch (Exception e) {
            _log.error("Stopping server" + e);
        }
        // Get and stop all running threads
        ThreadGroup threadgroup = Thread.currentThread().getThreadGroup();
        Thread[] threads = new Thread[threadgroup.activeCount()+3];
        threadgroup.enumerate(threads, true);
        for (Thread thread : threads){
//            System.out.println("Active thread: " + thread.getName());
            if (thread != null ){//&& thread.isAlive()){
                thread.interrupt();
            }
        }

        for (Thread thread : threads){
            if (thread != null){
                System.out.println("Active thread: " + thread.getName());
                //if (thread != null && thread.isAlive()){
                //    thread.interrupt();
                //}
            }
        }
        //Thread.currentThread().getName()
        threadgroup.interrupt();

        //Thread.currentThread().getThreadGroup().destroy();
    }

    public static String getPluginDir(){
        return _pluginDir;
    }
}
