package com.github.dakusui.symfonion.core.exceptions;

import java.util.Arrays;

import com.github.dakusui.json.JsonUtils;
import com.google.gson.JsonElement;

public class SymfonionTypeMismatchException extends SymfonionSyntaxException {
	public static final String OBJECT = "object";
	public static final String ARRAY = "array";
	public static final String PRIMITIVE = "primitive";
	public static final String NULL = "null";

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 6798033658231719409L;


	public SymfonionTypeMismatchException(String[] expectedTypes, JsonElement actualJSON, JsonElement location) {
		super(formatMessage(expectedTypes, actualJSON), location);
	}
	
	private static String formatMessage(String[] expectedTypes, JsonElement actualJSON) {
		String ret;
		if (expectedTypes == null) {
			ret = String.format("%s is not allowed here.", JsonUtils.summarizeJsonElement(actualJSON));
		} else {
			ret = String.format("%s is not allowed here. Acceptable type(s) are %s", JsonUtils.summarizeJsonElement(actualJSON), Arrays.toString(expectedTypes));
		}
		return ret;
	}
}
