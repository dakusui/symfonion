package com.github.dakusui.symfonion.compat.json;

import com.github.dakusui.valid8j_cliche.core.Expectations;
import com.github.dakusui.valid8j_cliche.core.Transform;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.github.dakusui.valid8j.Assertions.that;
import static com.github.dakusui.valid8j_cliche.core.Expectations.statement;
import static com.github.dakusui.valid8j_cliche.json.JsonObjectTo.keyList;
import static com.github.dakusui.valid8j_pcond.forms.Predicates.*;
import static com.github.dakusui.valid8j_pcond.forms.Printables.predicate;

public class JsonSummarizer {
  public static JsonElement summaryObject(JsonObject root, List<Object> pathToParent, Object focus) {
    JsonElement previousElement = parentElement(focus, CompatJsonUtils.asJsonElement(root, pathToParent.toArray()));
    JsonElement ret = focus instanceof String ? new JsonObject() : new JsonArray();
    for (int i = pathToParent.size() - 1; i >= 0; i--) {
      Object currentKey = pathToParent.get(i);
      if (currentKey instanceof String) {
        ret = new JsonObject();
        ((JsonObject) ret).add((String) currentKey, previousElement);
      } else if (currentKey instanceof Integer) {
        ret = new JsonArray();
        ((JsonArray) ret).add(previousElement);
      } else
        assert false;
      previousElement = ret;
    }
    return ret;
  }

  public static JsonElement collapseForObjectValue(JsonElement element) {
    JsonElement ret = collapseJsonElement(element);
    if (ret != null) return ret;
    return new JsonPrimitive("...");
  }

  private static JsonElement collapseJsonElement(JsonElement element) {
    if (element.isJsonArray()) {
      JsonArray ret = new JsonArray();
      if (!((JsonArray) element).isEmpty())
        ret.add("...");
      return ret;
    } else if (element.isJsonObject()) {
      JsonObject ret = new JsonObject();
      if (!((JsonObject) element).keySet().isEmpty())
        ret.add("...", new JsonPrimitive("..."));
      return ret;
    }
    return null;
  }

  public static JsonElement collapseForArrayElement(JsonElement element) {
    JsonElement ret = collapseJsonElement(element);
    if (ret != null) return ret;
    return element;
  }

  /**
   * <pre>
   *
   * </pre>
   *
   * @param object input JSON object.
   * @return A summarized JSON object.
   */
  public static JsonObject focusedObject(JsonObject object) {
    JsonObject ret = new JsonObject();
    object.keySet().forEach(k -> ret.add(k, collapseForObjectValue(object.get(k))));
    return ret;
  }

  public static JsonElement parentElement(Object focus, JsonElement parent) {
    Predicate<Object> focusIsInstanceOfString = predicate("focusIsInstanceOfString", v -> focus instanceof String);
    Predicate<Object> focusIsInstanceOfInteger = predicate("focusIsInstanceOfInteger", v -> focus instanceof Integer);
    assert Expectations.all(
        statement(focus, isNotNull()),
        statement(parent, allOf(isNotNull(), or(
            callp("isJsonObject").and(focusIsInstanceOfString),
            callp("isJsonArray").and(focusIsInstanceOfInteger)))));
    if (parent.isJsonObject())
      return parentObject((String) focus, (JsonObject) parent);
    if (parent.isJsonArray())
      return parentArray((Integer) focus, (JsonArray) parent);
    throw new RuntimeException();
  }

  public static JsonObject parentObject(String focusedChildKey, JsonObject object) {
    assert Expectations.all(
        statement(focusedChildKey, isNotNull()),
        statement(object, allOf(
            isNotNull(),
            Transform.$(keyList()).check(contains(focusedChildKey)))));
    JsonObject ret = new JsonObject();
    object
        .keySet()
        .forEach(k -> ret.add(k, Objects.equals(k, focusedChildKey) ?
            focusedElement(object.get(k)) :
            collapseForObjectValue(object.get(k))));
    return ret;
  }

  public static JsonArray focusedArray(JsonArray array) {
    JsonArray ret = new JsonArray();
    array.forEach(each -> ret.add(collapseForArrayElement(each)));
    return ret;
  }

  public static JsonArray parentArray(int focusedChildIndex, JsonArray array) {
    assert Expectations.all(
        statement(array, isNotNull()),
        statement(focusedChildIndex, greaterThanOrEqualTo(0).and(lessThan(array.size()))));
    JsonArray ret = new JsonArray();
    JsonElement focusedChild = array.get(focusedChildIndex);
    array.forEach(i -> ret.add(Objects.equals(i, focusedChild) ?
        focusedElement(i) :
        collapseForArrayElement(i)));
    return ret;
  }

  public static JsonElement focusedElement(JsonElement element) {
    assert that(element, isNotNull());
    JsonElement ret;
    if (element.isJsonObject()) {
      ret = focusedObject((JsonObject) element);
    } else if (element.isJsonArray()) {
      ret = focusedArray((JsonArray) element);
    } else {
      ret = element;
    }
    return ret;
  }

  static JsonPrimitive compactJsonPrimitive(JsonPrimitive primitive, int headLength, int tailLength) {
    if (primitive.isString()) {
      String dots = "...";
      String s = primitive.getAsString();
      if (headLength + dots.length() + tailLength >= s.length())
        return primitive;
      return new JsonPrimitive(s.substring(0, headLength) + dots + s.substring(s.length() - tailLength));
    }
    return primitive;
  }

  static JsonArray compactJsonArray(JsonArray array, int headLength, int tailLength) {
    if (headLength + 1 + tailLength >= array.size())
      return array;
    JsonArray ret = new JsonArray();
    for (int i = 0; i < headLength; i++)
      ret.add(array.get(i));
    ret.add("...");
    for (int i = array.size() - tailLength; i < array.size(); i++)
      ret.add(array.get(i));
    return ret;
  }

  static JsonObject compactJsonObject(JsonObject object, int headLength, int tailLength) {
    if (headLength + 1 + tailLength >= object.size())
      return object;
    JsonObject ret = new JsonObject();
    List<String> keys = object.keySet().stream().sorted().toList();
    for (int i = 0; i < headLength; i++)
      ret.add(keys.get(i), object.get(keys.get(i)));
    ret.add(keys.get(headLength) + "...", new JsonPrimitive("..."));
    for (int i = keys.size() - tailLength; i < keys.size(); i++)
      ret.add(keys.get(i), object.get(keys.get(i)));
    return ret;
  }

  static JsonElement compactJsonElement(JsonElement element, int headLength, int tailLength) {
    if (element.isJsonArray())
      return compactJsonArray((JsonArray) element, headLength, tailLength);
    if (element.isJsonObject())
      return compactJsonObject((JsonObject) element, headLength, tailLength);
    if (element.isJsonPrimitive())
      return compactJsonPrimitive((JsonPrimitive) element, headLength, tailLength);
    return element;
  }
}
