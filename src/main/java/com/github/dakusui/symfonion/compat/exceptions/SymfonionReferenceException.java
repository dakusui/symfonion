package com.github.dakusui.symfonion.compat.exceptions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.Serial;

import static com.github.dakusui.json.JsonUtils.createSummaryJsonObjectFromPaths;
import static com.github.dakusui.json.JsonUtils.findPathOf;

public class SymfonionReferenceException extends SymfonionSyntaxException {
  /**
   * Serial version UID.
   */
  @Serial
  private static final long serialVersionUID = 3554220091863267192L;
  private final JsonElement referencedNodeHoldingNode;

  public SymfonionReferenceException(String missingReference, String type, JsonElement problemCausingJsonNode, JsonObject root, File sourceFile, JsonElement referencedNodeHoldingNode) {
    super(formatMessage(missingReference, type),  problemCausingJsonNode, root, sourceFile);
    this.referencedNodeHoldingNode = referencedNodeHoldingNode;
  }
  
  
  protected static String formatMessage(String missingReference, String type) {
    return String.format("'%s' undefined %s symbol", missingReference, type);
  }

  protected JsonObject summaryRootObjectNode() {
    return createSummaryJsonObjectFromPaths(
        this.rootJsonObjectNode,
        findPathOf(problemCausingJsonNode, this.rootJsonObjectNode),
        findPathOf(referencedNodeHoldingNode, this.rootJsonObjectNode)
    );
  }
}
