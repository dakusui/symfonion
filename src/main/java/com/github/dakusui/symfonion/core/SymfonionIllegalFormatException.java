package com.github.dakusui.symfonion.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SymfonionIllegalFormatException extends SymfonionSyntaxException {
	public static final String FRACTION_EXAMPLE = "";
	public static final String NOTELENGTH_EXAMPLE = "This value must be a note length";
	
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 8614872945878002862L;

	public SymfonionIllegalFormatException(JsonElement location, String acceptableExample) {
		super(formatMessage(acceptableExample, location), location);
	}

	private static String formatMessage(String acceptableExample,
			JsonElement location) {
		return String.format("%s is invalid. (%s)", summary(location), acceptableExample);
	}

	static public void main(String[] args) {
		System.out.println(new JsonObject().toString());
	}
}
