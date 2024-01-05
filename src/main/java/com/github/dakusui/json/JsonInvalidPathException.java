package com.github.dakusui.json;

import com.google.gson.JsonElement;

import java.io.Serial;
import java.util.Arrays;

public class JsonInvalidPathException extends JsonException {
	/**
	 * A serial version UID string.
	 */
	@Serial
	private static final long serialVersionUID = 7832147182391783569L;

	protected String formatMessage(JsonElement base, Object[] path) {
		return String.format("This element (%s) doesn't have path: %s", JsonSummarizer.focusedElement(base), Arrays.toString(path));
	}
	
	private final JsonElement base;

	private final Object[] path;

	private final int index;

	private final String message;

	public JsonInvalidPathException(JsonElement base, Object[] path, int index) {
		super(base);
		this.message = formatMessage(base, path);
		this.base = base;
		this.path = path;
		this.index = index;
	}

  public JsonInvalidPathException(JsonElement base, Object[] path) {
    this(base, path, 0);
  }

  public JsonElement getProblemCausingNode() {
		return base;
	}

	public Object[] getPath() {
		return path;
	}

	public int  getIndex() {
		return this.index;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

}
