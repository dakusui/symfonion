package com.github.dakusui.symfonion.compat.json;

import com.google.gson.JsonElement;

import java.io.Serial;

public class JsonFormatException extends CompatJsonException {

	public JsonFormatException(JsonElement elem) {
		super(elem)	;
	}

	/**
	 * A serial version UID string.
	 */
	@Serial
	private static final long serialVersionUID = 7421426791291041934L;

}
