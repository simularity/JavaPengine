/**
 * Copyright (c) 2016 Simularity Inc.
 * 

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 * 
 */
package com.simularity.os.javapengine;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * A single 'proof' - a result from Prolog
 * 
 * A Query returns zero or more of these
 * 
 * @author Anne Ogborn
 *
 */
public class Proof {
	JsonObject json;
	
	/**
	 * Constructor based on the returned JSON data element
	 * 
	 * @param jsonValue
	 */
	Proof(JsonObject jsonValue) {
		json = jsonValue;
	}

	/**
	 * Expands the proof to it's JSON object as a string
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return json.toString();
	}
	
	/**
	 * given a key, which is a string representing the Prolog variable, return a JsonValue representing what
	 * it is bound to
	 * 
	 * @param key the Prolog variable, a string with an uppercase first letter
	 * @return the JsonValue
	 */
	public JsonValue getValue(String key) {
		return json.get(key);
	}
	
	/**
	 * Return the set of values as a JSON object
	 * 
	 * @return the JSON object
	 */
	public JsonObject getValues() {
		return json;
	}
	
	/**
	 * Convenience method to getValue that coerces the JSON value to a string.
	 * Usually you'd only use this if you're expecting a string
	 * @param key the Prolog variable, a string with an uppercase first letter
	 * 
	 */
	public String getString(String key) {
		switch (json.get(key).getValueType()) {
		case STRING:
			return ((JsonString)json.get(key)).getString();
		default:
			return json.get(key).toString();
		}
	}
	
	/**
	 * Convenience method to getValue that coerces the JSON value to an int.
	 * Usually you'd only use this if you're expecting an integer return from Prolog
	 * 
	 * Falls back to trying to parse the string if it can't match to an exact integer
	 * 
	 * @param key the Prolog variable, a string with an uppercase first letter
	 * 
	 */
	public int getInt(String key) {
		if (json.get(key).getValueType() == ValueType.NUMBER)
			return ((JsonNumber)json.get(key)).intValueExact();
		else
			return Integer.parseInt(getString(key));
	}

	/**
	 * Convenience method to getValue that coerces the JSON value to an int.
	 * Usually you'd only use this if you're expecting an integer return from Prolog
	 * 
	 * Falls back to trying to parse the string only if it can't get the result as an integer
	 * tries harder to be an integer than getInt
	 * 
	 * @param key the Prolog variable, a string with an uppercase first letter
	 * 
	 */
	public int getNearestInt(String key) {
		if (json.get(key).getValueType() == ValueType.NUMBER)
			return ((JsonNumber)json.get(key)).intValue();
		else
			return Integer.parseInt(getString(key));
	}


	/**
	 * Convenience method to getValue that coerces the JSON value to a double.
	 * Usually you'd only use this if you're expecting a float return from Prolog
	 * 
	 * @param key the Prolog variable, a string with an uppercase first letter
	 * 
	 */
	public double getDouble(String key) {
		if (json.get(key).getValueType() == ValueType.NUMBER)
			return ((JsonNumber)json.get(key)).doubleValue();
		else
			return Double.parseDouble(getString(key));
	}
}