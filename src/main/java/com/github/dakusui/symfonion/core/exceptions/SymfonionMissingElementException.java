package com.github.dakusui.symfonion.core.exceptions;

import com.github.dakusui.json.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serial;

public class SymfonionMissingElementException extends SymfonionSyntaxException {
  
  /**
   * Serial version UID.
   */
  @Serial
  private static final long serialVersionUID = -3887808558721595868L;
  
  public SymfonionMissingElementException(JsonElement actualJSON, JsonObject root, Object relPath) {
    super(formatMessage(actualJSON, relPath), actualJSON, root);
  }
  
  private static String formatMessage(JsonElement actualJSON, Object relPath) {
    if (relPath instanceof Number) {
      return String.format("%s at this path requires %dth element", JsonUtils.summarizeJsonElement(actualJSON), relPath);
    }
    return String.format("%s at this path requires child element %s", JsonUtils.summarizeJsonElement(actualJSON), relPath);
  }
  
}
