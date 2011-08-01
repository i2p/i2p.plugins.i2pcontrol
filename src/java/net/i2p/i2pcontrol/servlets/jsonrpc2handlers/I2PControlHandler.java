package net.i2p.i2pcontrol.servlets.jsonrpc2handlers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.jetty.Server;
import org.mortbay.util.InetAddrPort;

import net.i2p.I2PAppContext;
import net.i2p.data.RouterInfo;
import net.i2p.i2pcontrol.I2PControlController;
import net.i2p.i2pcontrol.router.RouterManager;
import net.i2p.i2pcontrol.security.SecurityManager;
import net.i2p.i2pcontrol.servlets.configuration.ConfigurationManager;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;
import net.i2p.router.transport.CommSystemFacadeImpl;
import net.i2p.router.transport.FIFOBandwidthRefiller;
import net.i2p.router.transport.TransportManager;
import net.i2p.router.transport.udp.UDPTransport;
import net.i2p.util.Log;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

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

public class I2PControlHandler implements RequestHandler {
    private static final int BW_BURST_PCT = 110;
    private static final int BW_BURST_TIME = 20;
    private static RouterContext _context;
    private static final Log _log = I2PAppContext.getGlobalContext().logManager().getLog(I2PControlHandler.class);
    private static final ConfigurationManager _conf = ConfigurationManager.getInstance();
    
    static{
    	try {
			_context = RouterManager.getRouterContext();
		} catch (Exception e) {
			_log.error("Unable to initialize RouterContext.", e);
		}
    }
    
	// Reports the method names of the handled requests
	public String[] handledRequests() {
		return new String[]{"I2PControl"};
	}
	
	// Processes the requests
	public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
		if (req.getMethod().equals("I2PControl")) {
			return process(req);
		}else {
			// Method name not supported
			return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
		}
	}
	
	
	private JSONRPC2Response process(JSONRPC2Request req){
		JSONRPC2Error err = JSONRPC2Helper.validateParams(null, req);
		if (err != null)
			return new JSONRPC2Response(err, req.getID());
		
		if (_context == null){
			return new JSONRPC2Response(
					new JSONRPC2Error(JSONRPC2Error.INTERNAL_ERROR.getCode(), 
							"RouterContext was not initialized. Query failed"), 
							req.getID());
		}
		HashMap inParams = (HashMap) req.getParams();
		Map outParams = new HashMap();
		

		boolean restartNeeded = false;
		boolean settingsSaved = false;
		String inParam;
		
		if (inParams.containsKey("i2pcontrol.port")){
			Integer  oldPort = _conf.getConf("i2pcontrol.listen.port", 7650);
			if ((inParam = (String) inParams.get("i2pcontrol.port")) != null){				
				if (oldPort == null || !inParam.equals(oldPort.toString())){
					Integer newPort;
					try {
						newPort = Integer.valueOf(inParam);
						if (newPort < 1 || newPort > 65535){
							throw new NumberFormatException();
						}
					} catch (NumberFormatException e){
						return new JSONRPC2Response(
								new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
										"\"i2pcontrol.port\" must be a string representing a number in the range 1-65535. " + inParam + " isn't valid."), 
										req.getID());
					}
					try {
						_conf.setConf("i2pcontrol.listen.port", newPort);
						Server server = I2PControlController.buildServer();
						I2PControlController.setServer(server);
						ConfigurationManager.writeConfFile();
						outParams.put("i2pcontrol.port", null);
						settingsSaved = true;						
					} catch (Exception e) {
						_conf.setConf("i2pcontrol.listen.port", oldPort);
						return new JSONRPC2Response(
								new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
										"\"i2pcontrol.port\" has been set to a port that is already in use, reverting. " +
										inParam + " is an already used port."),
										req.getID());
					}
				}
			}
		}
		
		if(inParams.containsKey("i2pcontrol.password")){
			if ((inParam = (String) inParams.get("i2pcontrol.password")) != null){
				if (SecurityManager.setPasswd(inParam)){
					outParams.put("i2pcontrol.password", null);
					settingsSaved = true;
				}
				ConfigurationManager.writeConfFile();
			}
		}
		
		if(inParams.containsKey("i2pcontrol.address")){
			String  oldAddress = _conf.getConf("i2pcontrol.listen.address", "127.0.0.1");
			if ((inParam = (String) inParams.get("i2pcontrol.address")) != null){				
				if ((oldAddress == null || !inParam.equals(oldAddress.toString()) && 
						(inParam.equals("0.0.0.0") || inParam.equals("127.0.0.1")))){
					InetAddrPort newAddress;
					try {
						newAddress = new InetAddrPort(inParam ,1000);
					} catch (UnknownHostException e){
						return new JSONRPC2Response(
								new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
										"\"i2pcontrol.address\" must be a string representing a hostname or ipaddress. " + inParam + " isn't valid."), 
										req.getID());
					}
					try {
						_conf.setConf("i2pcontrol.listen.address", inParam);
						I2PControlController._server.stop();
						Server server = I2PControlController.buildServer();
						I2PControlController.setServer(server);
						ConfigurationManager.writeConfFile();
						outParams.put("i2pcontrol.address", null);
						settingsSaved = true;						
					} catch (Exception e) {
						_conf.setConf("i2pcontrol.listen.address", oldAddress);
						_log.error("Client tried to set listen address to, " + newAddress + " which isn't valid", e);
						return new JSONRPC2Response(
								new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
										"\"i2pcontrol.address\" has been set to an invalid address, reverting. "),	req.getID());
					}
				}
			} else {
				outParams.put("i2pcontrol.address", oldAddress);
			}
		}
		
        
		outParams.put("SettingsSaved", settingsSaved);
		outParams.put("RestartNeeded", restartNeeded);
		return new JSONRPC2Response(outParams, req.getID());
	}
}
