package com.github.dakusui.json;

import com.google.gson.JsonElement;

public class JsonFormatException extends JsonException {

	public JsonFormatException(JsonElement elem) {
		super(elem)	;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7421426791291041934L;

}
