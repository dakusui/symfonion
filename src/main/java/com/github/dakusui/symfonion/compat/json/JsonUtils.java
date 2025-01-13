package com.github.dakusui.symfonion.compat.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Optional;

import static com.github.valid8j.fluent.Expectations.require;
import static com.github.valid8j.fluent.Expectations.value;

/**
 * //@formatter:off
 * //@formatter:on
 */
public enum JsonUtils {
  ;
  private static final JsonElement DEFAULT_VALUE = new JsonArray();
  public static Path path(Object... path) {
    return new Path(path);
  }

  public static Optional<JsonElement> findJsonElement(JsonElement base, Path path) {
    return getJsonElement(JsonElement.class, base, path);
  }

  public static Optional<JsonObject> findJsonObject(JsonElement base, Path path) {
    return getJsonElement(JsonObject.class, base, path);
  }

  public static Optional<JsonArray> findJsonArray(JsonElement base, Path path) {
    return getJsonElement(JsonArray.class, base, path);
  }

  public static Optional<JsonPrimitive> findJsonPrimitive(JsonPrimitive base, Path path) {
    return getJsonElement(JsonPrimitive.class, base, path);
  }

  @SuppressWarnings("unchecked")
  private static <T extends JsonElement> Optional<T> getJsonElement(Class<T> jsonType, JsonElement base, Path path) {
    var ret = require(value(CompatJsonUtils.asJsonElementWithDefault(base, DEFAULT_VALUE, path.path())).toBe()
                                                                                                       .instanceOf(jsonType));
    return ret != DEFAULT_VALUE ? Optional.of((T) ret)
                                : Optional.empty();
  }

  public record Path(Object... path) {
  }
}
