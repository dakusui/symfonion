package com.github.dakusui.testutils.json;

import com.google.gson.JsonElement;

/*
 "{patternName}": [
     {
     }
 ]
 */
public abstract class JsonBuilder<B extends JsonBuilder<B>> {
  
  public abstract JsonElement build();
}
