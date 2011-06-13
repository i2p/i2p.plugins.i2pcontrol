package com.thetransactioncompany.jsonrpc2.server;


import com.thetransactioncompany.jsonrpc2.*;


/**
 * Interface for handling JSON-RPC 2.0 requests.
 *
 * @author <a href="http://dzhuvinov.com">Vladimir Dzhuvinov</a>
 * @version 1.3 (2011-03-05)
 */
public interface RequestHandler {

	
	/**
	 * Gets the JSON-RPC 2.0 request method names that this handler 
	 * processes.
	 *
	 * @return The method names of the served JSON-RPC 2.0 requests.
	 */
	public String[] handledRequests();
	
	
	/**
	 * Processes a JSON-RPC 2.0 request.
	 *
	 * @param request    A valid JSON-RPC 2.0 request instance.
	 * @param requestCtx Context information about the request, may be 
	 *                   {@code null} if undefined.
	 *
	 * @return The resulting JSON-RPC 2.0 response.
	 */
	public JSONRPC2Response process(final JSONRPC2Request request, final MessageContext requestCtx);

}
