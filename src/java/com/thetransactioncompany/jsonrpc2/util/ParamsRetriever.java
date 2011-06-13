package com.thetransactioncompany.jsonrpc2.util;


import java.lang.reflect.*;

import com.thetransactioncompany.jsonrpc2.*;


/**
 * The base abstract class for the JSON-RPC 2.0 parameter retrievers.
 *
 * @author <a href="http://dzhuvinov.com">Vladimir Dzhuvinov</a>
 * @version 1.16 (2010-08-07)
 */
public abstract class ParamsRetriever {

	/**
	 * Returns the parameter count.
	 *
	 * @return The number of parameters.
	 */
	public abstract int size();


	/**
	 * Throws a {@code JSONRPC2Error.INVALID_PARAMS} exception if the input
	 * string doesn't match a value in the specified string array.
	 *
	 * <p>This method is intended to check a string against a set of
	 * acceptable values.
	 *
	 * @param input       The string to check.
	 * @param enumStrings The acceptable string values.
	 * @param ignoreCase  {@code true} for a case insensitive match.
	 *
	 * @return The matching string value.
	 *
	 * @throws JSONRPC2Error With proper code and message if the input
	 *                       string didn't match.
	 */
	protected static String ensureEnumString(final String input, final String[] enumStrings, final boolean ignoreCase)
		throws JSONRPC2Error {
	
		for (String en: enumStrings) {
		
			if (ignoreCase) {
				if (en.toLowerCase().equals(input.toLowerCase()))
					return en;
			}
			else {
				if (en.equals(input))
					return en;
			}
		}
		
		// No match -> raise error
		throw JSONRPC2Error.INVALID_PARAMS;
	}
	
	
	/**
	 * Throws a {@code JSONRPC2Error.INVALID_PARAMS} exception if the input
	 * string doesn't match a constant name in the specified enumeration
	 * class.
	 *
	 * <p>This method is intended to check a string against a set of
	 * acceptable values.
	 *
	 * @param input      The string to check.
	 * @param enumClass  The enumeration class specifying the acceptable 
	 *                   constant names.
	 * @param ignoreCase {@code true} for a case insensitive match.
	 *
	 * @return The matching enumeration constant.
	 *
	 * @throws JSONRPC2Error With proper code and message if the input
	 *                       string didn't match.
	 */
	protected static <T extends Enum<T>> T ensureEnumString(final String input, final Class<T> enumClass, final boolean ignoreCase)
		throws JSONRPC2Error {
		
		for (T en: enumClass.getEnumConstants()) {
		
			if (ignoreCase) {
				if (en.toString().toLowerCase().equals(input.toLowerCase()))
					return en;
			}
			else {
				if (en.toString().equals(input))
					return en;
			}
		
		}
		
		// No match -> raise error
		throw JSONRPC2Error.INVALID_PARAMS;
	}

}
