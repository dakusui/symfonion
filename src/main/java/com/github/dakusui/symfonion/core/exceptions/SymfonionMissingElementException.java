package com.github.dakusui.symfonion.core.exceptions;

import com.github.dakusui.json.JsonUtils;
import com.google.gson.JsonElement;

public class SymfonionMissingElementException extends SymfonionSyntaxException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = -3887808558721595868L;

	public SymfonionMissingElementException(JsonElement actualJSON,
			Object relPath) {
		super(formatMessage(actualJSON, relPath), actualJSON);
	}

	private static String formatMessage(JsonElement actualJSON, Object relPath) {
		if (relPath instanceof Number) {
          return String.format("%s at this path requires %dth element", JsonUtils.summarizeJsonElement(actualJSON), relPath);
		}
      return String.format("%s at this path requires child element %s", JsonUtils.summarizeJsonElement(actualJSON), relPath);
	}

}
