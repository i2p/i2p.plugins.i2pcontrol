package net.i2p.i2pcontrol.servlets.jsonrpc2handlers;

import java.util.HashMap;
import java.util.Map;

import org.tanukisoftware.wrapper.WrapperManager;

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
import net.i2p.router.networkdb.reseed.ReseedChecker;
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

public class RouterManagerHandler implements RequestHandler {
	private static RouterContext _context;
	private static final Log _log = I2PAppContext.getGlobalContext().logManager().getLog(RouterManagerHandler.class);
	
	private final static int SHUTDOWN_WAIT = 1500;

	static {
		try {
			_context = RouterManager.getRouterContext();
		} catch (Exception e) {
			_log.error("Unable to initialize RouterContext.", e);
		}
	}

	// Reports the method names of the handled requests
	public String[] handledRequests() {
		return new String[] { "RouterManager" };
	}

	// Processes the requests
	public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
		if (req.getMethod().equals("RouterManager")) {
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

		if (inParams.containsKey("Shutdown")) {
			outParams.put("Shutdown", null);
			(new Thread(){
				@Override
				public void run(){
					try {
						Thread.sleep(SHUTDOWN_WAIT);
					} catch (InterruptedException e) {}
		            _context.addShutdownTask(new UpdateWrapperManagerTask(Router.EXIT_HARD));
		            _context.router().shutdown(Router.EXIT_HARD);				}
			}).start();
			return new JSONRPC2Response(outParams, req.getID());
		}
		
		if (inParams.containsKey("Restart")) {
			outParams.put("Restart", null);
			(new Thread(){
				@Override
				public void run(){
					try {
						Thread.sleep(SHUTDOWN_WAIT);
					} catch (InterruptedException e) {}
		            _context.addShutdownTask(new UpdateWrapperManagerTask(Router.EXIT_HARD_RESTART));
		            _context.router().shutdown(Router.EXIT_HARD_RESTART);
				}
			}).start();
			return new JSONRPC2Response(outParams, req.getID());
		}
		
		if (inParams.containsKey("ShutdownGraceful")) {
			outParams.put("ShutdownGraceful", null);
			(new Thread(){
				@Override
				public void run(){
					try {
						Thread.sleep(SHUTDOWN_WAIT);
					} catch (InterruptedException e) {}
		            _context.addShutdownTask(new UpdateWrapperManagerTask(Router.EXIT_GRACEFUL));
		            _context.router().shutdownGracefully();
		        }
			}).start();
			return new JSONRPC2Response(outParams, req.getID());
		}
		
		if (inParams.containsKey("RestartGraceful")) {
			outParams.put("RestartGraceful", null);
			(new Thread(){
				@Override
				public void run(){
					try {
						Thread.sleep(SHUTDOWN_WAIT);
					} catch (InterruptedException e) {}
		            _context.addShutdownTask(new UpdateWrapperManagerTask(Router.EXIT_GRACEFUL_RESTART));
		            _context.router().shutdownGracefully(Router.EXIT_GRACEFUL_RESTART);				}
			}).start();
			return new JSONRPC2Response(outParams, req.getID());
		}
		
		if (inParams.containsKey("Reseed")){
			outParams.put("Reseed", null);
			(new Thread(){
				@Override
				public void run(){
					ReseedChecker reseeder = new ReseedChecker(_context);
					reseeder.requestReseed();
				}
			}).start();
		}
		
		return new JSONRPC2Response(outParams, req.getID());
	}
	
    public static class UpdateWrapperManagerTask implements Runnable {
        private int _exitCode;
        public UpdateWrapperManagerTask(int exitCode) {
            _exitCode = exitCode;
        }
        public void run() {
            try {
                WrapperManager.signalStopped(_exitCode);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
