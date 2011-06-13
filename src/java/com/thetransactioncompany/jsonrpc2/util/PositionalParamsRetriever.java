package com.thetransactioncompany.jsonrpc2.util;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.thetransactioncompany.jsonrpc2.*;


/**
 * Utility class for retrieving JSON-RPC 2.0 positional parameters (packed into
 * a JSON Array). 
 *
 * <p>Provides a set of getter methods according to the expected parameter type
 * (number, string, etc.) and whether the parameter is mandatory or optional:
 *
 * <ul>
 *     <li>{@code getXXX(param_pos)} for mandatory parameters, where {@code XXX}
 *         is the expected parameter type.</li>
 *     <li>{@code getOptXXX(param_pos, default_value)} for optional parameters,
 *         specifying a default value.</li>
 * </ul>
 *
 * <p>There are also generic getter methods that let you do the type conversion 
 * yourself.
 *
 * <p>If a parameter cannot be retrieved, e.g. due to a missing mandatory 
 * parameter or bad type, a {@link com.thetransactioncompany.jsonrpc2.JSONRPC2Error#INVALID_PARAMS}
 * exception is thrown.
 *
 * <p>Example: suppose you have a method with 3 positional parameters where the
 * first two are mandatory and the last is optional and has a default value of
 * {@code true}.
 *
 * <pre>
 * // Parse received request string
 * JSONRPC2Request request = null;
 *
 * try {
 *         request = JSONRPC2Request.parse(jsonString);
 * } catch (JSONRPC2ParseException e) {
 *         // handle exception...
 * }
 *
 * // Create a new retriever for positional parameters
 * List params = (List)request.getParams();
 * PositionalParamsRetriever r = new PositionalParamsRetriever(params);
 *
 * try {
 *         // Extract first mandatory string parameter
 *         String param1 = r.getString(0);
 *
 *         // Extract second integer parameter
 *         int param2 = r.getInt(1);
 *
 *         // Extract third optional boolean parameter which defaults to true
 *         boolean param3 = r.getOptBoolean(2, true);
 *
 * } catch (JSONRPC2Error e) {
 *         // A JSONRPC2Error.INVALID_PARAMS will be thrown to indicate
 *         // an unexpected parameter type or a missing mandatory parameter.
 *         // You can use it straight away to create the appropriate
 *         // JSON-RPC 2.0 error response.
 *         JSONRPC2Response response = new JSONRPC2Response(e, null);
 * }
 * 
 * </pre>
 *
 * @author <a href="http://dzhuvinov.com">Vladimir Dzhuvinov</a>
 * @version 1.16 (2010-09-07)
 */
