package com.github.dakusui.symfonion.compat.json;

import com.google.gson.JsonElement;

import java.io.Serial;

public class JsonPathNotFoundException extends JsonInvalidPathException {

	public JsonPathNotFoundException(JsonElement base, Object[] path, int from) {
		super(base, path);
	}

	/**
	 * A serial version UID string.
	 */
	@Serial
	private static final long serialVersionUID = -7545472562920182758L;

}
