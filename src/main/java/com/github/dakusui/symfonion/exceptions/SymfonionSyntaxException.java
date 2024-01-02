package com.github.dakusui.symfonion.exceptions;

import java.io.File;
import java.io.Serial;

import com.github.dakusui.json.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SymfonionSyntaxException extends SymfonionException {

	/**
	 * Serial version UID
	 */
	@Serial
	private static final long serialVersionUID = 5992346365176153504L;
	
	private final JsonElement problemCausingJsonNode;
	private final JsonObject root;

	public SymfonionSyntaxException(String message, JsonElement problemCausingJsonNode, JsonObject root) {
		super(message);
		this.problemCausingJsonNode = problemCausingJsonNode;
		this.root = root;
	}
	
	public JsonElement getProblemCausingJsonNode() {
		return this.problemCausingJsonNode;
	}

	public String getJsonPath() {
		if (root == null || problemCausingJsonNode == null)
			return "(n/a)";
		return JsonUtils.findPathOf(this.problemCausingJsonNode, this.root);
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
