package com.github.dakusui.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static com.github.dakusui.symfonion.exception.SymfonionExceptionThrower.*;
import static com.github.dakusui.symfonion.exception.SymfonionExceptionThrower.Key.ROOT_JSON_ELEMENT;
import static com.github.dakusui.valid8j.Requires.require;
import static com.github.dakusui.valid8j_cliche.core.ClassicExpectations.validate;
import static com.github.dakusui.valid8j_pcond.forms.Functions.stream;
import static com.github.dakusui.valid8j_pcond.forms.Predicates.*;
import static java.util.Arrays.asList;

public class JsonPath {
  private final List<Object> pathElements;

  private JsonPath(List<Object> pathElements) {
    this.pathElements = require(pathElements, allOf(
        isNotNull(),
        transform(stream()).allOf(
            isNotNull(),
            or(
                isInstanceOf(String.class),
                isInstanceOf(Integer.class)))));
  }

  public JsonPath add(int index) {
    return new JsonPath(new ArrayList<>() {{
      this.addAll(pathElements);
      this.add(index);
    }});
  }

  public JsonPath add(String key) {
    return new JsonPath(new ArrayList<>() {{
      this.addAll(pathElements);
      this.add(key);
    }});
  }

  public int length() {
    return this.pathElements.size();
  }

  public Object at(int index) {
    require(index, allOf(greaterThanOrEqualTo(0), lessThan(this.length())));
    return this.pathElements.get(index);
  }

  public JsonElement locate(JsonElement element) {
    require(element, isNotNull());
    try (var ignored = context($(ROOT_JSON_ELEMENT, element))) {
      return locateByPathElementAt(0, element);
    }
  }

  private JsonElement locateByPathElementAt(int pathElementIndex, JsonElement element) {
    if (pathElementIndex == this.length())
      return element;
    Object pathElement = this.pathElements.get(pathElementIndex);
    if (pathElement instanceof Integer) {
      int index = (Integer) pathElement;
      JsonArray curArray = requireJsonArray(element);
      return locateByPathElementAt(pathElementIndex + 1, getElementFromArrayByIndex(curArray, index));
    }
    // It is guaranteed that pathElement is a String by the validation at instantiation.
    String key = (String) pathElement;
    JsonObject curObject = requireJsonObject(element);
    return locateByPathElementAt(pathElementIndex + 1, getElementFromJsonObjectByKey(curObject, key));
  }

  private static JsonElement getElementFromArrayByIndex(JsonArray curArray, int index) {
    validate(index, allOf(greaterThanOrEqualTo(0), lessThan(curArray.size())), JSON_ARRAY_INDEX_OUT_OF_BOUNDS::exception);
    return curArray.get(index);
  }

  private static JsonElement getElementFromJsonObjectByKey(JsonObject curObject, String key) {
    return curObject.get(key);
  }

  private static JsonObject requireJsonObject(JsonElement curElement) {
    return (JsonObject) require(curElement, isInstanceOf(JsonObject.class));
  }

  private static JsonArray requireJsonArray(JsonElement curElement) {
    return (JsonArray) require(curElement, isInstanceOf(JsonArray.class));
  }

  public static JsonPath of(List<Object> pathElements) {
    return new JsonPath(pathElements);
  }

  public static JsonPath of(Object... pathElements) {
    return new JsonPath(asList(pathElements));
  }
}
