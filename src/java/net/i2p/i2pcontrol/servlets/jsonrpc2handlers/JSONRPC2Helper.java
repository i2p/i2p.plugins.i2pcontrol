package net.i2p.i2pcontrol.servlets.jsonrpc2handlers;

import java.util.HashMap;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class JSONRPC2Helper {

	public static JSONRPC2Error validateParams(String[] requiredArgs, JSONRPC2Request req){
		// Error on unnamed parameters
		if (req.getParamsType() != JSONRPC2ParamsType.OBJECT){
			return JSONRPC2Error.INVALID_PARAMS;
		}
		HashMap params = (HashMap) req.getParams();
		String missingArgs = "";
		for (int i = 0; i < requiredArgs.length; i++){
			if (!params.containsKey(requiredArgs[i])){
				missingArgs = missingArgs.concat(requiredArgs[i] + ",");
			}
		}
		if (missingArgs.length() > 0){
			missingArgs = missingArgs.substring(0, missingArgs.length()-1);
			return new JSONRPC2ExtendedError(JSONRPC2ExtendedError.CODE_MISSING_PARAMETER, "Missing parameter(s): " + missingArgs);
		}
		return null;
	}
}
