package net.i2p.i2pcontrol;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.i2p.I2PAppContext;
import net.i2p.stat.RateStat;
import net.i2p.util.Log;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;


/**
 * Provide an JSON-RPC 2.0 API for remote controlling of I2P
 */
public class JSONRPCServlet extends HttpServlet{

	private static final long serialVersionUID = -45075606818515212L;
	private static final int BUFFER_LENGTH = 2048;
	private static Dispatcher disp;
	private static char[] readBuffer;
	private static I2PControlManager _manager;
	private static Log _log;

	
	@Override
	public void init(){
		_log = I2PAppContext.getGlobalContext().logManager().getLog(JSONRPCServlet.class);
		readBuffer = new char[BUFFER_LENGTH];
		_manager = I2PControlManager.getInstance();
		
		disp = new Dispatcher();
		disp.register(new EchoHandler());
		disp.register(new StatHandler());
	}
	
	@Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
    	httpServletResponse.setContentType("text/html");
        PrintWriter out = httpServletResponse.getWriter();
        out.println("Nothing to see here");
        out.close();
    }
	
	@Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {	
    	String req = getRequest(httpServletRequest.getInputStream());
    	httpServletResponse.setContentType("application/json");
    	PrintWriter out = httpServletResponse.getWriter();
    	JSONRPC2Message msg = null;
    	JSONRPC2Response jsonResp = null;
		try {

	    	msg = JSONRPC2Message.parse(req);

	    	if (msg instanceof JSONRPC2Request) {
	    		jsonResp = disp.dispatch((JSONRPC2Request)msg, null);
		    	_manager.prependHistory("Request: " + msg);
		    	_manager.prependHistory("Response: " + jsonResp);
	    		System.out.println("The message is a Request");
	    	}
	    	else if (msg instanceof JSONRPC2Notification) {
	    		disp.dispatch((JSONRPC2Notification)msg, null);
		    	_manager.prependHistory("Notification: " + msg);
	    		System.out.println("The message is a Notification");
	    	}
	    	
	        out.println(jsonResp);
	        out.close();
		} catch (JSONRPC2ParseException e) {
			_log.error("Unable to parse JSONRPC2Message: " + e.getMessage());
		}
    }	
    
    private String getRequest(ServletInputStream sis) throws IOException{
    	Writer writer = new StringWriter();
    	
    	BufferedReader reader = new BufferedReader(new InputStreamReader(sis,"UTF-8"));
    	int n;
    	while ((n = reader.read(readBuffer)) != -1) {
    		writer.write(readBuffer, 0, n);
    	}
    	return writer.toString();
    }
    
    
	// Implements a handler for an "echo" JSON-RPC method
	public static class EchoHandler implements RequestHandler {

		// Reports the method names of the handled requests
		public String[] handledRequests() {
			return new String[]{"echo"};
		}
		
		// Processes the requests
		public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
			if (req.getMethod().equals("echo")) {
				// Echo first parameter
				if (req.getParamsType() != JSONRPC2ParamsType.OBJECT){
					return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
				}		
				Map params = (HashMap) req.getParams();
				String input = (String) params.get("echo");
				Map outParams = new HashMap();
				outParams.put("result", input);
				return new JSONRPC2Response(outParams, req.getID());
			}
			else {
				// Method name not supported
				return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
			}
		}
	}
	
	public static class StatHandler implements RequestHandler {
		public String[] handledRequests() {
			return new String[]{"getRate"};
		}

		@Override
		public JSONRPC2Response process(JSONRPC2Request req,
				MessageContext ctx) {
			if (req.getMethod().equals("getRate")) {
				
				// Error on unnamed parameters
				if (req.getParamsType() != JSONRPC2ParamsType.OBJECT){
					return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
				}
				
				// Error on wrong amount of parameters
				Map params = (HashMap)req.getParams();
				if (params.size()!=2)
					return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
				
				String input = (String) params.get("stat");
				long period = (Long) params.get("period");

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

}
