package com.github.dakusui.symfonion.core;

import com.google.gson.JsonElement;

public class SymfonionReferenceException extends SymfonionSyntaxException {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 3554220091863267192L;

	public SymfonionReferenceException(String missingReference, String type, JsonElement location) {
		super(formatMessage(missingReference, type), location);
	}

	
	
	protected static String formatMessage(String missingReference, String type) {
		String ret = String.format("'%s' undefined %s symbol", missingReference, type);
		return ret;
	}
	

}
