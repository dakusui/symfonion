package com.github.dakusui.symfonion.compat.exceptions;

import java.io.File;
import java.io.Serial;

import com.github.dakusui.json.JsonUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static com.github.dakusui.json.JsonUtils.createSummaryJsonObjectFromPaths;
import static com.github.dakusui.json.JsonUtils.findPathOf;

public class SymfonionSyntaxException extends SymfonionException {

  /**
   * Serial version UID
   */
  @Serial
  private static final long serialVersionUID = 5992346365176153504L;

  protected final JsonElement problemCausingJsonNode;
  protected final JsonObject rootJsonObjectNode;

  public SymfonionSyntaxException(String message, JsonElement problemCausingJsonNode, JsonObject rootJsonObjectNode, File sourceFile) {
    super(message, sourceFile);
    this.problemCausingJsonNode = problemCausingJsonNode;
    this.rootJsonObjectNode = rootJsonObjectNode;
  }

  public JsonElement getProblemCausingJsonNode() {
    return this.problemCausingJsonNode;
  }

  public String toJsonPathString() {
    if (rootJsonObjectNode == null || problemCausingJsonNode == null)
      return "(n/a)";
    return JsonUtils.findPathStringOf(this.problemCausingJsonNode, this.rootJsonObjectNode);
  }

  @Override
  public String getMessage() {
    String msg = "jsonpath: " + this.toJsonPathString() + ": error: " + super.getMessage();
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
        this.toJsonPathString(),
        this.getProblemCausingJsonNode(),
        new GsonBuilder().setPrettyPrinting().create().toJson(summaryRootObjectNode()));
  }

  protected JsonObject summaryRootObjectNode() {
    return createSummaryJsonObjectFromPaths(this.rootJsonObjectNode, findPathOf(this.problemCausingJsonNode, this.rootJsonObjectNode));
  }
}
