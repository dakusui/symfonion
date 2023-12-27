package com.github.dakusui.symfonion.core;

import com.google.gson.JsonObject;

import java.io.File;

import static com.github.dakusui.valid8j.Requires.requireNonNull;

public record ExceptionComposer(JsonObject rootObjectNode, File sourceFile) {
  
  public static void main(String... args) {
    Object r = new ExceptionComposer(null, null).rootObjectNode();
    System.out.println(r);
  }
  
}
