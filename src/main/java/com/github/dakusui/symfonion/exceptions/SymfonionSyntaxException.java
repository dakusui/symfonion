package com.github.dakusui.symfonion.exceptions;

import java.io.File;
import java.io.Serial;

import com.github.dakusui.json.JsonUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SymfonionSyntaxException extends SymfonionException {

  /**
   * Serial version UID
   */
  @Serial
  private static final long serialVersionUID = 5992346365176153504L;

  private final JsonElement problemCausingJsonNode;
  private final JsonObject rootJsonObjectNode;

  public SymfonionSyntaxException(String message, JsonElement problemCausingJsonNode, JsonObject rootJsonObjectNode, File sourceFile) {
    super(message, sourceFile);
    this.problemCausingJsonNode = problemCausingJsonNode;
    this.rootJsonObjectNode = rootJsonObjectNode;
  }

  public JsonElement getProblemCausingJsonNode() {
    return this.problemCausingJsonNode;
  }

  public String getJsonPath() {
    if (rootJsonObjectNode == null || problemCausingJsonNode == null)
      return "(n/a)";
    return JsonUtils.findPathOf(this.problemCausingJsonNode, this.rootJsonObjectNode);
  }

  @Override
  public String getMessage() {
    String msg = "jsonpath: " + this.getJsonPath() + ": error: " + super.getMessage();
    File src = this.getSourceFile();
    if (src != null) {
      msg = src.getPath() + ": " + msg;
    }
    return String.format(
        """
            %s
            
            .Path to the problem causing node:
            ----
            %s
            ----

            .Problem causing node:
            ----
            %s
            ----
            
            .Whole JSON:
            ----
            %s
            ----
            """,
        msg,
        this.getJsonPath(),
        this.getProblemCausingJsonNode(),
        new GsonBuilder().setPrettyPrinting().create().toJson(rootJsonObjectNode));
  }
}
