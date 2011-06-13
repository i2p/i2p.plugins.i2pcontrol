package com.thetransactioncompany.jsonrpc2.util;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thetransactioncompany.jsonrpc2.*;


/**
 * Utility class for retrieving JSON-RPC 2.0 named parameters (key-value pairs
 * packed into a JSON Object). 
 *
 * <p>Provides a set of getter methods according to the expected parameter type
 * (number, string, etc.) and whether the parameter is mandatory or optional:
 *
 * <ul>
 *     <li>{@code getXXX(param_name)} for mandatory parameters, where {@code XXX}
 *         is the expected parameter type.</li>
 *     <li>{@code getOptXXX(param_name, default_value)} for optional parameters,
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
 * <p>Example: suppose you have a method with 3 named parameters "name", "age"
 * and "sex", where the last is optional and defaults to "female":
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
 * // Create a new retriever for named parameters
 * Map params = (Map)request.getParams();
 * NamedParamsRetriever r = new NamedParamsRetriever(params);
 *
 * try {
 *         // Extract "name" string parameter
 *         String name = r.getString("name");
 *
 *         // Extract "age" integer parameter
 *         int age = r.getInt("age");
 *
 *         // Extract optional "sex" string parameter which defaults to "female"
 *         String sex = r.getOptString("sex", "female");
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
public class NamedParamsRetriever 
	extends ParamsRetriever {

	
	/** The named params interface. */
	private Map<String,?> params = null;


	/** 
	 * Creates a new named parameters retriever from the specified 
	 * key-value map.
	 *
	 * @param params The named parameters map.
	 */
	public NamedParamsRetriever(final Map params) {
	
		this.params = params;
	}
	
	
	/**
	 * Returns the number of available named parameters.
	 *
	 * @return The number of named parameters.
	 */
	public int size() {
	
		return params.size();
	}
	
	
	/** 
	 * Returns {@code true} if a parameter by the specified name exists, 
	 * otherwise {@code false}.
	 *
	 * @param name The parameter name.
	 *
	 * @return {@code true} if the parameter exists, otherwise 
	 *         {@code false}.
	 */
	public boolean hasParameter(final String name) {
		
		if (params.containsKey(name))
			return true;
		else
			return false;
	}
	
	
	/**
	 * Returns the names of all available parameters.
	 *
	 * @return The parameter names.
	 */
	public String[] getNames() {
	
		Set keyset = params.keySet();
		
		return (String[]) keyset.toArray(new String[]{});
	}
	
	
	/**
	 * Throws a {@code JSONRPC2Error.INVALID_PARAMS} if the specified
	 * names aren't contained in the parameters, or names outside the
	 * specified are contained.
	 *
	 * <p>You may use this method to a fire a proper JSON-RPC 2.0 error
	 * on a missing or unexpected mandatory parameter name.
	 *
	 * @param mandatoryNames The expected parameter names.
	 *
	 * @throws JSONRPC2Error On a missing parameter name or names outside
	 *                       the specified 
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public void ensureParameters(String[] mandatoryNames)
		throws JSONRPC2Error {
	
		ensureParameters(mandatoryNames, null);
	}
	
	
	/**
	 * Throws a {@code JSONRPC2Error.INVALID_PARAMS} if the specified
	 * mandatory names aren't contained in the parameters, or names outside 
	 * the specified mandatory and optional are contained.
	 *
	 * <p>You may use this method to a fire a proper JSON-RPC 2.0 error
	 * on a missing or unexpected mandatory parameter name.
	 *
	 * @param mandatoryNames The expected mandatory parameter names.
	 * @param optionalNames  The expected optional parameter names,
	 *                       empty array or {@code null} if none.
	 *
	 * @throws JSONRPC2Error On a missing parameter name or names outside
	 *                       the specified mandatory and optional
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public void ensureParameters(String[] mandatoryNames, String[] optionalNames)
		throws JSONRPC2Error {
	
		// Do shallow copy of params
		Map paramsCopy = (Map)((HashMap)params).clone();
	
		// Pop the mandatory names
		for (String name: mandatoryNames) {
			
			if (paramsCopy.containsKey(name))
				paramsCopy.remove(name);
			else
				throw JSONRPC2Error.INVALID_PARAMS;
		}
		
		// Pop the optional names (if any specified)
		if (optionalNames != null) {
		
			for (String name: optionalNames) {

				if (paramsCopy.containsKey(name))
					paramsCopy.remove(name);
			}
		}
		
		// Any remaining keys that shouldn't be there?
		int remainingKeys = paramsCopy.size();
		
		if (remainingKeys > 0)
			throw JSONRPC2Error.INVALID_PARAMS;
	}
	
	
	/**
	 * Throws a {@code JSONRPC2Error.INVALID_PARAMS} exception if there is
	 * no parameter by the specified name.
	 *
	 * <p>You may use this method to fire the proper JSON-RPC 2.0 error
	 * on a missing mandatory parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @throws JSONRPC2Error On a missing parameter
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public void ensureParameter(final String name)
		throws JSONRPC2Error {
		
		if (name == null)
			throw new IllegalArgumentException();
		
		if (! params.containsKey(name))
			throw JSONRPC2Error.INVALID_PARAMS;
	}
	
	
	/**
	 * Throws a {@code JSONRPC2Error.INVALID_PARAMS} exception if there is
	 * no parameter by the specified name, its value is {@code null}, or 
	 * its type doesn't map to the specified.
	 * 
	 * <p>You may use this method to fire the proper JSON-RPC 2.0 error
	 * on a missing or badly-typed mandatory parameter.
	 *
	 * @param name  The parameter name.
	 * @param clazz The corresponding Java class that the parameter should 
	 *              map to (any one of the return types of the {@code getXXX()} 
	 *              getter methods. Set to {@code Object.class} to allow any 
	 *              type.
	 *
	 * @throws JSONRPC2Error On a missing parameter, {@code null} value or 
	 *                       bad type ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public void ensureParameter(final String name, final Class clazz)
		throws JSONRPC2Error {
		
		ensureParameter(name, clazz, false);
	}
	
	
	/**
	 * Throws a {@code JSONRPC2Error.INVALID_PARAMS} exception if there is
	 * no parameter by the specified name or its type doesn't map to the 
	 * specified.
	 * 
	 * <p>You may use this method to fire the proper JSON-RPC 2.0 error
	 * on a missing or badly-typed mandatory parameter.
	 *
	 * @param name      The parameter name.
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
	public void ensureParameter(final String name, final Class clazz, final boolean allowNull)
		throws JSONRPC2Error {
		
		// First, check existence only
		ensureParameter(name);
		
		// Now check type
		Object value = params.get(name);
		
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
	 * @param name The parameter name.
	 *
	 * @return The parameter value.
	 *
	 * @throws JSONRPC2Error On a missing parameter
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Object get(final String name)
		throws JSONRPC2Error {
	
		ensureParameter(name);
	
		return params.get(name);
	}
	
	
	/**
	 * Retrieves the specified parameter which must map to the provided
	 * class (use the appropriate wrapper class for primitive types).
	 *
	 * @param name  The parameter name.
	 * @param clazz The corresponding Java class that the parameter should 
	 *              map to (any one of the return types of the {@code getXXX()} 
	 *              getter methods. Set to {@code Object.class} to allow any
	 *              type.
	 *
	 * @return The parameter value.
	 *
	 * @throws JSONRPC2Error On a missing parameter, {@code null} value or 
	 *                       bad type ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Object get(final String name, final Class clazz)
		throws JSONRPC2Error {
	
		return get(name, clazz, false);
	}
	
	
	/**
	 * Retrieves the specified parameter which must map to the provided
	 * class (use the appropriate wrapper class for primitive types).
	 *
	 * @param name      The parameter name.
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
	public Object get(final String name, final Class clazz, final boolean allowNull)
		throws JSONRPC2Error {
	
		ensureParameter(name, clazz, allowNull);
		
		return params.get(name);
	}
	
	
	/**
	 * Retrieves the specified optional parameter which must map to the
	 * provided class (use the appropriate wrapper class for primitive 
	 * types). If the parameter doesn't exist the method returns the 
	 * specified default value.
	 *
	 * @param name         The parameter name.
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
	public Object getOpt(final String name, final Class clazz, final Object defaultValue)
		throws JSONRPC2Error {
	
		return getOpt(name, clazz, false, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified optional parameter which must map to the
	 * provided class (use the appropriate wrapper class for primitive 
	 * types). If the parameter doesn't exist the method returns the 
	 * specified default value.
	 *
	 * @param name         The parameter name.
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
	public Object getOpt(final String name, final Class clazz, final boolean allowNull, final Object defaultValue)
		throws JSONRPC2Error {
	
		if (! hasParameter(name))
			return defaultValue;
		
		ensureParameter(name, clazz, allowNull);
		
		return params.get(name);
	}
	
	
	/**
	 * Retrieves the specified string parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or {@code null}
	 *                       value ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getString(final String name)
		throws JSONRPC2Error {
		
		return getString(name, false);
	}
	
	
	/**
	 * Retrieves the specified string parameter.
	 *
	 * @param name      The parameter name.
	 * @param allowNull If {@code true} allows a {@code null} value.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a missing parameter or bad type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getString(final String name, final boolean allowNull)
		throws JSONRPC2Error {
		
		return (String)get(name, String.class, allowNull);
	}
	
	
	/**
	 * Retrieves the specified optional string parameter. If it doesn't 
	 * exist the method will return the specified default value.
	 *
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a bad type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getOptString(final String name, final String defaultValue)
		throws JSONRPC2Error {
	
		return getOptString(name, false, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified optional string parameter. If it doesn't 
	 * exist the method will return the specified default value.
	 *
	 * @param name         The parameter name.
	 * @param allowNull    If {@code true} allows a {@code null} value.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a bad type ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getOptString(final String name, final boolean allowNull, final String defaultValue)
		throws JSONRPC2Error {
	
		return (String)getOpt(name, String.class, allowNull, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified enumerated string parameter.
	 *
	 * @param name        The parameter name.
	 * @param enumStrings The acceptable string values.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getEnumString(final String name, final String[] enumStrings)
		throws JSONRPC2Error {
		
		return getEnumString(name, enumStrings, false); 
	}
	
	
	/**
	 * Retrieves the specified enumerated string parameter, allowing for a
	 * case insenstive match.
	 *
	 * @param name        The parameter name.
	 * @param enumStrings The acceptable string values.
	 * @param ignoreCase  {@code true} for a case insensitive match.
	 *
	 * @return The matching parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getEnumString(final String name, final String[] enumStrings, final boolean ignoreCase)
		throws JSONRPC2Error {
		
		String value = (String)get(name, String.class);
		
		return ensureEnumString(value, enumStrings, ignoreCase);
	}
	
	
	/**
	 * Retrieves the specified optional enumerated string parameter.
	 *
	 * @param name         The parameter name.
	 * @param enumStrings  The acceptable string values.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a bad type or bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getOptEnumString(final String name, final String[] enumStrings, final String defaultValue)
		throws JSONRPC2Error {
		
		return getOptEnumString(name, enumStrings, defaultValue, false); 
	}
	
	
	/**
	 * Retrieves the specified optional enumerated string parameter, 
	 * allowing for a case insenstive match. If it doesn't exist the method 
	 * will return the specified default value.
	 *
	 * @param name         The parameter name.
	 * @param enumStrings  The acceptable string values.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 * @param ignoreCase   {@code true} for a case insensitive match.
	 *
	 * @return The matching parameter value as a string.
	 *
	 * @throws JSONRPC2Error On a bad type or bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String getOptEnumString(final String name, final String[] enumStrings, final String defaultValue, final boolean ignoreCase)
		throws JSONRPC2Error {
		
		String value = (String)getOpt(name, String.class, defaultValue);
		
		return ensureEnumString(value, enumStrings, ignoreCase);
	}
	
	
	/**
	 * Retrieves the specified enumerated parameter (from a JSON string that
	 * has a predefined set of possible values).
	 *
	 * @param name      The parameter name.
	 * @param enumClass An enumeration type with constant names representing
	 *                  the acceptable string values.
	 *
	 * @return The matching enumeration constant.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       bad enumeration value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public <T extends Enum<T>> T getEnum(final String name, final Class<T> enumClass)
		throws JSONRPC2Error {
		
		return getEnum(name, enumClass, false);
	}
	
	
	/**
	 * Retrieves the specified enumerated parameter (from a JSON string that
	 * has a predefined set of possible values), allowing for a case 
	 * insensitive match.
	 *
	 * @param name       The parameter name.
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
	public <T extends Enum<T>> T getEnum(final String name, final Class<T> enumClass, final boolean ignoreCase)
		throws JSONRPC2Error {
		
		String value = (String)get(name, String.class);
		
		return ensureEnumString(value, enumClass, ignoreCase);
	}
	
	
	/**
	 * Retrieves the specified optional enumerated parameter (from a JSON
	 * string that has a predefined set of possible values).
	 *
	 * @param name         The parameter name.
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
	public <T extends Enum<T>> T getOptEnum(final String name, final Class<T> enumClass, final T defaultValue)
		throws JSONRPC2Error {
		
		return getOptEnum(name, enumClass, defaultValue, false); 
	}
	
	
	/**
	 * Retrieves the specified optional enumerated parameter (from a JSON
	 * string that has a predefined set of possible values), allowing for 
	 * a case insenstive match. If it doesn't exist the method will return 
	 * the specified default value.
	 *
	 * @param name         The parameter name.
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
	public <T extends Enum<T>> T getOptEnum(final String name, final Class<T> enumClass, final T defaultValue, final boolean ignoreCase)
		throws JSONRPC2Error {
		
		String value = (String)getOpt(name, String.class, defaultValue.toString());
		
		return ensureEnumString(value, enumClass, ignoreCase);
	}
	
	
	/**
	 * Retrieves the specified boolean (maps from JSON true/false)
	 * parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a boolean.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public boolean getBoolean(final String name)
		throws JSONRPC2Error {
	
		return (Boolean)get(name, Boolean.class);
	}
	
	
	/**
	 * Retrieves the specified optional boolean (maps from JSON true/false)
	 * parameter. If it doesn't exist the method will return the specified 
	 * default value.
	 *
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a boolean.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public boolean getOptBoolean(final String name, final boolean defaultValue)
		throws JSONRPC2Error {
	
		return (Boolean)getOpt(name, Boolean.class, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified integer parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as an integer.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public int getInt(final String name)
		throws JSONRPC2Error {
		
		Number number = (Number)get(name, Long.class);
		return number.intValue();
	}
	
	
	/**
	 * Retrieves the specified optional integer parameter. If it doesn't 
	 * exist the method will return the specified default value.
	 *
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a boolean.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public int getOptInt(final String name, final int defaultValue)
		throws JSONRPC2Error {
	
		Number number = (Number)getOpt(name, Long.class, new Long(defaultValue));
		return number.intValue();
	}
	
	
	/**
	 * Retrieves the specified long parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a long.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public long getLong(final String name)
		throws JSONRPC2Error {
	
		Number number = (Number)get(name, Long.class);
		return number.longValue();
	}
	
	
	/**
	 * Retrieves the specified optional long parameter. If it doesn't exist
	 * the method will return the specified default value.
	 *
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a long.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public long getOptLong(final String name, final long defaultValue)
		throws JSONRPC2Error {
	
		Number number = (Number)getOpt(name, Long.class, defaultValue);
		return number.longValue();
	}
	
	
	/**
	 * Retrieves the specified float parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a float.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public float getFloat(final String name)
		throws JSONRPC2Error {
	
		Number number = (Number)get(name, Double.class);
		return number.floatValue();
	}
	
	
	/**
	 * Retrieves the specified optional float parameter. If it doesn't 
	 * exist the method will return the specified default value.
	 *
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a float.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public float getOptFloat(final String name, final float defaultValue)
		throws JSONRPC2Error {
	
		Number number = (Number)getOpt(name, Double.class, new Double(defaultValue));
		return number.floatValue();
	}
	
	
	/**
	 * Retrieves the specified double parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a double.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public double getDouble(final String name)
		throws JSONRPC2Error {
	
		Number number = (Number)get(name, Double.class);
		return number.doubleValue();
	}
	
	
	/**
	 * Retrieves the specified optional double parameter. If it doesn't 
	 * exist the method will return the specified default value.
	 *
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a double.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public double getOptDouble(final String name, final double defaultValue)
		throws JSONRPC2Error {
	
		Number number = (Number)getOpt(name, Double.class, defaultValue);
		return number.doubleValue();
	}
	
	
	/**
	 * Retrieves the specified list (maps from JSON array) parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a list.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public List getList(final String name)
		throws JSONRPC2Error {
	
		return getList(name, false);
	}
	
	
	/**
	 * Retrieves the specified list (maps from JSON array) parameter.
	 *
	 * @param name      The parameter name.
	 * @param allowNull If {@code true} allows a {@code null} value.
	 *
	 * @return The parameter value as a list.
	 *
	 * @throws JSONRPC2Error On a missing parameter or bad type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public List getList(final String name, final boolean allowNull)
		throws JSONRPC2Error {
	
		return (List)get(name, List.class, allowNull);
	}
	
	
	/**
	 * Retrieves the specified optional list (maps from JSON array) 
	 * parameter. If it doesn't exist the method will return the specified 
	 * default value.
	 *
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a list.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public List getOptList(final String name, final List defaultValue)
		throws JSONRPC2Error {
	
		return getOptList(name, false, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified optional list (maps from JSON array) 
	 * parameter. If it doesn't exist the method will return the specified 
	 * default value.
	 *
	 * @param name         The parameter name.
	 * @param allowNull    If {@code true} allows a {@code null} value.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a list.
	 *
	 * @throws JSONRPC2Error On a bad parameter type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public List getOptList(final String name, final boolean allowNull, final List defaultValue)
		throws JSONRPC2Error {
	
		return (List)getOpt(name, List.class, allowNull, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified string array (maps from JSON array of 
	 * strings) parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a string array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String[] getStringArray(final String name)
		throws JSONRPC2Error {
	
		return getStringArray(name, false);
	}
	
	
	/**
	 * Retrieves the specified string array (maps from JSON array of 
	 * strings) parameter.
	 *
	 * @param name      The parameter name.
	 * @param allowNull If {@code true} allows a {@code null} value.
	 *
	 * @return The parameter value as a string array.
	 *
	 * @throws JSONRPC2Error On a missing parameter or bad type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String[] getStringArray(final String name, final boolean allowNull)
		throws JSONRPC2Error {
	
		try {
			Object value = get(name);
			
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
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a string array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String[] getOptStringArray(final String name, final String[] defaultValue)
		throws JSONRPC2Error {
	
		return getOptStringArray(name, false, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified optional string array (maps from JSON array
	 * of strings) parameter. If it doesn't exist the method will return 
	 * the specified default value.
	 *
	 * @param name         The parameter name.
	 * @param allowNull    If {@code true} allows a {@code null} value.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a string array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public String[] getOptStringArray(final String name, final boolean allowNull, final String[] defaultValue)
		throws JSONRPC2Error {
	
		if (! hasParameter(name))
			return defaultValue;
	
		return getStringArray(name, allowNull);
	}
	
	
	/**
	 * Retrieves the specified boolean array (maps from JSON array of
	 * true/false values) parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a boolean array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public boolean[] getBooleanArray(final String name)
		throws JSONRPC2Error {
	
		try {
			List list = getList(name);
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
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a boolean array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public boolean[] getOptBooleanArray(final String name, final boolean[] defaultValue)
		throws JSONRPC2Error {
		
		if (! hasParameter(name))
			return defaultValue;
		
		return getBooleanArray(name);
	}
	
	
	/**
	 * Retrieves the specified integer array (maps from JSON array of 
	 * integer numbers) parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as an int array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or 
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public int[] getIntArray(final String name)
		throws JSONRPC2Error {
		
		try {
			List list = getList(name);
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
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as an int array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public int[] getOptIntArray(final String name, final int[] defaultValue)
		throws JSONRPC2Error {
	
		if (! hasParameter(name))
			return defaultValue;
	
		return getIntArray(name);
	}
	
	
	/**
	 * Retrieves the specified long array (maps from JSON array of integer
	 * numbers) parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a long array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public long[] getLongArray(final String name)
		throws JSONRPC2Error {
	
		try {
			List list = getList(name);
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
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a long array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public long[] getOptLongArray(final String name, final long[] defaultValue)
		throws JSONRPC2Error {
		
		if (! hasParameter(name))
			return defaultValue;
		
		return getLongArray(name);
	}
	
	
	/**
	 * Retrieves the specified float array (maps from JSON array of 
	 * fraction numbers) parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a float array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public float[] getFloatArray(final String name)
		throws JSONRPC2Error {
	
		try {
			List list = getList(name);
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
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a float array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public float[] getOptFloatArray(final String name, final float[] defaultValue)
		throws JSONRPC2Error {
		
		if (! hasParameter(name))
			return defaultValue;
		
		return getFloatArray(name);
	}
	
	
	/**
	 * Retrieves the specified double array (maps from JSON array of 
	 * fraction numbers) parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a double array.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public double[] getDoubleArray(final String name)
		throws JSONRPC2Error {
	
		try {
			List list = getList(name);
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
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a double array.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public double[] getOptDoubleArray(final String name, final double[] defaultValue)
		throws JSONRPC2Error {
		
		if (! hasParameter(name))
			return defaultValue;
		
		return getDoubleArray(name);
	}
	
	
	/**
	 * Retrieves the specified map (maps from JSON object) parameter.
	 *
	 * @param name The parameter name.
	 *
	 * @return The parameter value as a map.
	 *
	 * @throws JSONRPC2Error On a missing parameter, bad type or
	 *                       {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Map getMap(final String name)
		throws JSONRPC2Error {
		
		return getMap(name, false);
	}
	
	
	/**
	 * Retrieves the specified map (maps from JSON object) parameter.
	 *
	 * @param name      The parameter name.
	 * @param allowNull If {@code true} allows a {@code null} value.
	 *
	 * @return The parameter value as a map.
	 *
	 * @throws JSONRPC2Error On a missing parameter or bad type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Map getMap(final String name, final boolean allowNull)
		throws JSONRPC2Error {
		
		return (Map)get(name, Map.class, allowNull);
	}
	
	
	/**
	 * Retrieves the specified optional map (maps from JSON object) 
	 * parameter. If it doesn't exist the method will return the specified 
	 * default value.
	 *
	 * @param name         The parameter name.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist. May be {@code null}.
	 *
	 * @return The parameter value as a map.
	 *
	 * @throws JSONRPC2Error On a bad parameter type or {@code null} value
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Map getOptMap(final String name, final Map defaultValue)
		throws JSONRPC2Error {
	
		return getOptMap(name, false, defaultValue);
	}
	
	
	/**
	 * Retrieves the specified optional map (maps from JSON object) 
	 * parameter. If it doesn't exist the method will return the specified 
	 * default value.
	 *
	 * @param name         The parameter name.
	 * @param allowNull    If {@code true} allows a {@code null} value.
	 * @param defaultValue The default return value if the parameter 
	 *                     doesn't exist.
	 *
	 * @return The parameter value as a map.
	 *
	 * @throws JSONRPC2Error On a bad parameter type
	 *                       ({@link JSONRPC2Error#INVALID_PARAMS}).
	 */
	public Map getOptMap(final String name, final boolean allowNull, final Map defaultValue)
		throws JSONRPC2Error {
	
		return (Map)getOpt(name, Map.class, allowNull, defaultValue);
	}
}
