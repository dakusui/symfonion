package com.github.dakusui.testutils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.dakusui.testutils.json.JsonTestUtils.*;

public abstract class JsonObjectBuilder<B extends JsonObjectBuilder<B>> extends JsonBuilder<B> {
  Map<String, JsonElement> data = new LinkedHashMap<>();
  
  public B add(String key, Object value) {
    return this.add(key, json(value));
  }
  
  @SuppressWarnings("unchecked")
  B add(String key, JsonElement value) {
    this.data.put(key, value);
    return (B) this;
  }
  
  public JsonObject build() {
    return object(data.keySet().stream().map(k -> $(k, data.get(k))).toArray(JsonTestUtils.Entry[]::new));
  }
}
