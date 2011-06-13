package com.thetransactioncompany.jsonrpc2;


import org.json.simple.JSONObject;


/**
 * The base abstract class for JSON-RPC 2.0 requests, notifications and
 * responses. Provides common methods for parsing (from JSON string) and
 * serialisation (to JSON string) of the three message types.
 *
 * <p>Example showing parsing and serialisation back to JSON:
 *
 * <pre>
 * String jsonString = "{\"method\":\"progressNotify\",\"params\":[\"75%\"],\"jsonrpc\":\"2.0\"}";
 *
 * JSONRPC2Message message = null;
 *
 * // parse
 * try {
 *        message = JSONRPC2Message.parse(jsonString);
 * } catch (JSONRPC2ParseException e) {
 *        // handle parse exception
 * }
 *
 * if (message instanceof JSONRPC2Request)
 *        System.out.println("The message is a request");
 * else if (message instanceof JSONRPC2Notification)
 *        System.out.println("The message is a notification");
 * else if (message instanceof JSONRPC2Response)
 *        System.out.println("The message is a response");
 *
 * // serialise back to JSON string
 * System.out.println(message);
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
public abstract class JSONRPC2Message {


	/** 
	 * Provides common parsing of JSON-RPC 2.0 requests, notifications 
	 * and responses. Use this method if you don't know which type of 
	 * JSON-RPC message the input string represents.
	 *
	 * <p>This method is thread-safe. Batched requests / notifications 
	 * are not supported.
	 *
	 * <p>If you are certain about the message type use the dedicated 
	 * {@link JSONRPC2Request#parse}, {@link JSONRPC2Notification#parse} 
	 * and {@link JSONRPC2Response#parse} methods. They are more efficient 
	 * and would provide you with more detailed parse error reporting.
	 *
	 * <p>The member order of parsed JSON objects will not be preserved 
	 * (for efficiency reasons) and the JSON-RPC 2.0 version field must be 
	 * set to "2.0". To change this behaviour check the optional {@link 
	 * #parse(String,boolean,boolean)} method.
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
	public static JSONRPC2Message parse(final String jsonString)
		throws JSONRPC2ParseException {

		return parse(jsonString, false, false);
	}
	
	
	/** 
	 * Provides common parsing of JSON-RPC 2.0 requests, notifications 
	 * and responses. Use this method if you don't know which type of 
	 * JSON-RPC message the input string represents.
	 *
	 * <p>This method is thread-safe. Batched requests / notifications 
	 * are not supported.
	 *
	 * <p>If you are certain about the message type use the dedicated 
	 * {@link JSONRPC2Request#parse}, {@link JSONRPC2Notification#parse} 
	 * and {@link JSONRPC2Response#parse} methods. They are more efficient 
	 * and would provide you with more detailed parse error reporting.
	 *
	 * @param jsonString    A JSON string representing a JSON-RPC 2.0 
	 *                      request, notification or response, UTF-8
	 *                      encoded.
	 * @param preserveOrder If {@code true} the member order of JSON objects
	 *                      in parameters and results will be preserved.
	 * @param noStrict      If {@code true} the {@code "jsonrpc":"2.0"}
	 *                      version field in the JSON-RPC 2.0 message will 
	 *                      not be checked.
	 *
	 * @return An instance of {@link JSONRPC2Request}, 
	 *         {@link JSONRPC2Notification} or {@link JSONRPC2Response}.
	 *
	 * @throws JSONRPC2ParseException With detailed message if the parsing 
	 *                                failed.
	 */
	public static JSONRPC2Message parse(final String jsonString, final boolean preserveOrder, final boolean noStrict)
		throws JSONRPC2ParseException {
		
		JSONRPC2Parser parser = new JSONRPC2Parser(preserveOrder, noStrict);
		
		return parser.parseJSONRPC2Message(jsonString);
	}

	
	/** 
	 * Gets a JSON object representing this message.
	 *
	 * @return A JSON object.
	 */
	public abstract JSONObject toJSON();
	
	
	/** 
	 * Serialises this message to a JSON string.
	 *
	 * @return A JSON-RPC 2.0 encoded string.
	 */
	public String toString() {
		
		return toJSON().toString();
	}
}
