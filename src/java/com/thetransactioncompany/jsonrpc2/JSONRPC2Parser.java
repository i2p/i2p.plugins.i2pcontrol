package com.thetransactioncompany.jsonrpc2;


import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.LinkedHashMap;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Parses JSON-RPC 2.0 request, notification and response messages. 
 *
 * <p>This class is not thread-safe. A parser instance should not be used by 
 * more than one thread unless properly synchronised. Alternatively, you may 
 * use the thread-safe {@link JSONRPC2Message#parse} and sister methods.
 *
 * <p>Parsing of batched requests / notifications is not supported.
 *
 * <p>Example:
 *
 * <pre>
 * String jsonString = "{\"method\":\"makePayment\"," +
 *                      "\"params\":{\"recipient\":\"Penny Adams\",\"amount\":175.05}," +
 *                      "\"id\":\"0001\","+
 *                      "\"jsonrpc\":\"2.0\"}";
 *  
 *  JSONRPC2Request req = null;
 *
 * JSONRPC2Parser parser = new JSONRPC2Parser();
 *  
 *  try {
 *          req = parser.parseJSONRPC2Request(jsonString);
 * 
 *  } catch (JSONRPC2ParseException e) {
 *          // handle exception
 *  }
 *
 * </pre>
 *
 * <p id="map">The mapping between JSON and Java entities (as defined by the 
 * underlying JSON.simple library): 
 * <pre>
 *     true|false  <--->  java.lang.Boolean
 *     number      <--->  java.lang.Number
 *     string      <--->  java.lang.String
 *     array       <--->  java.util.List
 *     object      <--->  java.util.Map
 *     null        <--->  null
 * </pre>
 *
 * <p>The JSON-RPC 2.0 specification and user group forum can be found 
 * <a href="http://groups.google.com/group/json-rpc">here</a>.
 * 
 * @author <a href="http://dzhuvinov.com">Vladimir Dzhuvinov</a>
 * @version 1.16 (2011-05-19)
 */
public class JSONRPC2Parser {


	/**
	 * Reusable JSON parser.
	 */
	private JSONParser parser;
	
	
	/**
	 * If {@code true} the order of the parsed JSON object members must be
	 * preserved.
	 */
	private boolean preserveOrder;
	
	
	/**
	 * If {@code true} the {@code "jsonrpc":"2.0"} version field in the 
	 * JSON-RPC 2.0 message must not be checked.
	 */
	private boolean noStrict;
	
	
	/**
	 * Special container factory for constructing JSON objects in a way
	 * that preserves their original member order.
	 */
	private static final ContainerFactory linkedContainerFactory = new ContainerFactory() {
	
		// Yes, there is a typo here!
		public List creatArrayContainer() {
			return new LinkedList();
		}
		
		public Map createObjectContainer() {
			return new LinkedHashMap();
		}
	};
	
	
	/**
	 * Creates a new JSON-RPC 2.0 message parser.
	 *
	 * <p>The member order of parsed JSON objects will not be preserved 
	 * (for efficiency reasons) and the JSON-RPC 2.0 version field must be 
	 * set to "2.0". To change this behaviour check the {@link 
	 * #JSONRPC2Parser(boolean,boolean) alternative constructor}.
	 */
	public JSONRPC2Parser() {
	
		this(false, false);
	}
	
	
	/**
	 * Creates a new JSON-RPC 2.0 message parser.
	 *
	 * @param preserveOrder If {@code true} the member order of JSON objects
	 *                      in parameters and results will be preserved.
	 * @param noStrict      If {@code true} the {@code "jsonrpc":"2.0"}
	 *                      version field in the JSON-RPC 2.0 message will 
	 *                      not be checked.
	 */
	public JSONRPC2Parser(final boolean preserveOrder, final boolean noStrict) {
	
		parser = new JSONParser();
		this.preserveOrder = preserveOrder;
		this.noStrict = noStrict;
	}
	
	
	/**
	 * Parses a JSON object string. Provides the initial parsing of JSON-RPC
	 * 2.0 messages. The member order of JSON objects will be preserved if
	 * {@link #preserveOrder} is set to {@code true}.
	 *
	 * @param jsonString The JSON string to parse.
	 *
	 * @return The parsed JSON object.
	 *
	 * @throws JSONRPC2ParseException With detailed message if parsing 
	 *                                failed.
	 */
	private Map parseJSONObject(final String jsonString)
		throws JSONRPC2ParseException {
	
		if (jsonString == null)
			throw new JSONRPC2ParseException("Null argument", JSONRPC2ParseException.JSON, null);
		
		if (jsonString.trim().isEmpty())
			throw new JSONRPC2ParseException("Invalid JSON: Empty string", JSONRPC2ParseException.JSON, jsonString);
		
		Object json;
		
		// Parse the JSON string
		try {
			if (preserveOrder)
				json = parser.parse(jsonString, linkedContainerFactory);
			else
				json = parser.parse(jsonString);
			
		} catch (ParseException e) {
			// JSON.simple provides no error message
			throw new JSONRPC2ParseException("Invalid JSON", JSONRPC2ParseException.JSON, jsonString);
		} 
		
		if (json instanceof List)
			throw new JSONRPC2ParseException("JSON-RPC 2.0 batch requests/notifications not supported", jsonString);
			
		if (! (json instanceof Map))
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 message: Message must be a JSON object", jsonString);
		
		return (Map)json;
	}
	
	
	/**
	 * Ensures the specified parameter is a {@code String} object set to 
	 * "2.0". This method is intended to check the "jsonrpc" field during 
	 * parsing of JSON-RPC messages.
	 *
	 * @param version    The version parameter.
	 * @param jsonString The original JSON string.
	 *
	 * @throws JSONRPC2Exception If the parameter is not a string matching
	 *                           "2.0".
	 */
	private static void ensureVersion2(final Object version, final String jsonString)
		throws JSONRPC2ParseException {
	
		if (version == null)
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0: Version string missing", jsonString);
			
		else if (! (version instanceof String))
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0: Version not a JSON string", jsonString);
			
		else if (! version.equals("2.0"))
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0: Version must be \"2.0\"", jsonString);
	}
	
	
	/** 
	 * Provides common parsing of JSON-RPC 2.0 requests, notifications 
	 * and responses. Use this method if you don't know which type of 
	 * JSON-RPC message the input string represents.
	 *
	 * <p>If you are certain about the message type use the dedicated 
	 * {@link #parseJSONRPC2Request}, {@link #parseJSONRPC2Notification} 
	 * and {@link #parseJSONRPC2Response} methods. They are more efficient 
	 * and would provide you with more detailed parse error reporting.
	 *
	 * @param jsonString A JSON string representing a JSON-RPC 2.0 request, 
	 *                   notification or response, UTF-8 encoded.
	 *
	 * @return An instance of {@link JSONRPC2Request}, 
	 *         {@link JSONRPC2Notification} or {@link JSONRPC2Response}.
	 *
	 * @throws JSONRPC2ParseException With detailed message if the parsing 
	 *                                failed.
	 */
	public JSONRPC2Message parseJSONRPC2Message(final String jsonString)
		throws JSONRPC2ParseException {
	
		// Try each of the parsers until one succeeds (or all fail)
		try {
			return parseJSONRPC2Request(jsonString);

		} catch (JSONRPC2ParseException e) {
		
			// throw on JSON error, ignore on protocol error
			if (e.getCauseType() == JSONRPC2ParseException.JSON)
				throw e;
		}
		
		try {
			return parseJSONRPC2Notification(jsonString);
			
		} catch (JSONRPC2ParseException e) {
			
			// throw on JSON error, ignore on protocol error
			if (e.getCauseType() == JSONRPC2ParseException.JSON)
				throw e;
		}
		
		try {
			return parseJSONRPC2Response(jsonString);
			
		} catch (JSONRPC2ParseException e) {
			
			// throw on JSON error, ignore on protocol error
			if (e.getCauseType() == JSONRPC2ParseException.JSON)
				throw e;
		}
		
		throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 message", JSONRPC2ParseException.PROTOCOL, jsonString);
	}
	
	
	/** 
	 * Parses a JSON-RPC 2.0 request string.
	 *
	 * @param jsonString The JSON-RPC 2.0 request string, UTF-8 encoded.
	 *
	 * @return The corresponding JSON-RPC 2.0 request object.
	 *
	 * @throws JSONRPC2ParseException With detailed message if the parsing 
	 *                                failed.
	 */
	public JSONRPC2Request parseJSONRPC2Request(final String jsonString)
		throws JSONRPC2ParseException {
	
		// Initial JSON object parsing
		Map json = parseJSONObject(jsonString);
		
		
		// Check for JSON-RPC version "2.0"
		if (! noStrict) {
			Object version = json.get("jsonrpc");
			ensureVersion2(version, jsonString);
		}
			
		
		// Extract method name
		Object method = json.get("method");
		
		if (method == null)
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 request: Method name missing", jsonString);
		else if (! (method instanceof String))
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 request: Method name not a JSON string", jsonString);
		else if (((String)method).length() == 0)
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 request: Method name is an empty string", jsonString);
		
		
		// Extract ID
		if (! json.containsKey("id"))
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 request: Missing identifier", jsonString);
		
		Object id = json.get("id");
		
		if (  id != null             &&
		    !(id instanceof Number ) &&
		    !(id instanceof Boolean) &&
		    !(id instanceof String )    )
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 request: Identifier not a JSON scalar", jsonString);
		
		
		// Extract params
		Object params = json.get("params");
		
		if (params == null)
			return new JSONRPC2Request((String)method, id);
		else if (params instanceof List)
			return new JSONRPC2Request((String)method, (List)params, id);
		else if (params instanceof Map)
			return new JSONRPC2Request((String)method, (Map)params, id);
		else
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 request: Method parameters have unexpected JSON type", jsonString);
	}
	
	
	/** 
	 * Parses a JSON-RPC 2.0 notification string.
	 *
	 * @param jsonString The JSON-RPC 2.0 notification string, UTF-8 
	 *                   encoded.
	 *
	 * @return The corresponding JSON-RPC 2.0 notification object.
	 *
	 * @throws JSONRPC2ParseException With detailed message if the parsing 
	 *                                failed.
	 */
	public JSONRPC2Notification parseJSONRPC2Notification(final String jsonString)
		throws JSONRPC2ParseException {
	
		// Initial JSON object parsing
		Map json = parseJSONObject(jsonString);
		
		
		// Check for JSON-RPC version "2.0"
		if (! noStrict) {
			Object version = json.get("jsonrpc");
			ensureVersion2(version, jsonString);
		}
		
		
		// Extract method name
		Object method = json.get("method");
		
		if (method == null)
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 notification: Method name missing", jsonString);
		else if (! (method instanceof String))
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 notification: Method name not a JSON string", jsonString);
		else if (((String)method).length() == 0)
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 notification: Method name is an empty string", jsonString);
		
				
		// Extract params
		Object params = json.get("params");
		
		if (params == null)
			return new JSONRPC2Notification((String)method);
		else if (params instanceof List)
			return new JSONRPC2Notification((String)method, (List)params);
		else if (params instanceof Map)
			return new JSONRPC2Notification((String)method, (Map)params);
		else
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 notification: Method parameters have unexpected JSON type", jsonString);
	}
	
	
	/** 
	 * Parses a JSON-RPC 2.0 response string.
	 *
	 * @param jsonString The JSON-RPC 2.0 response string, UTF-8 encoded.
	 *
	 * @return The corresponding JSON-RPC 2.0 response object.
	 *
	 * @throws JSONRPC2ParseException With detailed message if the parsing 
	 *                                failed.
	 */
	public JSONRPC2Response parseJSONRPC2Response(final String jsonString)
		throws JSONRPC2ParseException {
	
		// Initial JSON object parsing
		Map json = parseJSONObject(jsonString);
		
		// Check for JSON-RPC version "2.0"
		if (! noStrict) {
			Object version = json.get("jsonrpc");
			ensureVersion2(version, jsonString);
		}
		
		// Extract request ID
		Object id = json.get("id");
		
		if (   id != null             &&
		    ! (id instanceof Boolean) &&
		    ! (id instanceof Number ) &&
		    ! (id instanceof String )    )
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 response: Identifier not a JSON scalar", jsonString);
		
		
		// Extract result/error and create response object
		// Note: result and error are mutually exclusive
		if (json.containsKey("result") && ! json.containsKey("error")) {
			
			// Success
			Object res = json.get("result");
			
			return new JSONRPC2Response(res, id);
					
		}
		else if (! json.containsKey("result") && json.containsKey("error")) {
		
			// Error
			Map errorJSON = (Map)json.get("error");
			
			if (errorJSON == null)
				throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 response: Missing error object", jsonString);
			
			int errorCode;
			try {
				errorCode = ((Long)errorJSON.get("code")).intValue();
			} catch (Exception e) {
				throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 response: Error code missing or not an integer", jsonString);
			}
			
			String errorMessage = null;
			try {
				errorMessage = (String)errorJSON.get("message");
			} catch (Exception e) {
				throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 response: Error message missing or not a string", jsonString);
			}
			
			Object errorData = errorJSON.get("data");
			
			return new JSONRPC2Response(new JSONRPC2Error(errorCode, errorMessage, errorData), id);
			
		}
		else if (json.containsKey("result") && json.containsKey("error")) {
			// Invalid response
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 response: You cannot have result and error at the same time", jsonString);
		}
		else if (! json.containsKey("result") && ! json.containsKey("error")){
			// Invalid response
			throw new JSONRPC2ParseException("Invalid JSON-RPC 2.0 response: Neither result nor error specified", jsonString);
		}
		else {
			throw new AssertionError();
		}
	}
	
	
	/**
	 * Controls the preservation of JSON object member order in parsed
	 * JSON-RPC 2.0 messages.
	 *
	 * @param preserveOrder {@code true} to preserve JSON object member,
	 *                      else {@code false}.
	 */
	public void preserveOrder(final boolean preserveOrder) {
	
		this.preserveOrder = preserveOrder;
	}
	
	
	/**
	 * Returns {@code true} if the order of JSON object members in parsed
	 * JSON-RPC 2.0 messages is preserved, else {@code false}.
	 *
	 * @return {@code true} if order is preserved, else {@code false}.
	 */
	public boolean preservesOrder() {
	
		return preserveOrder;
	}
	
	
	/**
	 * Sets the strictness of JSON-RPC 2.0 message parsing.
	 *
	 * @param noStrict If {@code true} the {@code "jsonrpc":"2.0"} version 
	 *                 field in parsed JSON-RPC 2.0 messages must be 
	 *                 ignored. 
	 */
	public void noStrict(final boolean noStrict) {
	
		this.noStrict = noStrict;
	}
	
	/**
	 * Gets the strictness of JSON-RPC 2.0 message parsing.
	 *
	 * @return {@code true} if the {@code "jsonrpc":"2.0"} version field in 
	 *         parsed JSON-RPC 2.0 messages is ignored, else {@code false}.
	 */
	public boolean isNoStrict() {
	
		return noStrict;
	}
}
