package com.github.dakusui.json;

import com.github.dakusui.json.JsonUtil.JsonTypes;
import com.google.gson.JsonElement;

import java.util.Arrays;

/**
 * An exception thrown when a Json element is found on a certain path has an
 * invalid type.
 * 
 * @author hiroshi
 */
public class JsonTypeMismatchException extends JsonException {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -4922304198740292631L;

	/**
	 * An array of strings that describe types or values that are allowed on the path
	 */
	private JsonUtil.JsonTypes[] expectedTypes;

	/**
	 * A string that describes the reason why the element is considered invalid.
	 */
	private String reason;

	/**
	 * Creates an object of this class.
	 * 
	 * @param elem A JSON element whose value is found invalid.
	 * @param expectedTypes Strings which describe expected values.
	 */
	public JsonTypeMismatchException(JsonElement elem, JsonUtil.JsonTypes... expectedTypes) {
		this(elem, null, expectedTypes);
	}

	/**
	 * Creates an object of this class.
	 * 
	 * @param elem A JSON element whose value is found invalid.
	 * @param reason A string that describes the reason why <code>elem</code> was considered invalid.
	 */
	public JsonTypeMismatchException(JsonElement elem, String reason) {
		this(elem, reason, new JsonTypes[] {});
	}

	/**
	 * Creates an object of this class.
	 * 
	 * @param elem A JSON element whose value is found invalid.
	 * @param expectedTypes Strings which describe expected values.
	 * @param reason A string that describes the reason why <code>elem</code> was considered invalid.
	 */
	public JsonTypeMismatchException(JsonElement elem, String reason, JsonUtil.JsonTypes... expectedTypes) {
		super(elem);
		this.expectedTypes = expectedTypes;
		this.reason = reason;
	}

	/*
	 * Formats an message.
	 */
	private static String formatMessage(String reason, JsonUtil.JsonTypes[] types, JsonElement actualJSON) {
		String ret = null;
		String r = "";
		if (reason != null) {
			r = String.format("(%s)", reason);
		}
		if (types == null || types.length == 0) {
			ret = String.format("%s is not allowed here %s.", summary(actualJSON), r);
		} else {
			ret = String.format("%s is not allowed here %s. Acceptable type(s) are %s", summary(actualJSON), r, Arrays.toString(types));
		}
		return ret;
	}

	/**
	 * Returns a formatted message.
	 */
	@Override
	public String getMessage() {
		return formatMessage(this.reason, this.expectedTypes, getLocation());
	}
}
