package net.i2p.i2pcontrol.servlets.jsonrpc2handlers;

import java.util.HashMap;
import java.util.Map;

import net.i2p.I2PAppContext;
import net.i2p.data.RouterInfo;
import net.i2p.i2pcontrol.I2PControlController;
import net.i2p.i2pcontrol.router.RouterManager;
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

public class OLDNetworkInfoHandler implements RequestHandler {
    private static final int BW_BURST_PCT = 110;
    private static RouterContext _context;
    private static final Log _log = I2PAppContext.getGlobalContext().logManager().getLog(OLDNetworkInfoHandler.class);
    
    static{
    	try {
			_context = RouterManager.getRouterContext();
		} catch (Exception e) {
			_log.error("Unable to initialize RouterContext.", e);
		}
    }
    
	// Reports the method names of the handled requests
	public String[] handledRequests() {
		return new String[]{"getNetworkInfo", "setNetworkInfo"};
	}
	
	// Processes the requests
	public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
		if (req.getMethod().equals("getNetworkInfo")) {
			return processGet(req, ctx);
		} else if (req.getMethod().equals("setNetworkInfo")){
			return processSet(req, ctx);
		}else {
			// Method name not supported
			return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
		}
	}
	
	// TODO: Everything
	@SuppressWarnings("unchecked")
	private JSONRPC2Response processGet(JSONRPC2Request req, MessageContext ctx){
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

		/*
		 * i2p.router.net.ntcp.port
		 * i2p.router.net.ntcp.hostname
		 * i2p.router.net.ntcp.autoip // true|always|false //disables autodetect|disabled //disables ntcp
		 * i2p.router.net.ssu.port
		 * i2p.router.net.ssu.hostname
		 * i2p.router.net.ssu.detectedip
		 * i2p.router.net.ssu.autoip //[local,upnp,ssu] any of prev., in order |fixed // fixed = no detection
		 * i2p.router.net.upnp //
		 * i2p.router.net.bw.share
		 * i2p.router.net.bw.in
		 * i2p.router.net.bw.out
		 * i2p.router.net.laptopmode
		 */
		
		boolean returnAll = inParams.containsKey("all");
		if (returnAll || inParams.containsKey("i2p.router.net.ntcp.port"))
			outParams.put("i2p.router.net.ntcp.port", _context.getProperty(CommSystemFacadeImpl.PROP_I2NP_NTCP_PORT));
		if (returnAll || inParams.containsKey("i2p.router.net.ntcp.hostname"))
			outParams.put("i2p.router.net.ntcp.hostname", _context.getProperty(CommSystemFacadeImpl.PROP_I2NP_NTCP_HOSTNAME));
		if (returnAll || inParams.containsKey("i2p.router.net.ntcp.autoip"))
			outParams.put("i2p.router.net.ntcp.autoip", _context.getProperty(CommSystemFacadeImpl.PROP_I2NP_NTCP_AUTO_IP));
		if (returnAll || inParams.containsKey("i2p.router.net.ssu.port"))
			outParams.put("i2p.router.net.ssu.port", _context.getProperty(UDPTransport.PROP_EXTERNAL_PORT));
		if (returnAll || inParams.containsKey("i2p.router.net.ssu.hostname"))
			outParams.put("i2p.router.net.ssu.hostname", _context.getProperty(UDPTransport.PROP_EXTERNAL_HOST));
		if (returnAll || inParams.containsKey("i2p.router.net.ssu.autoip"))
			outParams.put("i2p.router.net.ssu.autoip", _context.getProperty(UDPTransport.PROP_SOURCES));
		if (returnAll || inParams.containsKey("i2p.router.net.ssu.detectedip")){
			outParams.put("i2p.router.net.ssu.detectedip", _context.router().getRouterInfo().getTargetAddress("SSU"));
		}
		if (returnAll || inParams.containsKey("i2p.router.net.upnp"))
			outParams.put("i2p.router.net.upnp", _context.getProperty(TransportManager.PROP_ENABLE_UPNP));
		if (returnAll || inParams.containsKey("i2p.router.net.bw.share"))
			outParams.put("i2p.router.net.bw.share", _context.router().getConfigSetting(Router.PROP_BANDWIDTH_SHARE_PERCENTAGE));
		if (returnAll || inParams.containsKey("i2p.router.net.bw.in"))
			outParams.put("i2p.router.net.bw.in", _context.getProperty(FIFOBandwidthRefiller.PROP_INBOUND_BANDWIDTH));
		if (returnAll || inParams.containsKey("i2p.router.net.bw.out"))
			outParams.put("i2p.router.net.bw.out", _context.getProperty(FIFOBandwidthRefiller.PROP_OUTBOUND_BANDWIDTH));
		if (returnAll || inParams.containsKey("i2p.router.net.laptopmode"))
			outParams.put("i2p.router.net.laptopmode", _context.getProperty(UDPTransport.PROP_LAPTOP_MODE));

		return new JSONRPC2Response(outParams, req.getID());
	}
	
	private JSONRPC2Response processSet(JSONRPC2Request req, MessageContext ctx){
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
		
		/*
		 * require.restart - [true|false]
		 */
		boolean restartNeeded = false;
		String inParam;
		if ((inParam = (String) inParams.get("i2p.router.net.ntcp.port")) != null){
			String oldNTCPPort = _context.getProperty(CommSystemFacadeImpl.PROP_I2NP_NTCP_PORT);
			if (oldNTCPPort == null || !oldNTCPPort.equals(inParam.trim())){
				_context.router().setConfigSetting(CommSystemFacadeImpl.PROP_I2NP_NTCP_PORT, inParam);
				outParams.put("i2p.router.net.ntcp.port", true);
				restartNeeded = true;
			}
		}
		if ((inParam = (String) inParams.get("i2p.router.net.ntcp.hostname")) != null){
			String oldNTCPHostname = _context.getProperty(CommSystemFacadeImpl.PROP_I2NP_NTCP_HOSTNAME);
			if (oldNTCPHostname == null || !oldNTCPHostname.equals(inParam.trim())){
				_context.router().setConfigSetting(CommSystemFacadeImpl.PROP_I2NP_NTCP_HOSTNAME, inParam);
				outParams.put("i2p.router.net.ntcp.hostname", true);
				restartNeeded = true;
			}
		}
		if ((inParam = (String) inParams.get("i2p.router.net.ntcp.autoip")) != null){
			String oldNTCPAutoIP = _context.getProperty(CommSystemFacadeImpl.PROP_I2NP_NTCP_AUTO_IP);
			inParam = inParam.trim().toLowerCase();
			if (oldNTCPAutoIP == null || !oldNTCPAutoIP.equals(inParam)){
				if ("always".equals(inParam) || "true".equals(inParam) || "false".equals(inParam)){
					_context.setProperty(CommSystemFacadeImpl.PROP_I2NP_NTCP_AUTO_IP, inParam);
					outParams.put("i2p.router.net.ntcp.autoip", true);
					restartNeeded = true;
				} else {
					return new JSONRPC2Response(
							new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
									"\"i2p.router.net.ntcp.autoip\" can only be always, true or false. " + inParam + " isn't valid."), 
									req.getID());
				}
			}
		}		
		if ((inParam = (String) inParams.get("i2p.router.net.ssu.port")) != null){
			String oldSSUPort = _context.getProperty(UDPTransport.PROP_EXTERNAL_PORT);
			if (oldSSUPort== null || !oldSSUPort.equals(inParam.trim())){
				_context.router().setConfigSetting(UDPTransport.PROP_EXTERNAL_PORT, inParam);
				_context.router().setConfigSetting(UDPTransport.PROP_INTERNAL_PORT, inParam);
				outParams.put("i2p.router.net.ssu.port", true);
				restartNeeded = true;
			}
		}
		if ((inParam = (String) inParams.get("i2p.router.net.ssu.hostname")) != null){
			String oldSSUHostname = _context.getProperty(UDPTransport.PROP_EXTERNAL_HOST);
			if (oldSSUHostname == null || !oldSSUHostname.equals(inParam.trim())){
				_context.router().setConfigSetting(UDPTransport.PROP_EXTERNAL_HOST, inParam);
				outParams.put("i2p.router.net.ssu.hostname", true);
				restartNeeded = true;
			}
		}
		if ((inParam = (String) inParams.get("i2p.router.net.ssu.autoip")) != null){
			String oldSSUAutoIP =  _context.getProperty(UDPTransport.PROP_SOURCES);
			inParam = inParam.trim().toLowerCase();
			if (oldSSUAutoIP == null || !oldSSUAutoIP.equals(inParam)){
				if (inParam.equals("ssu") || inParam.equals("local,ssu") || inParam.equals("upnp,ssu") || inParam.equals("local,upnp,ssu")){
	            _context.router().setConfigSetting(UDPTransport.PROP_SOURCES, inParam);
				outParams.put("i2p.router.net.ssu.autoip", true);    
				restartNeeded = true;
				} else {
					return new JSONRPC2Response(
							new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
									"\"i2p.router.net.ssu.autoip\" can only be ssu/local,upnp,ssu/local/ssu/upnp,ssu. " + inParam + " isn't valid."), 
									req.getID());
				}
			}
		}
		if ((inParam = (String) inParams.get("i2p.router.net.upnp")) != null){
			String oldUPNP = _context.getProperty(TransportManager.PROP_ENABLE_UPNP);
			if (oldUPNP == null || !oldUPNP.equals(inParam.trim())){
				_context.router().setConfigSetting(TransportManager.PROP_ENABLE_UPNP, inParam);
				outParams.put("i2p.router.net.upnp", true);
				restartNeeded = true;
			}
		}
		if ((inParam = (String) inParams.get("i2p.router.net.bw.share")) != null){
			String oldShare = _context.router().getConfigSetting(Router.PROP_BANDWIDTH_SHARE_PERCENTAGE);
			if (oldShare == null || !oldShare.equals(inParam.trim())){
				_context.router().setConfigSetting(Router.PROP_BANDWIDTH_SHARE_PERCENTAGE, inParam);
				outParams.put("i2p.router.net.bw.share", true);
			}
		}	
		if ((inParam = (String) inParams.get("i2p.router.net.bw.in")) != null){
			String oldBWIn = _context.getProperty(FIFOBandwidthRefiller.PROP_INBOUND_BANDWIDTH);
			Integer rate;
			try{
				rate = Integer.parseInt(inParam);
				if (rate < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e){
				return new JSONRPC2Response(
						new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
								"\"i2p.router.net.bw.in\" A positive integer must supplied, " + inParam + " isn't valid"), 
								req.getID());
			}
			Integer burstRate = (rate * BW_BURST_PCT)/100;
			if (oldBWIn == null || !oldBWIn.equals(rate.toString())){
                _context.router().setConfigSetting(FIFOBandwidthRefiller.PROP_INBOUND_BURST_BANDWIDTH, burstRate.toString());
                _context.router().setConfigSetting(FIFOBandwidthRefiller.PROP_INBOUND_BANDWIDTH_PEAK, rate.toString());
				outParams.put("i2p.router.net.bw.in", true);
			}
		}
		if ((inParam = (String) inParams.get("i2p.router.net.bw.out")) != null){
			String oldBWOut = _context.getProperty(FIFOBandwidthRefiller.PROP_OUTBOUND_BANDWIDTH);
			Integer rate;
			try{
				rate = Integer.parseInt(inParam);
				if (rate < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e){
				return new JSONRPC2Response(
						new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
								"\"i2p.router.net.bw.out\" A positive integer must supplied, " + inParam + " isn't valid"), 
								req.getID());
			}
			Integer burstRate = (rate * BW_BURST_PCT)/100;
			if (oldBWOut == null || !oldBWOut.equals(rate.toString())){
                _context.router().setConfigSetting(FIFOBandwidthRefiller.PROP_OUTBOUND_BURST_BANDWIDTH, burstRate.toString());
                _context.router().setConfigSetting(FIFOBandwidthRefiller.PROP_OUTBOUND_BANDWIDTH_PEAK, rate.toString());
				outParams.put("i2p.router.net.bw.out", true);
			}
		}
		if ((inParam = (String) inParams.get("i2p.router.net.laptopmode")) != null){
			String oldLaptopMode = _context.getProperty(UDPTransport.PROP_LAPTOP_MODE);
			if (oldLaptopMode == null || !oldLaptopMode.equals(inParam.trim())){
				_context.setProperty(UDPTransport.PROP_LAPTOP_MODE, inParam);
				outParams.put("i2p.router.net.laptopmode", true);
			}
		}	
		
		outParams.put("restart.needed", restartNeeded);
		return new JSONRPC2Response(outParams, req.getID());
	}
}