public class PositionalParamsRetriever
	extends ParamsRetriever {

	
	/** The positional params interface. */
	private List params = null;
	
	
	/**
	 * Creates a new positional parameters retriever from the specified 
	 * value list.
	 *
	 * @param params The positional parameters list.
	 */
	public PositionalParamsRetriever(final List params) {
	
		this.params = params;
	}
	
	
	/**
	 * Returns the number of available positional parameters.
	 *
	 * @return The number of positional parameters.
	 */
	public int size() {
	
		return params.size();
	}
	
	
	/** 
	 * Returns {@code true} a parameter at the specified position exists, 
	 * otherwise {@code false}.
	 *
	 * @param position The parameter position.
	 *
	 * @return {@code true} if the parameter exists, otherwise 
	 *         {@code false}.
	 */
	public boolean hasParameter(final int position) {
	
		
		if (position >= params.size())
			return false;
		else
			return true;
	}
	
	
	/**
	 * Throws a {@code JSONRPC2Error.INVALID_PARAMS} exception if there is
	 * no parameter at the specified position.
	 *
	 * <p>You may use this method to fire the proper JSON-RPC 2.0 error
	 * on a missing mandatory parameter.
	 *
	 * @param position The parameter position, starting with zero for the 
	 *                 first.
	 *
	 * @throws JSONRPC2Error On a missing parameter
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public void ensureParameter(final int position)
		throws JSONRPC2Error {
		
		if (position >= params.size() )
			throw JSONRPC2Error.INVALID_PARAMS;
	}
	
	
	/**
	 * Throws a {@code JSONRPC2Error.INVALID_PARAMS} exception if there is
	 * no parameter at the specified position, its value is {@code null}, 
	 * or its type doesn't map to the specified.
	 *
	 * <p>You may use this method to fire the proper JSON-RPC 2.0 error
	 * on a missing or badly-typed mandatory parameter.
	 *
	 * @param position The parameter position.
	 * @param clazz    The corresponding Java class that the parameter 
	 *                 should map to (any one of the return types of the 
	 *                 {@code getXXX()} getter methods. Set to 
	 *                 {@code Object.class} to allow any type.
	 *
	 * @throws JSONRPC2Error On a missing parameter, {@code null} value or 
	 *                       bad type ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public void ensureParameter(final int position, final Class clazz)
		throws JSONRPC2Error {
		
		ensureParameter(position, clazz, false);
	}
	
	
	/**
	 * Throws a {@code JSONRPC2Error.INVALID_PARAMS} exception if there is
	 * no parameter at the specified position or its type doesn't map to the
	 * specified.
	 *
	 * <p>You may use this method to fire the proper JSON-RPC 2.0 error
	 * on a missing or badly-typed mandatory parameter.
	 *
	 * @param position  The parameter position.
	 * @param clazz     The corresponding Java class that the parameter 
	 *                  should map to (any one of the return types of the 
	 *                  {@code getXXX()} getter methods. Set to 
	 *                  {@code Object.class} to allow any type.
	 * @param allowNull If {@code true} allows a {@code null} parameter
	 *                  value.
	 *
	 * @throws JSONRPC2Error On a missing parameter or bad type 
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public void ensureParameter(final int position, final Class clazz, final boolean allowNull)
		throws JSONRPC2Error {
		
		// First, check existence only
		ensureParameter(position);
		
		// Now check type
		Object value = params.get(position);
		
		if (value == null) {
			
			if (allowNull)
				return; // ok
		
			else
				throw JSONRPC2Error.INVALID_PARAMS;
		}
		
		if (! clazz.isAssignableFrom(value.getClass()))
			throw JSONRPC2Error.INVALID_PARAMS;
	}
	
	
		/**
	 * Retrieves the specified parameter which can be of any type. Use this 
	 * generic getter if you want to cast the value yourself. Otherwise 
	 * look at the typed {@code get*} methods.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value.
	 *
	 * @throws JSONRPC2Error On a missing parameter
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Object get(final int position)
		throws JSONRPC2Error {
		
		ensureParameter(position);
		
		return params.get(position);
	}
	
	
	/**
	 * Retrieves the specified parameter which must map to the provided
	 * class (use the appropriate wrapper class for primitive types).
	 *
	 * @param position The parameter position.
	 * @param clazz    The corresponding Java class that the parameter 
	 *                 should map to (any one of the return types of the 
	 *                 {@code getXXX()} getter methods. Set to
	 *                 {@code Object.class} to allow any type.
	 *
	 * @return The parameter value.
	 *
	 * @throws JSONRPC2Error On a missing parameter, {@code null} value or 
	 *                       bad type ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Object get(final int position, final Class clazz)
		throws JSONRPC2Error {
	
		return get(position, clazz, false);
	}
	
	
	/**
	 * Retrieves the specified parameter which must map to the provided
	 * class (use the appropriate wrapper class for primitive types).
	 *
	 * @param position  The parameter position.
	 * @param clazz     The corresponding Java class that the parameter 
	 *                  should map to (any one of the return types of the 
	 *                  {@code getXXX()} getter methods. Set to
	 *                  {@code Object.class} to allow any type.
	 * @param allowNull If {@code true} allows a {@code null} parameter
	 *                  value.
	 *
	 * @return The parameter value.
	 *
	 * @throws JSONRPC2Error On a missing parameter or bad type 
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Object get(final int position, final Class clazz, final boolean allowNull)
		throws JSONRPC2Error {
	
		ensureParameter(position, clazz, allowNull);
		
		return params.get(position);
	}
	
	
	/**
	 * Retrieves the specified optional parameter which must map to the
	 * provided class (use the appropriate wrapper class for primitive 
	 * types). If the parameter doesn't exist the method returns the 
	 * specified default value.
	 *
	 * @param position     The parameter position.
	 * @param clazz        The corresponding Java class that the parameter 
	 *                     should map to (any one of the return types of the 
	 *                     {@code getXXX()} getter methods. Set to 
	 *                     {@code Object.class} to allow any type.
	 * @param defaultValue The default return value if the parameter
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Object getOpt(final int position, final Class clazz, final Object defaultValue)
		throws JSONRPC2Error {
	
		return getOpt(position, clazz, false, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified optional parameter which must map to the
	 * provided class (use the appropriate wrapper class for primitive 
	 * types). If the parameter doesn't exist the method returns the 
	 * specified default value.
	 *
	 * @param position     The parameter position.
	 * @param clazz        The corresponding Java class that the parameter 
	 *                     should map to (any one of the return types of the 
	 *                     {@code getXXX()} getter methods. Set to 
	 *                     {@code Object.class} to allow any type.
	 * @param allowNull    If {@code true} allows a {@code null} parameter
	 *                     value.
	 * @param defaultValue The default return value if the parameter
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value.
	 *
	 * @throws JSONRPC2Error On a bad parameter type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Object getOpt(final int position, final Class clazz, final boolean allowNull, final Object defaultValue)
		throws JSONRPC2Error {
	
		if (! hasParameter(position))
			return defaultValue;
		
		ensureParameter(position, clazz, allowNull);
		
		return params.get(position);
	}
	
	
	/**
	 * Retrieves the specified string parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or {@code null}
	 *                       value ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getString(final int position)
		throws JSONRPC2Error {
		
		return getString(position, false);
	}
	
	
	/**
	 * Retrieves the specified string parameter.
	 *
	 * @param position  The parameter position.
	 * @param allowNull If {@code true} allows a {@code null} value.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a missing parameter or bad type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getString(final int position, final boolean allowNull)
		throws JSONRPC2Error {
		
		return (String)get(position, String.class, allowNull);
	}
	
	
	/**
	 * Retrieves the specified optional string parameter. If it doesn't 
	 * exist the method will return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a bad type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getOptString(final int position, final String defaultValue)
		throws JSONRPC2Error {
	
		return getOptString(position, false, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified optional string parameter. If it doesn't 
	 * exist the method will return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param allowNull    If {@code true} allows a {@code null} value.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a bad type ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getOptString(final int position, final boolean allowNull, final String defaultValue)
		throws JSONRPC2Error {
	
		return (String)getOpt(position, String.class, allowNull, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified enumerated string parameter.
	 *
	 * @param position    The parameter position.
	 * @param enumStrings The acceptable string values.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getEnumString(final int position, final String[] enumStrings)
		throws JSONRPC2Error {
		
		return getEnumString(position, enumStrings, false); 
	}
	
	
	/**
	 * Retrieves the specified enumerated string parameter, allowing for a
	 * case insenstive match.
	 *
	 * @param position    The parameter position.
	 * @param enumStrings The acceptable string values.
	 * @param ignoreCase  {@code true} for a case insensitive match.
	 *
	 * @return The matching parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getEnumString(final int position, final String[] enumStrings, final boolean ignoreCase)
		throws JSONRPC2Error {
		
		String value = (String)get(position, String.class);
		
		return ensureEnumString(value, enumStrings, ignoreCase);
	}
	
	
	/**
	 * Retrieves the specified optional enumerated string parameter.
	 *
	 * @param position     The parameter position.
	 * @param enumStrings  The acceptable string values.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a bad type or bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getOptEnumString(final int position, final String[] enumStrings, final String defaultValue)
		throws JSONRPC2Error {
		
		return getOptEnumString(position, enumStrings, defaultValue, false); 
	}
	
	
	/**
	 * Retrieves the specified optional enumerated string parameter, 
	 * allowing for a case insenstive match. If it doesn't exist the method 
	 * will return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param enumStrings  The acceptable string values.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 * @param ignoreCase   {@code true} for a case insensitive match.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a bad type or bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getOptEnumString(final int position, final String[] enumStrings, final String defaultValue, final boolean ignoreCase)
		throws JSONRPC2Error {
		
		String value = (String)getOpt(position, String.class, defaultValue);
		
		return ensureEnumString(value, enumStrings, ignoreCase);
	}
	
	
	/**
	 * Retrieves the specified enumerated parameter (from a JSON string that
	 * has a predefined set of possible values).
	 *
	 * @param position  The parameter position.
	 * @param enumClass An enumeration type with constant names representing
	 *                  the acceptable string values.
	 *
	 * @return The matching enumeration constant.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public <T extends Enum<T>> T getEnum(final int position, final Class<T> enumClass)
		throws JSONRPC2Error {
		
		return getEnum(position, enumClass, false); 
	}
	
	
	/**
	 * Retrieves the specified enumerated parameter (from a JSON string that
	 * has a predefined set of possible values), allowing for a case 
	 * insensitive match.
	 *
	 * @param position   The parameter position.
	 * @param enumClass  An enumeration type with constant names representing
	 *                   the acceptable string values.
	 * @param ignoreCase If {@code true} a case insensitive match against
	 *                   the acceptable constant names is performed.
	 *
	 * @return The matching enumeration constant.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public <T extends Enum<T>> T getEnum(final int position, final Class<T> enumClass, final boolean ignoreCase)
		throws JSONRPC2Error {
		
		String value = (String)get(position, String.class);
		
		return ensureEnumString(value, enumClass, ignoreCase);
	}
	
	
	/**
	 * Retrieves the specified optional enumerated parameter (from a JSON
	 * string that has a predefined set of possible values).
	 *
	 * @param position     The parameter position.
	 * @param enumClass    An enumeration type with constant names representing
	 *                     the acceptable string values.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The matching enumeration constant.
	 *
	 * @throws JSONRPC2Error On a bad type or bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public <T extends Enum<T>> T getOptEnum(final int position, final Class<T> enumClass, final String defaultValue)
		throws JSONRPC2Error {
		
		return getOptEnum(position, enumClass, defaultValue, false); 
	}
	
	
	/**
	 * Retrieves the specified optional enumerated parameter (from a JSON
	 * string that has a predefined set of possible values), allowing for a 
	 * case insenstive match. If it doesn't exist the method will return 
	 * the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param enumClass    An enumeration type with constant names representing
	 *                     the acceptable string values.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 * @param ignoreCase   If {@code true} a case insensitive match against
	 *                     the acceptable constant names is performed.
	 *
	 * @return The matching enumeration constant.
	 *
	 * @throws JSONRPC2Error On a bad type or bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public <T extends Enum<T>> T getOptEnum(final int position, final Class<T> enumClass, final String defaultValue, final boolean ignoreCase)
		throws JSONRPC2Error {
		
		String value = (String)getOpt(position, String.class, defaultValue);
		
		return ensureEnumString(value, enumClass, ignoreCase);
	}
	
	
	/**
	 * Retrieves the specified boolean (maps from JSON true/false)
	 * parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a boolean.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public boolean getBoolean(final int position)
		throws JSONRPC2Error {
	
		return (Boolean)get(position, Boolean.class);
	}
	
	
	/**
	 * Retrieves the specified optional boolean (maps from JSON true/false)
	 * parameter. If it doesn't exist the method will return the specified 
	 * default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a boolean.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public boolean getOptBoolean(final int position, final boolean defaultValue)
		throws JSONRPC2Error {
	
		return (Boolean)getOpt(position, Boolean.class, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified integer parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as an integer.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public int getInt(final int position)
		throws JSONRPC2Error {
		
		Number number = (Number)get(position, Long.class);
		return number.intValue();
	}
	
	
	/**
	 * Retrieves the specified optional integer parameter. If it doesn't 
	 * exist the method will return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a boolean.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public int getOptInt(final int position, final int defaultValue)
		throws JSONRPC2Error {
	
		Number number = (Number)getOpt(position, Long.class, new Long(defaultValue));
		return number.intValue();
	}
	
	
	/**
	 * Retrieves the specified long parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a long.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public long getLong(final int position)
		throws JSONRPC2Error {
	
		Number number = (Number)get(position, Long.class);
		return number.longValue();
	}
	
	
	/**
	 * Retrieves the specified optional long parameter. If not defined
	 * the method will return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a long.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public long getOptLong(final int position, final long defaultValue)
		throws JSONRPC2Error {
	
		Number number = (Number)getOpt(position, Long.class, defaultValue);
		return number.longValue();
	}
	
	
	/**
	 * Retrieves the specified float parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a float.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public float getFloat(final int position)
		throws JSONRPC2Error {
	
		Number number = (Number)get(position, Double.class);
		return number.floatValue();
	}
	
	
	/**
	 * Retrieves the specified optional float parameter. If it doesn't 
	 * exist the method will return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a float.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public float getOptFloat(final int position, final float defaultValue)
		throws JSONRPC2Error {
	
		Number number = (Number)getOpt(position, Double.class, new Double(defaultValue));
		return number.floatValue();
	}
	
	
	/**
	 * Retrieves the specified double parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a double.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public double getDouble(final int position)
		throws JSONRPC2Error {
	
		Number number = (Number)get(position, Double.class);
		return number.doubleValue();
	}
	
	
	/**
	 * Retrieves the specified optional double parameter. If it doesn't 
	 * exist the method will return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a double.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public double getOptDouble(final int position, final double defaultValue)
		throws JSONRPC2Error {
	
		Number number = (Number)getOpt(position, Double.class, defaultValue);
		return number.doubleValue();
	}
	
	
	/**
	 * Retrieves the specified list (maps from JSON array) parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a list.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public List getList(final int position)
		throws JSONRPC2Error {
	
		return getList(position, false);
	}
	
	
	/**
	 * Retrieves the specified list (maps from JSON array) parameter.
	 *
	 * @param position  The parameter position.
	 * @param allowNull If {@code true} allows a {@code null} value.
	 *
	 * @return The parameter value as a list.
	 *
	 * @throws JSONRPC2Error On a missing parameter or bad type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public List getList(final int position, final boolean allowNull)
		throws JSONRPC2Error {
	
		return (List)get(position, List.class, allowNull);
	}
	
	
	/**
	 * Retrieves the specified optional list (maps from JSON array) 
	 * parameter. If it doesn't exist the method will return the specified 
	 * default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a list.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public List getOptList(final int position, final List defaultValue)
		throws JSONRPC2Error {
	
		return getOptList(position, false, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified optional list (maps from JSON array) 
	 * parameter. If it doesn't exist the method will return the specified 
	 * default value.
	 *
	 * @param position     The parameter position.
	 * @param allowNull    If {@code true} allows a {@code null} value.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.  May be {@code null}.
	 *
	 * @return The parameter value as a list.
	 *
	 * @throws JSONRPC2Error On a bad parameter type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public List getOptList(final int position, final boolean allowNull, final List defaultValue)
		throws JSONRPC2Error {
	
		return (List)getOpt(position, List.class, allowNull, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified string array (maps from JSON array of strings) 
	 * parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a string array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String[] getStringArray(final int position)
		throws JSONRPC2Error {
	
		return getStringArray(position, false);
	}
	
	
	/**
	 * Retrieves the specified string array (maps from JSON array of strings) 
	 * parameter.
	 *
	 * @param position  The parameter position.
	 * @param allowNull If {@code true} allows a {@code null} value.
	 *
	 * @return The parameter value as a string array.
	 *
	 * @throws JSONRPC2Error On a missing parameter or bad type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String[] getStringArray(final int position, final boolean allowNull)
		throws JSONRPC2Error {
	
		try {
			Object value = get(position);
			
			if (value == null) { 
			
				if (allowNull)
					return null;
				else
					throw JSONRPC2Error.INVALID_PARAMS;
			}
			
			List list = (List)value;
			
			return (String[])list.toArray(new String[]{});
			
		} catch (ClassCastException e) {
			throw JSONRPC2Error.INVALID_PARAMS;
		} catch (ArrayStoreException e) {
			throw JSONRPC2Error.INVALID_PARAMS;
		}
	}
	
	
	/**
	 * Retrieves the specified optional string array (maps from JSON array
	 * of strings) parameter. If it doesn't exist the method will return 
	 * the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a string array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String[] getOptStringArray(final int position, final String[] defaultValue)
		throws JSONRPC2Error {
	
		return getOptStringArray(position, false, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified optional string array (maps from JSON array
	 * of strings) parameter. If it doesn't exist the method will return 
	 * the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param allowNull    If {@code true} allows a {@code null} value.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.  May be {@code null}.
	 *
	 * @return The parameter value as a string array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String[] getOptStringArray(final int position, final boolean allowNull, final String[] defaultValue)
		throws JSONRPC2Error {
	
		if (! hasParameter(position))
			return defaultValue;
	
		return getStringArray(position, allowNull);
	}
	
	
	/**
	 * Retrieves the specified boolean array (maps from JSON array of
	 * true/false values) parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a boolean array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public boolean[] getBooleanArray(final int position)
		throws JSONRPC2Error {
	
		try {
			List list = getList(position);
			boolean[] booleanArray = new boolean[list.size()];
			
			for (int i=0; i < list.size(); i++) {
				booleanArray[i] = (Boolean)list.get(i);
			}
			
			return booleanArray;
			
		} catch (ClassCastException e) {
			throw JSONRPC2Error.INVALID_PARAMS;
		}
	}
	
	
	/**
	 * Retrieves the specified optional boolean array (maps from JSON array
	 * of true/false values) parameter. If it doesn't exist the method will 
	 * return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a boolean array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public boolean[] getOptBooleanArray(final int position, final boolean[] defaultValue)
		throws JSONRPC2Error {
		
		if (! hasParameter(position))
			return defaultValue;
		
		return getBooleanArray(position);
	}
	
	
	/**
	 * Retrieves the specified integer array (maps from JSON array of integer
	 * numbers) parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as an int array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public int[] getIntArray(final int position)
		throws JSONRPC2Error {
		
		try {
			List list = getList(position);
			int[] intArray = new int[list.size()];
			
			for (int i=0; i < list.size(); i++) {
				Number number = (Number)list.get(i);
				intArray[i] = number.intValue();
			}
			
			return intArray;
			
		} catch (ClassCastException e) {
			throw JSONRPC2Error.INVALID_PARAMS;
		}
	}
	
	
	/**
	 * Retrieves the specified optional integer array (maps from JSON array
	 * of integer numbers) parameter. If it doesn't exist the method will 
	 * return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as an int array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public int[] getOptIntArray(final int position, final int[] defaultValue)
		throws JSONRPC2Error {
	
		if (! hasParameter(position))
			return defaultValue;
		
		return getIntArray(position);
	}
	
	
	/**
	 * Retrieves the specified long array (maps from JSON array of integer
	 * numbers) parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a long array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public long[] getLongArray(final int position)
		throws JSONRPC2Error {
	
		try {
			List list = getList(position);
			long[] longArray = new long[list.size()];
			
			for (int i=0; i < list.size(); i++) {
				Number number = (Number)list.get(i);
				longArray[i] = number.longValue();
			}
			
			return longArray;
			
		} catch (ClassCastException e) {
			throw JSONRPC2Error.INVALID_PARAMS;
		}
	}
	
	
	/**
	 * Retrieves the specified optional long array (maps from JSON array
	 * of integer numbers) parameter. If it doesn't exist the method will 
	 * return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a long array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public long[] getOptLongArray(final int position, final long[] defaultValue)
		throws JSONRPC2Error {
		
		if (! hasParameter(position))
			return defaultValue;
		
		return getLongArray(position);
	}
	
	
	/**
	 * Retrieves the specified float array (maps from JSON array of fraction
	 * numbers) parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a float array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public float[] getFloatArray(final int position)
		throws JSONRPC2Error {
	
		try {
			List list = getList(position);
			float[] floatArray = new float[list.size()];
			
			for (int i=0; i < list.size(); i++) {
				Number number = (Number)list.get(i);
				floatArray[i] = number.floatValue();
			}
			
			return floatArray;
			
		} catch (ClassCastException e) {
			throw JSONRPC2Error.INVALID_PARAMS;
		}
	}
	
	
	/**
	 * Retrieves the specified optional float array (maps from JSON array
	 * of fraction numbers) parameter. If it doesn't exist the method will 
	 * return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a float array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public float[] getOptFloatArray(final int position, final float[] defaultValue)
		throws JSONRPC2Error {
		
		if (! hasParameter(position))
			return defaultValue;
		
		return getFloatArray(position);
	}
	
	
	/**
	 * Retrieves the specified double array (maps from JSON array of fraction
	 * numbers) parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a double array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public double[] getDoubleArray(final int position)
		throws JSONRPC2Error {
	
		try {
			List list = getList(position);
			double[] doubleArray = new double[list.size()];
			
			for (int i=0; i < list.size(); i++) {
				Number number = (Number)list.get(i);
				doubleArray[i] = number.doubleValue();
			}
			
			return doubleArray;
			
		} catch (ClassCastException e) {
			throw JSONRPC2Error.INVALID_PARAMS;
		}
	}
	
	
	/**
	 * Retrieves the specified optional double array (maps from JSON array
	 * of fraction numbers) parameter. If it doesn't exist the method will 
	 * return the specified default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a double array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public double[] getOptDoubleArray(final int position, final double[] defaultValue)
		throws JSONRPC2Error {
		
		if (! hasParameter(position))
			return defaultValue;
		
		return getDoubleArray(position);
	}
	
	
	/**
	 * Retrieves the specified map (maps from JSON object) parameter.
	 *
	 * @param position The parameter position.
	 *
	 * @return The parameter value as a map.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Map getMap(final int position)
		throws JSONRPC2Error {
		
		return getMap(position, false);
	}
	
	
	/**
	 * Retrieves the specified map (maps from JSON object) parameter.
	 *
	 * @param position  The parameter position.
	 * @param allowNull If {@code true} allows a {@code null} value.
	 *
	 * @return The parameter value as a map.
	 *
	 * @throws JSONRPC2Error On a missing parameter or bad type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Map getMap(final int position, final boolean allowNull)
		throws JSONRPC2Error {
		
		return (Map)get(position, Map.class, allowNull);
	}
	
	
	/**
	 * Retrieves the specified optional map (maps from JSON object) 
	 * parameter. If it doesn't exist the method will return the specified 
	 * default value.
	 *
	 * @param position     The parameter position.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a map.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Map getOptMap(final int position, final Map defaultValue)
		throws JSONRPC2Error {
	
		return getOptMap(position, false, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified optional map (maps from JSON object) 
	 * parameter. If it doesn't exist the method will return the specified 
	 * default value.
	 *
	 * @param position     The parameter position.
	 * @param allowNull    If {@code true} allows a {@code null} value.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.  May be {@code null}.
	 *
	 * @return The parameter value as a map.
	 *
	 * @throws JSONRPC2Error On a bad parameter type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Map getOptMap(final int position, final boolean allowNull, final Map defaultValue)
		throws JSONRPC2Error {
	
		return (Map)getOpt(position, Map.class, allowNull, defaultValue);
	}
}
