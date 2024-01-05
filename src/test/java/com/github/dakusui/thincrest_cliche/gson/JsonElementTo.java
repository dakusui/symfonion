package com.github.dakusui.thincrest_cliche.gson;

import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.testutils.json.JsonTestUtils;
import com.github.dakusui.thincrest_pcond.forms.Printables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

public class JsonElementTo {
  public static Function<JsonElement, JsonArray> jsonArrayAt(JsonTestUtils.JsonPath jsonPath) {
    return Printables.function("jsonElementAt[" + jsonPath + "]", (JsonElement v) -> JsonUtils.asJsonArray(v, jsonPath.pathComponents()));
  }

  public static Function<JsonElement, JsonObject> jsonObjectAt(JsonTestUtils.JsonPath jsonPath) {
    return Printables.function("jsonElementAt[" + jsonPath + "]", (JsonElement v) -> JsonUtils.asJsonObject(v.getAsJsonObject(), jsonPath.pathComponents()));
  }
}
