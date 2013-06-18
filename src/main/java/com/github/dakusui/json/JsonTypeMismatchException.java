package com.github.dakusui.json;

import java.util.Arrays;

import com.google.gson.JsonElement;

public class JsonTypeMismatchException extends JsonException {

	private String[] expectedTypes;

	public JsonTypeMismatchException(JsonElement elem, String... expectedTypes) {
		super(elem);
		this.expectedTypes = expectedTypes;
	}

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -4922304198740292631L;

	private static String formatMessage(String[] expectedTypes, JsonElement actualJSON) {
		String ret = null;
		if (expectedTypes == null) {
			ret = String.format("%s is not allowed here.", summary(actualJSON));
		} else {
			ret = String.format("%s is not allowed here. Acceptable type(s) are %s", summary(actualJSON), Arrays.toString(expectedTypes));
		}
		return ret;
	}
	
	@Override
	public String getMessage() {
		return formatMessage(expectedTypes, getLocation());
	}
}
