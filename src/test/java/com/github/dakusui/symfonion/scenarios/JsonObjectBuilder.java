package com.github.dakusui.symfonion.scenarios;

import com.github.dakusui.testutils.json.JsonTestUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.dakusui.testutils.json.JsonTestUtils.*;

abstract class JsonObjectBuilder<B extends JsonObjectBuilder<B>> extends JsonBuilder<B> {
  Map<String, JsonElement> data = new LinkedHashMap<>();
  
  B add(String key, Object value) {
    return this.add(key, json(value));
  }
  
  @SuppressWarnings("unchecked")
  B add(String key, JsonElement value) {
    this.data.put(key, value);
    return (B) this;
  }
  
  JsonObject build() {
    return object(data.keySet().stream().map(k -> $(k, data.get(k))).toArray(JsonTestUtils.Entry[]::new));
  }
}
