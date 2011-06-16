package net.i2p.i2pcontrol;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;

import java.text.*;
import java.util.*;

import javax.servlet.http.*;


/**
 * Demonstration of the JSON-RPC 2.0 Server framework usage. The request
 * handlers are implemented as static nested classes for convenience, but in 
 * real life applications may be defined as regular classes within their old 
 * source files.
 *
 * @author Vladimir Dzhuvinov
 * @version 2011-03-05
 */ 
public class Example {


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
				
				List params = (List)req.getParams();
			
				Object input = params.get(0);
			
				return new JSONRPC2Response(input, req.getID());
			}
			else {
				// Method name not supported
				
				return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
			}
		}
	}
	
	
	// Implements a handler for "getDate" and "getTime" JSON-RPC methods
	// that return the current date and time
	public static class DateTimeHandler implements RequestHandler {
	
	
		// Reports the method names of the handled requests
		public String[] handledRequests() {
		
			return new String[]{"getDate", "getTime"};
		}
		
		
		// Processes the requests
		public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
		
			if (req.getMethod().equals("getDate")) {			
				DateFormat df = DateFormat.getDateInstance();
				
				String date = df.format(new Date());
				
				return new JSONRPC2Response(date, req.getID());
			}
			else if (req.getMethod().equals("getTime")) {
			
				DateFormat df = DateFormat.getTimeInstance();
				
				String time = df.format(new Date());
				
				return new JSONRPC2Response(time, req.getID());
			}
			else {
			
				// Method name not supported
				
				return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
			}
		}
	}


	public static void main(String[] args) {

		
		// Create a new JSON-RPC 2.0 request dispatcher
		Dispatcher dispatcher =  new Dispatcher();
		
		
		// Register the "echo", "getDate" and "getTime" handlers with it
		dispatcher.register(new EchoHandler());
		dispatcher.register(new DateTimeHandler());
		
		// Simulate an "echo" JSON-RPC 2.0 request
		List echoParam = new LinkedList();
		echoParam.add("Hello world!");
		
		JSONRPC2Request req = new JSONRPC2Request("echo", echoParam, "req-id-01");
		System.out.println("Request: \n" + req);
		
		JSONRPC2Response resp = dispatcher.dispatch(req, null);
		System.out.println("Response: \n" + resp);
		
		
		// Simulate a "getDate" JSON-RPC 2.0 request
		req = new JSONRPC2Request("getDate", "req-id-02");
		System.out.println("Request: \n" + req);
		
		resp = dispatcher.dispatch(req, null);
		System.out.println("Response: \n" + resp);
		
		
		// Simulate a "getTime" JSON-RPC 2.0 request
		req = new JSONRPC2Request("getTime", "req-id-03");
		System.out.println("Request: \n" + req);
		
		resp = dispatcher.dispatch(req, null);
		System.out.println("Response: \n" + resp);
	}
}