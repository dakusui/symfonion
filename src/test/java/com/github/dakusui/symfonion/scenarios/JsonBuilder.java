package com.github.dakusui.symfonion.scenarios;

import com.google.gson.JsonElement;

/*
 "{patternName}": [
     {
     }
 ]
 */
abstract class JsonBuilder<B extends JsonBuilder<B>> {
  
  abstract JsonElement build();
}
