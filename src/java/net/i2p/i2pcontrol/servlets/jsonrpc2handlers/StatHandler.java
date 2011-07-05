package net.i2p.i2pcontrol.servlets.jsonrpc2handlers;

import java.util.HashMap;
import java.util.Map;

import net.i2p.I2PAppContext;
import net.i2p.stat.RateStat;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

public class StatHandler implements RequestHandler {

	private String[] requiredArgs = {"stat", "period"};
	// Reports the method names of the handled requests
	public String[] handledRequests() {
		return new String[]{"getRate"};
	}
	
	// Processes the requests
	public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
		if (req.getMethod().equals("getRate")) {
			JSONRPC2Error err = JSONRPC2Helper.validateParams(requiredArgs, req);
			if (err != null)
				return new JSONRPC2Response(err, req.getID());
			
			HashMap inParams = (HashMap) req.getParams();
			
			String input = (String) inParams.get("stat");
			if (input == null){
				return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
			}
			long period;
			try{
				period = (Long) inParams.get("period");
			} catch (NumberFormatException e){
				return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
			}

			RateStat rate = I2PAppContext.getGlobalContext().statManager().getRate(input);
			
			// If RateStat or the requested period doesn't already exist, create them.
			if (rate == null || rate.getRate(period) == null){
				long[] tempArr = new long[1];
				tempArr[0] = period;
				I2PAppContext.getGlobalContext().statManager().createRequiredRateStat(input, "I2PControl", "I2PControl", tempArr);
				rate = I2PAppContext.getGlobalContext().statManager().getRate(input);
			}
			if (rate.getRate(period) == null)
				return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, req.getID());
			Map outParams = new HashMap();
			outParams.put("result", rate.getRate(period).getAverageValue());
			return new JSONRPC2Response(outParams, req.getID());
		}
		return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
	}
}
