package com.github.dakusui.valid8j_cliche.json;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.valid8j_pcond.forms.Printables.function;

public class JsonObjectTo {

  public static Function<JsonObject, List<String>> keyList() {
    return function("keyList", (JsonObject obj) -> obj.keySet().stream().sorted().toList());
  }
}
