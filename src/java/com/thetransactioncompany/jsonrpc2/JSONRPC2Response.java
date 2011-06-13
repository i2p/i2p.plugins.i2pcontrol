package com.thetransactioncompany.jsonrpc2;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;


/** 
 * Represents a JSON-RPC 2.0 response.
 *
 * <p>A response is returned to the caller after a JSON-RPC 2.0 request has 
 * been processed (notifications, however, don't produce a response). The
 * response can take two different forms depending on the outcome:
 *
 * <ul>
 *     <li>The request was successful. The corresponding response returns
 *         a JSON object with the following information:
 *         <ul>
 *             <li>{@code result} The result, which can be of any JSON type
 *                 - a number, a boolean value, a string, an array, an object 
 *                 or null.
 *             <li>{@code id} The request identifier which is echoed back back 
 *                 to the caller.
 *             <li>{@code jsonrpc} A string indicating the JSON-RPC protocol 
 *                 version set to "2.0".
 *         </ul>
 *     <li>The request failed. The returned JSON object contains:
 *         <ul>
 *             <li>{@code error} An object with:
 *                 <ul>
 *                     <li>{@code code} An integer indicating the error type.
 *                     <li>{@code message} A brief error messsage.
 *                     <li>{@code data} Optional error data.
 *                 </ul>
 *             <li>{@code id} The request identifier. If it couldn't be 
 *                 determined, e.g. due to a request parse error, the ID is
 *                 set to {@code null}.
 *             <li>{@code jsonrpc} A string indicating the JSON-RPC protocol 
 *                 version set to "2.0".
 *         </ul>
 * </ul>
 *
 * <p>Here is an example JSON-RPC 2.0 response string where the request
 * has succeeded:
 *
 * <pre>
 * {  
 *    "result"  : true,
 *    "id"      : "req-002",
 *    "jsonrpc" : "2.0"  
 * }
 * </pre>
 *
 *
 * <p>And here is an example JSON-RPC 2.0 response string indicating a failure:
 *
 * <pre>
 * {  
 *    "error"   : { "code" : -32601, "message" : "Method not found" },
 *    "id"      : "req-003",
 *    "jsonrpc" : "2.0"
 * }
 * </pre>
 *
 * <p>A response object is obtained either by passing a valid JSON-RPC 2.0
 * response string to the static {@link #parse} method or by invoking the
 * appropriate constructor.
 *
 * <p>Here is how parsing is done:
 * 
 * <pre>
 * String jsonString = "{\"result\":true,\"id\":\"req-002\",\"jsonrpc\":\"2.0\"}";
 * 
 * JSONRPC2Response response = null;
 * 
 * try {
 *         response = JSONRPC2Response.parse(jsonString);
 *
 * } catch (JSONRPC2Exception e) {
 *         // handle exception
 * }
 * </pre>
 *
 * <p>And here is how you can replicate the above example response strings:
 *
 * <pre>
 * // success example
 * JSONRPC2Response resp = new JSONRPC2Response(true, "req-002");
 * System.out.println(resp);
 * 
 * // failure example
 * JSONRPC2Error err = new JSONRPC2Error(-32601, "Method not found");
 * resp = new JSONRPC2Response(err, "req-003");
 * System.out.println(resp);
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
 * @version 1.16 (2010-05-19)
 */
