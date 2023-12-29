package com.github.dakusui.json;

import com.google.gson.JsonElement;

import java.io.Serial;

public class JsonException extends RuntimeException {

	/**
	 * Serial version UID.
	 */
	@Serial
	private static final long serialVersionUID = 1383951548283698713L;
	
	private final JsonElement problemCausingNode;

	public JsonException(JsonElement problemCausingNode) {
		this.problemCausingNode = problemCausingNode;
	}
	
	
	public JsonElement getProblemCausingNode() {
		return this.problemCausingNode;
	}
}
