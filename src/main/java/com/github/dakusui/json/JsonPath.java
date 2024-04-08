package com.github.dakusui.json;

import com.google.gson.JsonElement;

import java.util.List;

import static com.github.dakusui.valid8j.Requires.require;
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

  public int length() {
    return this.pathElements.size();
  }

  public Object at(int index) {
    require(index, allOf(greaterThanOrEqualTo(0), lessThan(this.length())));
    return this.pathElements.get(index);
  }

  public JsonElement locate(JsonElement element) {
    require(element, isNotNull());
    return locateByPathElementAt(0, element);
  }

  private JsonElement locateByPathElementAt(int index, JsonElement element) {
    if (index == this.length())
      return element;
    return null;
  }

  public static JsonPath of(List<Object> pathElements) {
    return new JsonPath(pathElements);
  }

  public static JsonPath of(Object... pathElements) {
    return new JsonPath(asList(pathElements));
  }
}
