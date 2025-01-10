package com.github.dakusui.symfonion.song;

import com.google.gson.JsonObject;

public class Measure {
  /**
   * // @formatter:off
   * [source, JSON]
   * .measureJsonObject
   * ----
   * {
   *   "$beats": "<beatsDefiningString>",
   *   "$parts": {
   *     "<partName1>": {
   *         "$notes": "<strokeSequence>",
   *         "$velocityBase": "<number>",
   *         "$reverb": ["<number>", "...", "<number>"],
   *         "...": "..."
   *     },
   *     "<partName2>": "<partMeasure>",
   *     "<partName3>": "<partMeasure>",
   *     "...": "..."
   *   },
   *   "$groove": [{"$length": "1/4", "$ticks": 384, "$accent": 96 }, "..."],
   *   "$noteMap": {
   *      "<partName1>": "<noteMap1>",
   *      "<partName2>": "<noteMap2>",
   *      "<partName3>": "<noteMap3>"
   *   }
   *   "$labels": ["<label1>", "<label2>", "..."]
   * }
   * ----
   * // @formatter:on
   */
  public Measure(JsonObject measureJsonObject) {

  }
}