public class JSONRPC2Response extends JSONRPC2Message {
	
	
	/** 
	 * The result. 
	 */
	private Object result = null;
	
	
	/** 
	 * The error object.
	 */
	private JSONRPC2Error error = null;
	
	
	/** 
	 * The request identifier. 
	 */
	private Object id = null;
	
	
	/** 
	 * Parses a JSON-RPC 2.0 response string. This method is thread-safe.
	 *
	 * <p>The member order of parsed JSON objects will not be preserved 
	 * (for efficiency reasons) and the JSON-RPC 2.0 version field must be 
	 * set to "2.0". To change this behaviour check the optional {@link 
	 * #parse(String,boolean,boolean)} method.
	 *
	 * @param jsonString    The JSON-RPC 2.0 response string, UTF-8 encoded.
	 *
	 * @return The corresponding JSON-RPC 2.0 response object.
	 *
	 * @throws JSONRPC2ParseException With detailed message if the parsing 
	 *                                failed.
	 */
	public static JSONRPC2Response parse(final String jsonString)
		throws JSONRPC2ParseException {
	
		return parse(jsonString, false, false);
	}
	
	
	/** 
	 * Parses a JSON-RPC 2.0 response string. This method is thread-safe.
	 *
	 * @param jsonString    The JSON-RPC 2.0 response string, UTF-8 encoded.
	 * @param preserveOrder If {@code true} the member order of JSON objects
	 *                      in results will be preserved.
	 * @param noStrict      If {@code true} the {@code "jsonrpc":"2.0"}
	 *                      version field in the JSON-RPC 2.0 message will 
	 *                      not be checked.
	 *
	 * @return The corresponding JSON-RPC 2.0 response object.
	 *
	 * @throws JSONRPC2ParseException With detailed message if the parsing 
	 *                                failed.
	 */
	public static JSONRPC2Response parse(final String jsonString, final boolean preserveOrder, final boolean noStrict)
		throws JSONRPC2ParseException {
	
		JSONRPC2Parser parser = new JSONRPC2Parser(preserveOrder, noStrict);
		
		return parser.parseJSONRPC2Response(jsonString);
	}
	
	
	/** 
	 * Creates a new JSON-RPC 2.0 response to a successful request.
	 *
	 * @param result The result. The value can <a href="#map">map</a> 
	 *               to any JSON type.
	 * @param id     The request identifier echoed back to the caller. 
	 */
	public JSONRPC2Response(final Object result, final Object id) {
	
		setResult(result);
		setID(id);
	}
	
	
	/** 
	 * Creates a new JSON-RPC 2.0 response to a failed request.
	 * 
	 * @param error A JSON-RPC 2.0 error instance indicating the
	 *              cause of the failure.
	 * @param id    The request identifier echoed back to the caller.
	 *              Pass a {@code null} if the request identifier couldn't
	 *              be determined (e.g. due to a parse error).
	 */
	public JSONRPC2Response(final JSONRPC2Error error, final Object id) {
	
		setError(error);
		setID(id);
	}
	
	
	/** 
	 * Indicates a successful JSON-RPC 2.0 request and sets the result. 
	 * Note that if the response was previously indicating failure this
	 * will turn it into a response indicating success. Any previously set
	 * error data will be invalidated.
	 *
	 * @param result The result. The value can <a href="#map">map</a> to 
	 *               any JSON type.
	 */
	public void setResult(final Object result) {
		
		if (   result != null             &&
		    ! (result instanceof Boolean) &&
		    ! (result instanceof Number ) &&
		    ! (result instanceof String ) &&
		    ! (result instanceof List   ) &&
		    ! (result instanceof Map    )    )
		    	throw new IllegalArgumentException("The result must map to a JSON type");
		
		// result and error are mutually exclusive
		this.result = result;
		this.error = null;
	}	
	
	
	/** 
	 * Gets the result of the request. The returned value has meaning
	 * only if the request was successful. Use the {@link #getError getError}
	 * method to check this.
	 *
	 * @return The result
	 */
	public Object getResult() {
		
		return result;
	}
	
	
	/** 
	 * Indicates a failed JSON-RPC 2.0 request and sets the error details.
	 * Note that if the response was previously indicating success this
	 * will turn it into a response indicating failure. Any previously set 
	 * result data will be invalidated.
	 *
	 * @param error A JSON-RPC 2.0 error instance indicating the
	 *              cause of the failure.
	 */
	public void setError(final JSONRPC2Error error) {
		
		if (error == null)
			throw new NullPointerException("The error object cannot be null");
		
		// result and error are mutually exclusive
		this.error = error;
		this.result = null;		
	}
	
	
	/** 
	 * Gets the error object indicating the cause of the request failure. 
	 * If a {@code null} is returned, the request succeeded and there was
	 * no error.
	 *
	 * @return A JSON-RPC 2.0 error object, {@code null} if the
	 *         response indicates success.
	 */
	public JSONRPC2Error getError() {
		
		return error;
	}
	
	
	/**
	 * A convinience method to check if the response indicates success or
	 * failure of the request. Alternatively, you can use the 
	 * {@code #getError} method for this purpose.
	 *
	 * @return {@code true} if the request succeeded, {@code false} if
	 *         there was an error.
	 */
	public boolean indicatesSuccess() {
		
		if (error == null)
			return true;
		else
			return false;
	}
	
	
	/**
	 * Sets the request identifier echoed back to the caller.
	 *
	 * @param id The value must <a href="#map">map</a> to a JSON scalar. 
	 *           Pass a {@code null} if the request identifier couldn't 
	 *           be determined (e.g. due to a parse error).
	 */
	public void setID(final Object id) {
		
		if (   id != null             &&
		    ! (id instanceof Boolean) &&
		    ! (id instanceof Number ) &&
		    ! (id instanceof String )    )
			throw new IllegalArgumentException("The request identifier must map to a JSON scalar");
		
		this.id = id;
	}
	
	
	/** 
	 * Gets the request identifier that is echoed back to the caller.
	 *
	 * @return The request identifier. If there was an error during the
	 *         the request retrieval (e.g. parse error) and the identifier 
	 *         couldn't be determined, the value will be {@code null}.
	 */
	 public Object getID() {
	 
	 	return id;
	}
	
	
	/** 
	 * Gets a JSON representation of this JSON-RPC 2.0 response.
	 *
	 * @return A JSON object representing the response.
	 */
	public JSONObject toJSON() {
		
		JSONObject out = new JSONObject();
		
		// Result and error are mutually exclusive
		if (error != null) {
			out.put("error", error.toJSON());
		}
		else {
			out.put("result", result);
		}
		
		out.put("id", id);
		
		out.put("jsonrpc", "2.0");
		
		return out;
	}

}
