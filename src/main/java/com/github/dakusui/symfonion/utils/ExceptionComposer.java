package com.github.dakusui.symfonion.utils;

import com.google.gson.JsonObject;

import java.io.File;

public record ExceptionComposer(JsonObject rootObjectNode, File sourceFile) {
  
  public static void main(String... args) {
    Object r = new ExceptionComposer(null, null).rootObjectNode();
    System.out.println(r);
  }
  
}
