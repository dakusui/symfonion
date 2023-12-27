package com.github.dakusui.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
 
}
