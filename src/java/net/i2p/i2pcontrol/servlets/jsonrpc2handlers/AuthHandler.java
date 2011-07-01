package net.i2p.i2pcontrol.servlets.jsonrpc2handlers;

import java.util.HashMap;
import java.util.Map;

import net.i2p.i2pcontrol.security.AuthToken;
import net.i2p.i2pcontrol.security.SecurityManager;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

public class AuthHandler implements RequestHandler {

	private String[] requiredArgs = {"password"};
	// Reports the method names of the handled requests
	public String[] handledRequests() {
		return new String[]{"authenticate"};
	}
	
	// Processes the requests
	public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
		if (req.getMethod().equals("authenticate")) {
			JSONRPC2Error err = JSONRPC2Helper.validateParams(requiredArgs, req);
			if (err != null)
				return new JSONRPC2Response(err, req.getID());
			
			HashMap inParams = (HashMap) req.getParams();
			
			String pwd = (String) inParams.get("password");
			
			// Try get an AuthToken
			AuthToken token = SecurityManager.validatePasswd(pwd);
			if (token == null){
				return new JSONRPC2Response(JSONRPC2ExtendedError.INVALID_PASSWORD, req.getID());
			}
			
			Map outParams = new HashMap();
			outParams.put("tokenID", token.getId());				
			return new JSONRPC2Response(outParams, req.getID());
		} else {
			// Method name not supported
			return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
		}
	}
}
