package com.thetransactioncompany.jsonrpc2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;


/** 
 * Represents a JSON-RPC 2.0 request. 
 *
 * <p>A request carries four pieces of data:
 * <ul>
 *     <li>{@code method} The name of the remote method to call.
 *     <li>{@code params} The required method parameters (if any), which can 
 *         be packed into a JSON array or object.
 *     <li>{@code id} An identifier which is echoed back to the client with 
 *         the response.
 *     <li>{@code jsonrpc} A string indicating the JSON-RPC protocol version 
 *         set to "2.0".
 * </ul>
 *
 * <p>Here is a sample JSON-RPC 2.0 request string:
 *
 * <pre>
 * {  
 *    "method"  : "makePayment",
 *    "params"  : { "recipient" : "Penny Adams", "amount":175.05 },
 *    "id"      : "0001",
 *    "jsonrpc" : "2.0"
 * }
 * </pre>
 *
 * <p>This class provides two methods to obtain a request object:
 * <ul>
 *     <li>Pass a JSON-RPC 2.0 request string to the static 
 *         {@link #parse} method, or 
 *     <li>Invoke one of the constructors with the appropriate arguments.
 * </ul>
 *
 * <p>Example 1: Parsing a request string:
 *
 * <pre>
 * String jsonString = "{\"method\":\"makePayment\"," +
 *                     "\"params\":{\"recipient\":\"Penny Adams\",\"amount\":175.05}," +
 *                     "\"id\":\"0001\","+
 *                     "\"jsonrpc\":\"2.0\"}";
 * 
 * JSONRPC2Request req = null;
 * 
 * try {
 *         req = JSONRPC2Request.parse(jsonString);
 *
 * } catch (JSONRPC2ParseException e) {
 *         // handle exception
 * }
 * </pre>
 *
 * <p>Example 2: Recreating the above request:
 * 
 * <pre>
 * String method = "makePayment";
 * Map params = new HashMap();
 * params.put("recipient", "Penny Adams");
 * params.put("amount", 175.05);
 * String id = "0001";
 *
 * JSONRPC2Request req = new JSONRPC2Request(method, params, id);
 *
 * System.out.println(req);
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
public class JSONRPC2Request extends JSONRPC2Message {

	
	/** 
	 * The requested method name. 
	 */
	private String method;
	
	
	/** 
	 * The request parameters. 
	 */
	private Object params;
	
	
	/** 
	 * The parameters type constant. 
	 */
	private JSONRPC2ParamsType paramsType;
	
	
	/** 
	 * The request identifier. 
	 */
	private Object id;
	
	
	/** 
	 * Parses a JSON-RPC 2.0 request string. This method is thread-safe.
	 *
	 * <p>The member order of parsed JSON objects will not be preserved 
	 * (for efficiency reasons) and the JSON-RPC 2.0 version field must be 
	 * set to "2.0". To change this behaviour check the optional {@link 
	 * #parse(String,boolean,boolean)} method.
	 *
	 * @param jsonString The JSON-RPC 2.0 request string, UTF-8 encoded.
	 *
	 * @return The corresponding JSON-RPC 2.0 request object.
	 *
	 * @throws JSONRPC2ParseException With detailed message if the parsing 
	 *                                failed.
	 */
	public static JSONRPC2Request parse(final String jsonString)
		throws JSONRPC2ParseException {
		
		return parse(jsonString, false, false);
	}
	
	
	/** 
	 * Parses a JSON-RPC 2.0 request string. This method is thread-safe.
	 *
	 * @param jsonString    The JSON-RPC 2.0 request string, UTF-8 encoded.
	 * @param preserveOrder If {@code true} the member order of JSON objects
	 *                      in parameters will be preserved.
	 * @param noStrict      If {@code true} the {@code "jsonrpc":"2.0"}
	 *                      version field in the JSON-RPC 2.0 message will 
	 *                      not be checked.
	 *
	 * @return The corresponding JSON-RPC 2.0 request object.
	 *
	 * @throws JSONRPC2ParseException With detailed message if the parsing 
	 *                                failed.
	 */
	public static JSONRPC2Request parse(final String jsonString, final boolean preserveOrder, final boolean noStrict)
		throws JSONRPC2ParseException {
		
		JSONRPC2Parser parser = new JSONRPC2Parser(preserveOrder, noStrict);
		
		return parser.parseJSONRPC2Request(jsonString);
	}
	
	
	/** 
	 * Constructs a new JSON-RPC 2.0 request with no parameters.
	 *
	 * @param method The name of the requested method.
	 * @param id     The request identifier echoed back to the caller. 
	 *               The value must <a href="#map">map</a> to a JSON 
	 *               scalar ({@code null} and fractions, however, should
	 *               be avoided).
	 */
	public JSONRPC2Request(final String method, final Object id) {
		
		setMethod(method);
		setParams(null);
		setID(id);
	}
	
	
	/** 
	 * Constructs a new JSON-RPC 2.0 request with JSON array parameters.
	 *
	 * @param method The name of the requested method.
	 * @param params The request parameters packed as a JSON array
	 *               (<a href="#map">maps</a> to java.util.List).
	 * @param id     The request identifier echoed back to the caller. 
	 *               The value must <a href="#map">map</a> to a JSON 
	 *               scalar ({@code null} and fractions, however, should
	 *               be avoided).
	 */
	public JSONRPC2Request(final String method, final List params, final Object id) {
		
		setMethod(method);
		setParams(params);
		setID(id);
	}
		
	
	/** 
	 * Constructs a new JSON-RPC 2.0 request with JSON object parameters.
	 *
	 * @param method The name of the requested method.
	 * @param params The request parameters packed as a JSON object
	 *               (<a href="#map">maps</a> to java.util.Map).
	 * @param id     The request identifier echoed back to the caller. 
	 *               The value must <a href="#map">map</a> to a JSON 
	 *               scalar ({@code null} and fractions, however, should
	 *               be avoided).
	 */
	public JSONRPC2Request(final String method, final Map params, final Object id) {
		
		setMethod(method);
		setParams(params);
		setID(id);
	}
	
	
	/** 
	 * Gets the name of the requested method.
	 *
	 * @return The method name.
	 */
	public String getMethod() {
		
		return method;
	}
	
	
	/**
	 * Sets the name of the requested method.
	 *
	 * @param method The method name.
	 */
	public void setMethod(final String method) {
		
		// The method name is mandatory
		if (method == null)
			throw new NullPointerException();

		this.method = method;
	}
	
	
	/** 
	 * Gets the parameters type ({@link JSONRPC2ParamsType#ARRAY}, 
	 * {@link JSONRPC2ParamsType#OBJECT} or 
	 * {@link JSONRPC2ParamsType#NO_PARAMS}).
	 *
	 * @return The parameters type.
	 */
	public JSONRPC2ParamsType getParamsType() {
	
		return paramsType;
	}
	
	
	/** 
	 * Gets the request parameters.
	 *
	 * @return The parameters as {@code List} if JSON array, {@code Map} 
	 *         if JSON object, or {@code null} if none.
	 */
	public Object getParams() {
		
		return params;
	}
	
	
	/**
	 * Sets the request parameters.
	 *
	 * @param params The parameters. For a JSON array type pass a 
	 *               {@code List}. For a JSON object pass a {@code Map}. 
	 *               If there are no parameters pass {@code null}.
	 */
	public void setParams(final Object params) {
	
		if (params == null)
			paramsType = JSONRPC2ParamsType.NO_PARAMS;
			
		else if (params instanceof List)
			paramsType = JSONRPC2ParamsType.ARRAY;
			
		else if (params instanceof Map)
			paramsType = JSONRPC2ParamsType.OBJECT;
			
		else
			throw new IllegalArgumentException("The request parameters must be of type List, Map or null");
			
		this.params = params;
	}
	
	
	/** 
	 * Gets the request identifier.
	 *
	 * @return The request identifier ({@code Number}, {@code Boolean},
	 *         {@code String}) or {@code null}.
	 */
	public Object getID() {
		
		return id;
	}
	
	
	/**
	 * Sets the request identifier (ID).
	 *
	 * @param id The request identifier echoed back to the caller. 
	 *           The value must <a href="#map">map</a> to a JSON 
	 *           scalar ({@code null} and fractions, however, should
	 *           be avoided).
	 */
	public void setID(final Object id) {
		
		if (   id != null             &&
		    ! (id instanceof Boolean) &&
		    ! (id instanceof Number ) &&
		    ! (id instanceof String )     )
			throw new IllegalArgumentException("The request identifier must map to a JSON scalar");
		this.id = id;
	}
	
	
	/** 
	 * Gets a JSON representation of this JSON-RPC 2.0 request.
	 *
	 * @return A JSON object representing the request.
	 */
	public JSONObject toJSON() {
	
		JSONObject req = new JSONObject();
		
		req.put("method", method);
		
		// the params can be omitted if empty
		if (params != null && paramsType != JSONRPC2ParamsType.NO_PARAMS)
			req.put("params", params);
		
		req.put("id", id);
		
		req.put("jsonrpc", "2.0");
		
		return req;
	}
}
