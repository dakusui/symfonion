package com.github.dakusui.symfonion.core;

import com.google.gson.JsonElement;

public class SymfonionSyntaxException extends SymfonionException {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 5992346365176153504L;
	
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

	private JsonElement location;
	
	public SymfonionSyntaxException(String message, JsonElement location) {
		super(message);
		this.location = location;
	}
	
	public JsonElement getLocation() {
		return this.location;
	}
}
