package com.github.dakusui.symfonion.core.exceptions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serial;

public class SymfonionReferenceException extends SymfonionSyntaxException {
  /**
   * Serial version UID.
   */
  @Serial
  private static final long serialVersionUID = 3554220091863267192L;
  
  public SymfonionReferenceException(String missingReference, String type, JsonElement problemCausingJsonNode, JsonObject root) {
    super(formatMessage(missingReference, type),  problemCausingJsonNode, root);
  }
  
  
  protected static String formatMessage(String missingReference, String type) {
    return String.format("'%s' undefined %s symbol", missingReference, type);
  }
}
