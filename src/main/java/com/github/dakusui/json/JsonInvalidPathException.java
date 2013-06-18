package com.github.dakusui.json;

import java.util.Arrays;

import com.google.gson.JsonElement;

public class JsonInvalidPathException extends JsonException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7832147182391783569L;

	protected String formatMessage(JsonElement base, Object[] path) {
		return String.format("This element doesn't have path: %s", Arrays.toString(path));
	}
	
	private JsonElement base;

	private Object[] path;

	private int index;

	private String message;

	public JsonInvalidPathException(JsonElement base, Object[] path, int index) {
		super(base);
		this.message = formatMessage(base, path);
		this.base = base;
		this.path = path;
		this.index = index;
	}

	public JsonElement getLocation() {
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
