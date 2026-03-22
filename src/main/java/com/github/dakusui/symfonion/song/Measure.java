package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.utils.Fraction;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.dakusui.symfonion.compat.json.CompatJsonUtils.asJsonArray;
import static com.github.dakusui.symfonion.compat.json.CompatJsonUtils.asJsonObject;
import static com.github.dakusui.symfonion.song.Bar.extractBeatsFractionFrom;
import static com.github.dakusui.symfonion.song.Bar.resolveLabelsForBar;
import static com.github.dakusui.symfonion.song.Keyword.$parts;
import static com.github.valid8j.fluent.Expectations.precondition;
import static com.github.valid8j.fluent.Expectations.value;

public class Measure {
  private final Groove                   groove;
  private final Fraction                 beats;
  private final List<String>             activePartNames;
  private final Map<String, PartMeasure> partMeasures;
  private final List<String>             labels;

  /**
   * // @formatter:off
   * [source, JSON]
   * .measureJsonObject
   * ----
   * {
   *   "$beats": "<beatsDefiningString>",
   *   "$parts": [
   *     {
   *         "$name": "<partName1>",
   *         "$notes": "<strokeSequence1>",
   *         "$reverb": ["<number>", "...", "<number>"],
   *         "<otherArrayableControls>": ["..."]
   *     },
   *     {
   *         "$name": "<partName2>",
   *         "...": "..."
   *     }
   *   ],
   *   "$groove": "<groove:array@[object:grooveUnit]>",
   *   "$labels": ["<label1>", "<label2>", "..."]
   * }
   * ----
   * // @formatter:on
   */
  public Measure(JsonObject measureJsonObject, Map<String, NoteMap> noteMaps, Predicate<String> partFilter) {
    this.beats           = extractBeatsFractionFrom(measureJsonObject);
    this.groove          = composeGroove(measureJsonObject).orElse(Groove.defaultGrooveOf(this.beats));
    this.activePartNames = resolvePartNames(measureJsonObject);
    this.partMeasures    = composePartMeasures(measureJsonObject, this.activePartNames, noteMaps, partFilter);
    this.labels          = resolveLabelsForBar(measureJsonObject);
  }

  public Groove groove() {
    return this.groove;
  }

  public List<String> activePartNames() {
    return this.activePartNames;
  }

  public Fraction beats() {
    return this.beats;
  }

  public List<String> labels() {
    return this.labels;
  }

  public PartMeasure partMeasureFor(String partName) {
    assert precondition(value(this.partMeasures).invoke("containsKey", partName)
                                                .asBoolean()
                                                .toBe()
                                                .trueValue());
    return this.partMeasures.get(partName);
  }

  private static List<String> resolvePartNames(JsonObject measureJsonObject) {
    assert precondition(value(measureJsonObject).toBe().notNull());
    JsonArray partsArray = asJsonArray(measureJsonObject, $parts);
    List<String> names = new ArrayList<>();
    for (JsonElement elem : partsArray) {
      if (elem.isJsonObject()) {
        String name = CompatJsonUtils.asString(elem.getAsJsonObject(), Keyword.$name);
        if (!names.contains(name)) names.add(name);
      }
    }
    return List.copyOf(names);
  }

  private static Map<String, PartMeasure> composePartMeasures(JsonObject measureJsonObject, List<String> partNames, Map<String, NoteMap> noteMaps, Predicate<String> partFilter) {
    JsonArray partsArray = asJsonArray(measureJsonObject, $parts);
    Map<String, PartMeasure> partMeasures = new HashMap<>();
    for (JsonElement elem : partsArray) {
      if (!elem.isJsonObject()) continue;
      JsonObject partObj  = elem.getAsJsonObject();
      String     partName = CompatJsonUtils.asString(partObj, Keyword.$name);
      if (!partFilter.test(partName)) continue;
      if (!partMeasures.containsKey(partName)) {
        partMeasures.put(partName, composePartMeasure(partObj, noteMaps.getOrDefault(partName, NoteMap.defaultNoteMap)));
      }
    }
    return partMeasures;
  }

  private static PartMeasure composePartMeasure(JsonObject partMeasureJsonObject, NoteMap noteMap) {
    return new PartMeasure(partMeasureJsonObject, composePartMeasureParameters(partMeasureJsonObject, noteMap));
  }

  private static PartMeasureParameters composePartMeasureParameters(JsonObject partMeasureJsonObject, NoteMap noteMap) {
    return new PartMeasureParameters(asJsonObject(partMeasureJsonObject, "$parameters"), noteMap);
  }

  private static Optional<Groove> composeGroove(JsonObject measureJsonObject) {
    if (CompatJsonUtils.hasPath(measureJsonObject, Keyword.$groove))
      return Optional.of(Groove.createGroove(CompatJsonUtils.asJsonArray(measureJsonObject, Keyword.$groove)));
    return Optional.empty();
  }
}
