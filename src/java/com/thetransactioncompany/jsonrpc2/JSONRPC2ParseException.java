package com.thetransactioncompany.jsonrpc2;


/** 
 * Thrown to indicate an exception during the parsing of a JSON-RPC 2.0 
 * message string.
 *
 * <p>The JSON-RPC 2.0 specification and user group forum can be found 
 * <a href="http://groups.google.com/group/json-rpc">here</a>.
 *
 * @author <a href="http://dzhuvinov.com">Vladimir Dzhuvinov</a>
 * @version 1.16 (2010-05-18)
 */
public class JSONRPC2ParseException extends Exception {
	
	
	/**
	 * Indicates a parse exception caused by a JSON message not conforming
	 * to the JSON-RPC 2.0 protocol.
	 */
	public static int PROTOCOL = 0;
	
	
	/**
	 * Indicates a parse exception caused by invalid JSON.
	 */
	public static int JSON = 1;
	
	
	/**
	 * The parse exception cause type. Default is {@link #PROTOCOL}.
	 */
	private int causeType = PROTOCOL;
	
	
	
	/** 
	 * The string that could't be parsed.
	 */
	private String unparsableString = null;
	
	
	/** 
	 * Creates a new parse exception with the specified message. The cause 
	 * type is set to {@link #PROTOCOL}.
	 *
	 * @param message The exception message.
	 */
	public JSONRPC2ParseException(final String message) {
	
		super(message);
	}
	
	
	/**
	 * Creates a new parse exception with the specified message and the 
	 * original string that didn't parse. The cause type is set to
	 * {@link #PROTOCOL}.
	 *
	 * @param message          The exception message.
	 * @param unparsableString The unparsable string.
	 */
	public JSONRPC2ParseException(final String message, final String unparsableString) {
	
		super(message);
		this.unparsableString = unparsableString;
	}
	
	
	/**
	 * Creates a new parse exception with the specified message, cause type 
	 * and the original string that didn't parse.
	 *
	 * @param message          The exception message.
	 * @param causeType        The exception cause type, either 
	 *                         {@link #PROTOCOL} or {@link #JSON}.
	 * @param unparsableString The unparsable string.
	 */
	public JSONRPC2ParseException(final String message, final int causeType, final String unparsableString) {
	
		super(message);
		
		if (causeType != PROTOCOL && causeType != JSON)
			throw new IllegalArgumentException("Cause type must be either PROTOCOL or JSON");
		
		this.causeType = causeType;
		this.unparsableString = unparsableString;
	}
	
	
	/**
	 * Gets the parse exception cause type.
	 *
	 * @return The cause type, either {@link #PROTOCOL} or {@link #JSON}.
	 */
	public int getCauseType() {
	
		return causeType;
	}
	
	
	/**
	 * Gets original string that caused the parse exception (if specified).
	 *
	 * @return The string that didn't parse, {@code null} if none.
	 */
	public String getUnparsableString() {
	
		return unparsableString;
	}
}
