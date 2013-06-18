package com.github.dakusui.json;

import com.google.gson.JsonElement;

public class JsonException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1383951548283698713L;
	
	private JsonElement location;

	public JsonException(JsonElement location) {
		this.location = location;
	}
	
	
	public JsonElement getLocation() {
		return this.location;
	}

	protected static String summary(JsonElement actualJSON) {
		if (actualJSON == null || actualJSON.isJsonNull()) {
			return "null";
		}
		if (actualJSON.isJsonPrimitive()) {
			return actualJSON.getAsString() + "(primitive)";
		}
		if (actualJSON.isJsonArray()) {
			return "array(size=" + actualJSON.getAsJsonArray().size() + ")";
		}
		if (actualJSON.isJsonObject()) {
			return "object(" + actualJSON.getAsJsonObject().entrySet().size() + " entries)";
		}
		return actualJSON.toString() + "(unknown)";
	}
}
