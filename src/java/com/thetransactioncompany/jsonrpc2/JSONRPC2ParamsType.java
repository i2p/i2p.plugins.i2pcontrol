package com.thetransactioncompany.jsonrpc2;


/**
 * Defines the three parameter types constants for JSON-RPC 2.0 requests and 
 * notifications.
 *
 * <ul>
 *     <li>{@link #NO_PARAMS} The method takes no parameters</li>
 *     <li>{@link #ARRAY} The method parameters are packed as a JSON array
 *         e.g. {@code ["val1", "val2", ...]}</li>
 *     <li>{@link #OBJECT} The method parameters are packed as a JSON object
 *         e.g. {@code {"param1":"val1", "param2":"val2", ...}}</li>
 * </ul>
 *
 * <p>The JSON-RPC 2.0 specification and user group forum can be found 
 * <a href="http://groups.google.com/group/json-rpc">here</a>.
 * 
 * @author <a href="http://dzhuvinov.com">Vladimir Dzhuvinov</a>
 * @version 1.16 (2010-08-07)
 */
public final class JSONRPC2ParamsType {

	/** The integer value for this parameters type. */
	private final int intValue;
	
	
	/** The name to use for this parameters type. */
	private final String name;
	
	
	/**
	 * The integer constant for the "NO_PARAMS" parameters type.
	 */
	public static final int NO_PARAMS_CONST = 0;
	
	
	/**
	 * No parameters.
	 */
	public static final JSONRPC2ParamsType NO_PARAMS =
		new JSONRPC2ParamsType("NO_PARAMS", NO_PARAMS_CONST);
	
	
	/**
	 * The integer constant for the "ARRAY" parameters type.
	 */
	public static final int ARRAY_CONST = 1;
	
	
	/**
	 * The parameters are packed as a JSON array.
	 */
	public static final JSONRPC2ParamsType ARRAY =
		new JSONRPC2ParamsType("ARRAY", ARRAY_CONST);
	
	
	/**
	 * The integer constant for the "OBJECT" parameters type.
	 */
	public static final int OBJECT_CONST = 2;
	
	
	/**
	 * The parameters are packed as a JSON object.
	 */
	public static final JSONRPC2ParamsType OBJECT =
		new JSONRPC2ParamsType("OBJECT", OBJECT_CONST);
	
	
	/**
	 * Creates a new parameter type with the specified name and integer
	 * value.
	 *
	 * @param name     The name to use for this parameter type.
	 * @param intValue The integer value to use for this parameter type.
	 */
	private JSONRPC2ParamsType(final String name, final int intValue) {
		
		this.name = name;
		this.intValue = intValue;
	}
	
	
	/**
	 * Retrieves the name for this parameters type.
	 *
	 * @return The parameters type name.
	 */
	public String getName() {
	
		return name;
	}
	
	
	/**
	 * Retrieves the integer constant for this parameters type.
	 *
	 * @return The parameters integer constant.
	 */
	public int intValue() {
	
		return intValue;
	}
	
	
	/**
	 * Retrieves the parameters type with the specified integer constant.
	 *
	 * @param intValue The integer constant for which to retrieve the
	 *                 corresponding parameters type.
	 *
	 * @return The parameters type or {@code null} if none matches.
	 */
	public static JSONRPC2ParamsType valueOf(final int intValue) {
	
		switch (intValue) {
		
			case 0:
				return NO_PARAMS;
			case 1:
				return ARRAY;
			case 2:
				return OBJECT;
			default:
				return null;
		}
		
	}
	
	
	/**
	 * Indicates wheter the provided object is equal to this parameters type.
	 * 
	 * @param o The object for which to make the comparison.
	 *
	 * @return {@code true} if the objects are equal, or {@code false} if 
	 *         not.
	 */
	public boolean equals(final Object o) {
	
		if (o == null)
			return false;
		
		else if (o == this)
			return true;
		
		else if (o instanceof JSONRPC2ParamsType)
			return (intValue == ((JSONRPC2ParamsType) o).intValue);
		
		else
			return false;
	}
	
	
	/**
	 * Retrieves a string representing this parameters type.
	 *
	 * @return A string representing this parameters type.
	 */
	public String toString() {
	
		return name;
	}
	
}
