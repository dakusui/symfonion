package com.github.dakusui.symfonion.exceptions;

import com.github.dakusui.json.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.Serial;

public class SymfonionIllegalFormatException extends SymfonionSyntaxException {
	public static final String FRACTION_EXAMPLE = "This value must be a fraction. e.g. '1/2', '1/4', and so on.";
	public static final String NOTE_LENGTH_EXAMPLE = "This value must be a note length. e.g. '4', '8.', '16'";
	
	/**
	 * Serial version UID
	 */
	@Serial
	private static final long serialVersionUID = 8614872945878002862L;

	public SymfonionIllegalFormatException(JsonElement problemCausingJsonNode, JsonObject root, String acceptableExample, File sourceFile) {
		super(formatMessage(acceptableExample, problemCausingJsonNode), problemCausingJsonNode, root, sourceFile);
	}

	private static String formatMessage(String acceptableExample,
			JsonElement location) {
		return String.format("%s is invalid. (%s)", JsonUtils.summarizeJsonElement(location), acceptableExample);
	}

	static public void main(String[] args) {
		System.out.println(new JsonObject());
	}
}
