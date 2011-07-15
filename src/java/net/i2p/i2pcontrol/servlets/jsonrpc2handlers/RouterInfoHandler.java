package net.i2p.i2pcontrol.servlets.jsonrpc2handlers;

import java.util.HashMap;
import java.util.Map;

import net.i2p.I2PAppContext;
import net.i2p.data.DataHelper;
import net.i2p.data.RouterAddress;
import net.i2p.data.RouterInfo;
import net.i2p.i2pcontrol.I2PControlController;
import net.i2p.i2pcontrol.router.RouterManager;
import net.i2p.router.CommSystemFacade;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;
import net.i2p.router.RouterVersion;
import net.i2p.router.networkdb.kademlia.FloodfillNetworkDatabaseFacade;
import net.i2p.router.transport.CommSystemFacadeImpl;
import net.i2p.router.transport.FIFOBandwidthRefiller;
import net.i2p.router.transport.TransportManager;
import net.i2p.router.transport.ntcp.NTCPAddress;
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

public class RouterInfoHandler implements RequestHandler {
	private static RouterContext _context;
	private static final Log _log = I2PAppContext.getGlobalContext().logManager().getLog(RouterInfoHandler.class);

	static {
		try {
			_context = RouterManager.getRouterContext();
		} catch (Exception e) {
			_log.error("Unable to initialize RouterContext.", e);
		}
	}

	// Reports the method names of the handled requests
	public String[] handledRequests() {
		return new String[] { "RouterInfo" };
	}

	// Processes the requests
	public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
		if (req.getMethod().equals("RouterInfo")) {
			return process(req);
		} else {
			// Method name not supported
			return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND,
					req.getID());
		}
	}

	private JSONRPC2Response process(JSONRPC2Request req) {
		JSONRPC2Error err = JSONRPC2Helper.validateParams(null, req);
		if (err != null)
			return new JSONRPC2Response(err, req.getID());

		if (_context == null) {
			return new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INTERNAL_ERROR.getCode(),
					"RouterContext was not initialized. Query failed"),
					req.getID());
		}
		HashMap inParams = (HashMap) req.getParams();
		Map outParams = new HashMap();

		if (inParams.containsKey("i2p.router.version")) {
			outParams.put("i2p.router.version", RouterVersion.FULL_VERSION);
		}

		if (inParams.containsKey("i2p.router.uptime")) {
			Router router = _context.router();
			if (router == null) {
				outParams.put("i2p.router.uptime", "[not up]");
			} else {
				outParams.put("i2p.router.uptime", DataHelper.formatDuration2(router.getUptime()));
			}
		}
		
		if (inParams.containsKey("i2p.router.status")) {
			outParams.put("i2p.router.status", _context.throttle().getTunnelStatus());
		}

		if (inParams.containsKey("i2p.router.net.status")) {
			outParams.put("i2p.router.net.status", getNetworkStatus());
		}

		return new JSONRPC2Response(outParams, req.getID());
	}

	
	// Ripped out of SummaryHelper.java
	private String getNetworkStatus() {
		if (_context.router().getUptime() > 60 * 1000
				&& (!_context.router().gracefulShutdownInProgress())
				&& !_context.clientManager().isAlive())
			return ("ERR-Client Manager I2CP Error - check logs");
		long skew = _context.commSystem().getFramedAveragePeerClockSkew(33);
		// Display the actual skew, not the offset
		if (Math.abs(skew) > 60 * 1000)
			return "ERR-Clock Skew of " + Math.abs(skew);
		if (_context.router().isHidden())
			return ("Hidden");

		int status = _context.commSystem().getReachabilityStatus();
		switch (status) {
		case CommSystemFacade.STATUS_OK:
			RouterAddress ra = _context.router().getRouterInfo().getTargetAddress("NTCP");
			if (ra == null || (new NTCPAddress(ra)).isPubliclyRoutable())
				return "OK";
			return "ERR-Private TCP Address";
		case CommSystemFacade.STATUS_DIFFERENT:
			return "ERR-SymmetricNAT";
		case CommSystemFacade.STATUS_REJECT_UNSOLICITED:
			if (_context.router().getRouterInfo().getTargetAddress("NTCP") != null)
				return "WARN-Firewalled with Inbound TCP Enabled";
			if (((FloodfillNetworkDatabaseFacade) _context.netDb())
					.floodfillEnabled())
				return "WARN-Firewalled and Floodfill";
			if (_context.router().getRouterInfo().getCapabilities()
					.indexOf('O') >= 0)
				return "WARN-Firewalled and Fast";
			return "Firewalled";
		case CommSystemFacade.STATUS_HOSED:
			return "ERR-UDP Port In Use";
		case CommSystemFacade.STATUS_UNKNOWN: // fallthrough
		default:
			ra = _context.router().getRouterInfo().getTargetAddress("SSU");
			if (ra == null && _context.router().getUptime() > 5 * 60 * 1000) {
				if (_context.commSystem().countActivePeers() <= 0)
					return "ERR-No Active Peers, Check Network Connection and Firewall";
				else if (_context.getProperty(CommSystemFacadeImpl.PROP_I2NP_NTCP_HOSTNAME) == null || _context.getProperty(CommSystemFacadeImpl.PROP_I2NP_NTCP_PORT) == null)
					return "ERR-UDP Disabled and Inbound TCP host/port not set";
				else
					return "WARN-Firewalled with UDP Disabled";
			}
			return "Testing";
		}
	}
}
