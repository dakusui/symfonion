package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.utils.Fraction;
import com.google.gson.JsonObject;

import java.util.Map;

public class Measure {
  private final Groove              groove;
  private final Fraction            beats;
  private final Map<String, String> noteMapNames;

  /**
   * // @formatter:off
   * [source, JSON]
   * .measureJsonObject
   * ----
   * {
   *   "$beats": "<beatsDefiningString>",
   *   "$parts": [
   *     {
   *         "$partName": "<partName1>",
   *         "$notes": "<strokeSequence>",
   *         "$velocityBase": "<number>",
   *         "$reverb": ["<number>", "...", "<number>"],
   *         "...": "..."
   *     },
   *     {
   *         "...": "..."
   *     },
   *     "..."
   *   ],
   *   "$groove": [ {"$length": "1/4", "$ticks": 384, "$accent": 96 }, "..."],
   *   "$noteMap": {
   *      "<partName1>": "<noteMapName1>",
   *      "<partName2>": "<noteMapName2>",
   *      "<partName3>": "<noteMapName3>"
   *   }
   *   "$labels": ["<label1>", "<label2>", "..."]
   * }
   * ----
   * // @formatter:on
   */
  public Measure(JsonObject measureJsonObject) {
    this.groove       = resolveGroove(measureJsonObject);
    this.beats        = resolveBeats(measureJsonObject);
    this.noteMapNames = resolveNoteMapNames(measureJsonObject);
  }

  private static Map<String, String> resolveNoteMapNames(JsonObject measureJsonObject) {
    throw new RuntimeException();
  }

  private static Fraction resolveBeats(JsonObject measureJsonObject) {
    throw new RuntimeException();
  }

  private static Groove resolveGroove(JsonObject measureJsonObject) {
    throw new RuntimeException();
  }
}
