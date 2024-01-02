package com.github.dakusui.json;

import com.github.dakusui.json.JsonUtils.JsonTypes;
import com.google.gson.JsonElement;

import java.io.Serial;
import java.util.Arrays;

import static com.github.dakusui.json.JsonUtils.summarizeJsonElement;

/**
 * An exception thrown when a Json element is found on a certain path has an
 * invalid type.
 * 
 * @author hiroshi
 */
public class JsonTypeMismatchException extends JsonException {
	/**
	 * A serial version UID string.
	 */
	@Serial
	private static final long serialVersionUID = -4922304198740292631L;

	/**
	 * An array of strings that describe types or values that are allowed on the path
	 */
	private final JsonUtils.JsonTypes[] expectedTypes;

	/**
	 * A string that describes the reason why the element is considered invalid.
	 */
	private final String reason;

	/**
	 * Creates an object of this class.
	 * 
	 * @param elem A JSON element whose value is found invalid.
	 * @param expectedTypes Strings which describe expected values.
	 */
	JsonTypeMismatchException(JsonElement elem, JsonUtils.JsonTypes... expectedTypes) {
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
	JsonTypeMismatchException(JsonElement elem, String reason, JsonUtils.JsonTypes... expectedTypes) {
		super(elem);
		this.expectedTypes = expectedTypes;
		this.reason = reason;
	}

	/*
	 * Formats an message.
	 */
	private static String formatMessage(String reason, JsonUtils.JsonTypes[] types, JsonElement actualJSON) {
		String ret = null;
		String r = "";
		if (reason != null) {
			r = String.format("(%s)", reason);
		}
		if (types == null || types.length == 0) {
			ret = String.format("%s is not allowed here %s.", summarizeJsonElement(actualJSON), r);
		} else {
			ret = String.format("%s is not allowed here %s. Acceptable type(s) are %s", summarizeJsonElement(actualJSON), r, Arrays.toString(types));
		}
		return ret;
	}

	/**
	 * Returns a formatted message.
	 */
	@Override
	public String getMessage() {
		return formatMessage(this.reason, this.expectedTypes, getProblemCausingNode());
	}
}
