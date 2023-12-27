package com.github.dakusui.symfonion.core;

import com.google.gson.JsonObject;

import static com.github.dakusui.valid8j.Requires.requireNonNull;

public class ExceptionComposer {
  final JsonObject rootObjectNode;
  
  public ExceptionComposer(JsonObject rootObjectNode) {
    this.rootObjectNode = requireNonNull(rootObjectNode);
  }
}
