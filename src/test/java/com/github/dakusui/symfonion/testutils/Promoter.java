package com.github.dakusui.symfonion.testutils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/*

        "$patterns":{
            "01r":{
                "$body":["r4;B;A;G#;A"],
                "$length":16
            },
            "...": "..."
        }

        "$patterns":{
            "01r":[ ["r4;B;A;G#;A"], 16 ],
            "...": "..."
        }

        "$patterns":{
            "01r":[ ["r4;B;A;G#;A"], 16 ],
            "...": "..."
        }

        ////////// INPUT //////////

        "$patterns":{
            "01r": "r4;B;A;G#;A"
            "...": "..."
        }

        //// ."$patterns".* requires Object("$body", "$length":16, ...)

        "$patterns":{
            "01r":{
                "$body":"r4;B;A;G#;A",
                "$length":16
            },
            "...": "..."
        }

        //// ."$patterns".*."$body" requires Array("$body", "$length":16, ...)

        "$patterns":{
            "01r": {
                "$body": ["r4;B;A;G#;A"],
                "$length":16
            },
            "...": "..."
        }

        // One consideration is to parameterize a function to convert a primitive found at the path to an array.
        //
        // Such as    (textNode) -> Arrays.stream(textNode.split(";")).collect(toJsonArray)
        // Instead of (textNode) -> singletonJsonArray(textNode)

        // The same thing can be said to the object node conversion.

 */

public record Promoter(List<String> requiredKeys, List<Entry> optionals) {
  JsonObject promoteToJsonObject(JsonElement element) {
    if (element.isJsonObject())
      return (JsonObject) element;
    JsonObject ret = new JsonObject();
    JsonArray arr = promoteToJsonArray(element);
    int i = 0;
    for (Entry entry : optionals()) {
      if (i < arr.size())
        ret.add(entry.key(), arr.get(i));
      else
        ret.add(entry.key(), entry.defaultValue().orElseThrow(RuntimeException::new));
      i++;
    }
    return ret;
  }

  JsonArray promoteToJsonArray(JsonElement element) {
    if (element.isJsonArray())
      return element.getAsJsonArray();
    return element.isJsonPrimitive() ?
        singletonJsonArray(element.getAsJsonPrimitive()) :
        element.getAsJsonArray();
  }

  private static JsonArray singletonJsonArray(JsonElement element) {
    JsonArray ret = new JsonArray();
    ret.add(element);
    return ret;
  }

  public record Entry(String key, Optional<JsonElement> defaultValue) {
    public static Entry optional(String key, JsonElement defaultValue) {
      return new Entry(requireNonNull(key), Optional.of(requireNonNull(defaultValue)));
    }

    public static Entry required(String key) {
      return new Entry(requireNonNull(key), Optional.empty());
    }
  }
}
