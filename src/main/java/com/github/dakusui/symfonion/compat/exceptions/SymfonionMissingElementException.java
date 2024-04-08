package com.github.dakusui.symfonion.compat.exceptions;

import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.Serial;

public class SymfonionMissingElementException extends SymfonionSyntaxException {
  
  /**
   * Serial version UID.
   */
  @Serial
  private static final long serialVersionUID = -3887808558721595868L;
  
  public SymfonionMissingElementException(JsonElement problemCausingJsonNode, Object relativePathFromProblemCausingJsonNode, JsonObject root, File sourceFile) {
    super(formatMessage(problemCausingJsonNode, relativePathFromProblemCausingJsonNode), problemCausingJsonNode, root, sourceFile);
  }
  
  private static String formatMessage(JsonElement problemCausingJsonNode, Object relPath) {
    if (relPath instanceof Integer) {
      return String.format("%s at this path requires %dth element", CompatJsonUtils.summarizeJsonElement(problemCausingJsonNode), relPath);
    }
    return String.format("%s at this path requires child element %s", CompatJsonUtils.summarizeJsonElement(problemCausingJsonNode), relPath);
  }
  
}
