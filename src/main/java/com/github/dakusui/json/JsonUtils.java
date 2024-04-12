package com.github.dakusui.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.dakusui.valid8j.ValidationFluents.requireAll;
import static com.github.dakusui.valid8j_cliche.core.Expectations.that;
import static com.github.dakusui.valid8j_pcond.forms.Predicates.isInstanceOf;
import static java.util.Collections.emptyList;
import static java.util.function.Predicate.not;

public enum JsonUtils {
  ;

  public static String summarizeJson(JsonElement rootJsonElement, JsonElement focus) {
    requireAll(
        that(rootJsonElement).satisfies().isNotNull(),
        that(focus).satisfies().isNotNull().predicate(not(isInstanceOf(JsonNull.class))));
    return summarizeJson(
        rootJsonElement,
        locate(rootJsonElement, focus)
            .orElseThrow(() -> new NoSuchElementException("No such element: <" + focus + "> in: <" + rootJsonElement + ">")));
  }

  public static String summarizeJson(JsonElement rootJsonElement, JsonPath jsonPath) {
    requireAll(
        that(rootJsonElement).satisfies().isNotNull(),
        that(jsonPath).satisfies().isNotNull());
    return null; // JsonSummarizer.summaryObject();
  }

  public static Optional<JsonPath> locate(JsonElement root, JsonElement target) {
    requireAll(
        that(target).satisfies().isNotNull(),
        that(target).satisfies().isNotNull().predicate(not(isInstanceOf(JsonNull.class))));
    Map<JsonElement, JsonPath> map = buildJsonPathMap(root);
    return map.containsKey(target) ?
        Optional.of(map.get(target)) :
        Optional.empty();
  }

  private static Map<JsonElement, JsonPath> buildJsonPathMap(JsonElement root) {
    var ret = new IdentityHashMap<JsonElement, JsonPath>();
    return buildJsonPathMap(ret, root, JsonPath.of(emptyList()));
  }

  private static Map<JsonElement, JsonPath> buildJsonPathMap(Map<JsonElement, JsonPath> map, JsonElement currentElement, JsonPath currentPath) {
    if (currentElement.isJsonArray())
      return buildJsonPathMap(map, currentElement.getAsJsonArray(), currentPath);
    if (currentElement.isJsonObject())
      return buildJsonPathMap(map, currentElement.getAsJsonObject(), currentPath);
    if (currentElement.isJsonNull())
      return map;
    map.put(currentElement, currentPath);
    return map;
  }

  private static Map<JsonElement, JsonPath> buildJsonPathMap(Map<JsonElement, JsonPath> map, JsonObject currentElement, JsonPath currentPath) {
    currentElement.keySet().forEach(key -> buildJsonPathMap(map, currentElement.get(key), currentPath.add(key)));
    return map;
  }

  private static Map<JsonElement, JsonPath> buildJsonPathMap(Map<JsonElement, JsonPath> map, JsonArray currentElement, JsonPath currentPath) {
    int i = 0;
    for (var each : currentElement)
      buildJsonPathMap(map, each, currentPath.add(i++));
    return map;
  }
}
