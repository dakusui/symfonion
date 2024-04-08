package com.github.dakusui.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.util.IdentityHashMap;

public class JsonSandbox {
  @Test
  public void testJsonObject() {
    var s = """
        {
          "key": "hello",
          "key2": "hello",
          "key3": {
            "keyX": "helloWorld1",
            "keyY": "helloWorld2"
          },
          "key4": {
            "keyX": "helloWorld1",
            "keyY": "helloWorld2"
          },
          "key5": null,
          "key6": null,
          "key7": {
            "keyX": "helloWorld1",
            "keyY": null
          },
          "key8": {
            "keyX": "helloWorld1",
            "keyY": null
          },
          "key9": 1,
          "key10": 1
        }
        """;
    JsonElement json = JsonParser.parseString(s);
    System.out.println(json);
    new IdentityHashMap<>();
  }
}
