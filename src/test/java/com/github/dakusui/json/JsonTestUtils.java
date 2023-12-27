package com.github.dakusui.json;

import com.google.gson.*;

import java.util.Arrays;

public enum JsonTestUtils {
  ;
  
  public static JsonObject object(Entry... entries) {
    JsonObject ret = new JsonObject();
    for (Entry each : entries)
      ret.add(each.key(), each.value());
    return ret;
  }
  
  public static JsonArray array(Object... objects) {
    return array((JsonElement[]) Arrays.stream(objects)
        .map(JsonTestUtils::json)
        .toArray(JsonElement[]::new));
  }
  
  public static JsonArray array(JsonElement... elements) {
    JsonArray ret = new JsonArray(elements.length);
    for (JsonElement each : elements)
      ret.add(each);
    return ret;
  }
  
  public static Entry $(String key, JsonElement value) {
    return entry(key, value);
  }
  
  public static Entry entry(String key, JsonElement value) {
    return new Entry(key, value);
  }
  
  public static JsonElement json(Object object) {
    if (object instanceof JsonElement)
      return (JsonElement) object;
    if (object instanceof Number)
      return new JsonPrimitive((Number) object);
    if (object instanceof String)
      return new JsonPrimitive((String) object);
    if (object instanceof Boolean)
      return new JsonPrimitive((Boolean) object);
    if (object == null)
      return JsonNull.INSTANCE;
    throw new RuntimeException();
  }
  
  public record Entry(String key, JsonElement value) {
  }
}
