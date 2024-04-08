package com.github.dakusui.symfonion.compat.exceptions;

import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.Serial;

public class SymfonionIllegalFormatException extends SymfonionSyntaxException {
  public static final String FRACTION_EXAMPLE = "This value must be a fraction. e.g. '1/2', '1/4', and so on.";
  public static final String NOTE_LENGTH_EXAMPLE = "This value must be a note length. e.g. '4', '8.', '16'";

  /**
   * Serial version UID
   */
  @Serial
  private static final long serialVersionUID = 8614872945878002862L;

  public SymfonionIllegalFormatException(JsonElement problemCausingJsonNode, String explanationOfAcceptableValue, JsonObject root, File sourceFile) {
    super(formatMessage(explanationOfAcceptableValue, problemCausingJsonNode), problemCausingJsonNode, root, sourceFile);
  }

  private static String formatMessage(String acceptableExample, JsonElement problemContainingNode) {
    return String.format("%s is invalid. (%s)", CompatJsonUtils.summarizeJsonElement(problemContainingNode), acceptableExample);
  }

  static public void main(String[] args) {
    System.out.println(new JsonObject());
  }
}
