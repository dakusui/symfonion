package com.github.dakusui.symfonion.core.exceptions;

import java.io.File;

import com.google.gson.JsonElement;

public class SymfonionSyntaxException extends SymfonionException {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 5992346365176153504L;
	
	private JsonElement location;
	private String jsonpath;
	
	public SymfonionSyntaxException(String message, JsonElement location) {
		super(message);
		this.location = location;
	}
	
	public JsonElement getLocation() {
		return this.location;
	}

	public void setJsonPath(String path) {
		this.jsonpath = path;
	}
	
	public String getJsonPath() {
		if (this.jsonpath == null) {
			return "n/a";
		}
		return this.jsonpath;
	}
	
	@Override
	public String getMessage() {
		String msg = "jsonpath: " + this.getJsonPath() + ": error: " + super.getMessage();
		File src = this.getSourceFile();
		if (src != null) {
			msg = src.getPath() + ": " + msg;
		}
		return msg;
	}
}
