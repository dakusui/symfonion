package com.github.dakusui.thincrest_cliche.gson;

import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.testutils.json.JsonTestUtils;
import com.github.valid8j.pcond.forms.Printables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

public class JsonElementTo {
  public static Function<JsonElement, JsonArray> jsonArrayAt(JsonTestUtils.JsonPath jsonPath) {
    return Printables.function("jsonElementAt[" + jsonPath + "]", (JsonElement v) -> CompatJsonUtils.asJsonArray(v, jsonPath.pathComponents()));
  }

  public static Function<JsonElement, JsonObject> jsonObjectAt(JsonTestUtils.JsonPath jsonPath) {
    return Printables.function("jsonElementAt[" + jsonPath + "]", (JsonElement v) -> CompatJsonUtils.asJsonObject(v.getAsJsonObject(), jsonPath.pathComponents()));
  }
}
