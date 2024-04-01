package com.github.dakusui.json;

import com.google.gson.JsonElement;

import java.io.Serial;

public class CompatJsonException extends RuntimeException {

	/**
	 * Serial version UID.
	 */
	@Serial
	private static final long serialVersionUID = 1383951548283698713L;
	
	private final JsonElement problemCausingNode;

	public CompatJsonException(JsonElement problemCausingNode) {
		this.problemCausingNode = problemCausingNode;
	}
	
	
	public JsonElement getProblemCausingNode() {
		return this.problemCausingNode;
	}
}
