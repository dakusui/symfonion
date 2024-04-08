package com.github.dakusui.symfonion.tests.json;

import com.github.dakusui.symfonion.compat.json.JsonSummarizer;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.testutils.TestBase;
import com.github.dakusui.thincrest_cliche.core.AllOf;
import com.github.dakusui.thincrest_cliche.core.Transform;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.util.List;

import static com.github.dakusui.thincrest_cliche.gson.JsonElementTo.jsonArrayAt;
import static com.github.dakusui.thincrest_cliche.gson.JsonElementTo.jsonObjectAt;
import static com.github.dakusui.testutils.json.JsonTestUtils.*;
import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.isInstanceOf;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.isNotNull;
import static java.util.Arrays.asList;

public class JsonSummarizerTest extends TestBase {
  private static JsonObject createTestDataJsonObject1() {
    return object(
        $("hello1", object($("key11", json("world")))),
        $("hello2", object($("key21", json("world")))),
        $("hello3", object($("key31", json("world")))),
        $("hello4", object($("key41", json("world")))),
        $("hello5", object($("key51", json("world")),
            $("key52", object(
                $("key521", json("ABC")),
                $("key522", json("world")),
                $("key523", array(json("XYZ"))),
                $("key524", array()),
                $("key525", object(
                    $("key5231", json("XYZ")))),
                $("key526", object()))),
            $("key53", array(
                json("ABC"),
                json("world"),
                array(json("XYZ")), array(), object(
                    $("key5231", json("XYZ"))), object())))),
        $("hello6", object(
            $("key61", json("world")))),
        $("hello7", object($("key71", json("world")))),
        $("hello8", object($("key81", json("world")))),
        $("hello9", object($("key91", json("world")))),
        $("helloA", object($("keyA1", json("world")))));
  }

  private static JsonObject createTestDataJsonObject2() {
    return object(
        $("k1", object(
            $("k11", array(
                "e111",
                object(
                    $("k1121", json("value1121")),
                    $("k1122", json("value1122"))),
                "e113")))),
        $("k2", object()));
  }

  @Test
  public void whenCreateSummaryJsonObjectFromPaths_thenSummaryCreated() {
    JsonObject summarizedValue = CompatJsonUtils.createSummaryJsonObjectFromPaths(
        createTestDataJsonObject2(), asList("k1", "k11"));
    System.out.println(prettyPrintJsonElement(summarizedValue));
  }

  @Test
  public void whenSummaryObject_thenItLooksNice() {
    JsonObject testDataJsonObject = createTestDataJsonObject1();
    JsonObject objectSummary = JsonSummarizer.summaryObject(testDataJsonObject, List.of("hello5"), "key52").getAsJsonObject();
    System.out.println(prettyPrintJsonElement(objectSummary));

    assertThat(
        objectSummary,
        AllOf.$(
            isNotNull(),
            Transform.$(jsonObjectAt(jsonpath("hello5", "key52"))).allOf(
                isNotNull(),
                isInstanceOf(JsonObject.class))));
  }

  @Test
  public void whenSummaryArray_thenItLooksNice() {
    JsonObject testDataJsonObject = createTestDataJsonObject1();

    JsonElement arraySummary = JsonSummarizer.summaryObject(testDataJsonObject, List.of("hello5"), "key53");
    System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(arraySummary));

    assertThat(
        arraySummary,
        AllOf.$(
            isNotNull(),
            Transform.$(jsonArrayAt(jsonpath("hello5", "key53"))).allOf(
                isNotNull(),
                isInstanceOf(JsonArray.class))));
  }
}
