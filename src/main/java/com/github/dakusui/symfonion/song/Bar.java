package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.exceptions.FractionFormatException;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.compat.json.CompatJsonException;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.utils.Fraction;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException.FRACTION_EXAMPLE;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionTypeMismatchException.OBJECT;
import static com.github.dakusui.symfonion.compat.json.CompatJsonUtils.asJsonElement;
import static com.github.dakusui.symfonion.compat.json.JsonUtils.findJsonArray;
import static com.github.dakusui.symfonion.compat.json.JsonUtils.path;
import static com.github.dakusui.symfonion.utils.Fraction.parseFraction;
import static com.github.valid8j.classic.Requires.requireNonNull;
import static java.util.Collections.*;

/**
 * // @formatter:off
 * A class that models a "bar" in a musical score.
 *
 * // @formatter:on
 */
public class Bar {
  private final Map<String, NoteMap>    noteMaps;
  private final Fraction                beats;
  private final Map<String, List<Pattern>> patterns;
  private final Groove                  groove;
  private final List<String>            labels;
  private final JsonObject              barJsonObject;
  private final JsonArray               partsJsonArray;


  /**
   * Creates a `Bar` object.
   *
   * // @formatter:off
   * `barJsonObject` stores content of the bar.
   *
   * [source, JSON]
   * .barJsonObject
   * ----
   * {
   *   "$beats": "<beatsDefiningString>",
   *   "$parts": [
   *     { "$name": "<partName>", "<inline pattern definition>" },
   *     ...
   *   ],
   *   "$groove": "<grooveName>",
   *   "$noteMap": "<noteMapName>",
   *   "$labels": ["<label1>", "<label2>", "..."]
   * }
   * ----
   *
   * `root` is used only for composing messages on errors.
   * // @formatter:on
   *
   * @param barJsonObject A JSON object from which a bar is created.
   * @param grooves       A map that defines a groove on which this bar should be played.
   * @param noteMaps      A note map with which this bar should be played.
   * @param partFilter    A predicate that filters parts to be played in this bar.
   * @throws SymfonionException  Data processing was failed.
   * @throws CompatJsonException Invalid JSON is given.
   */
  public Bar(JsonObject barJsonObject,
             Map<String, Groove> grooves,
             Map<String, NoteMap> noteMaps,
             Predicate<String> partFilter) throws SymfonionException,
                                                  CompatJsonException {
    this.noteMaps      = requireNonNull(noteMaps);
    this.barJsonObject = requireNonNull(barJsonObject);
    this.beats         = extractBeatsFractionFrom(barJsonObject);
    this.groove        = resolveGrooveForBar(barJsonObject, this.beats, grooves);
    this.labels        = resolveLabelsForBar(barJsonObject);
    this.partsJsonArray = getPartsInBarAsJsonArray(barJsonObject);
    this.patterns = composePatternsMap(this, partFilter, this.partsJsonArray);
  }

  /**
   * Returns labels attached to this bar object.
   *
   * @return A list of labels.
   */
  public List<String> labels() {
    return this.labels;
  }

  /**
   * Returns a set of part names.
   *
   * @return A set of part names.
   */
  public Set<String> partNames() {
    return unmodifiableSet(this.patterns.keySet());
  }

  /**
   * Returns the list of stacked `Pattern` objects for the given `partName`.
   * Multiple entries with the same `$name` in `$parts` are played simultaneously.
   *
   * @param partName A part name for which the patterns should be returned.
   * @return A list of `Pattern` objects (stacked).
   */
  public List<Pattern> patternsForPart(String partName) {
    return this.patterns.get(partName);
  }

  /**
   * Returns `Beats` that determine the duration of this object.
   *
   * @return Beats
   */
  public Fraction beats() {
    return this.beats;
  }

  /**
   * Returns a `Groove` object that determines "groove" of this bar
   *
   * @return A `Groove` object.
   * @see Groove
   */
  public Groove groove() {
    return this.groove;
  }

  /**
   * Find up a `JsonElement` in `$parts` array that has `$name` matching `partName`.
   * Returns the first matching element, or the `$parts` array itself if not found.
   *
   * @param partName partName used for searching.
   * @return A matching JSON element, or the array if not found.
   */
  public JsonElement lookUpJsonNode(String partName) {
    for (JsonElement elem : this.partsJsonArray) {
      if (elem.isJsonObject()) {
        JsonObject obj = elem.getAsJsonObject();
        if (obj.has(Keyword.$name.name()) && partName.equals(CompatJsonUtils.asString(obj, Keyword.$name))) {
          return elem;
        }
      }
    }
    return this.partsJsonArray;
  }

  /**
   * // @formatter:off
   * Composes a map from part name to list of stacked patterns.
   *
   * [source, JSON]
   * .partsJsonArrayInBar
   * ----
   * [
   *   { "$name": "<partName>", "<inline pattern definition>" },
   *   { "$name": "<partName>", "<inline pattern definition>" }
   * ]
   * ----
   * // @formatter:on
   *
   * @param bar                 A bar object from which the pattern map is created.
   * @param partFilter          A predicate that filters a part to be rendered.
   * @param partsJsonArrayInBar A JSON array associated with `$parts` key in the bar JSON object.
   * @return A map from part name to a list of patterns (stacked).
   */
  private static Map<String, List<Pattern>> composePatternsMap(Bar bar,
                                                               Predicate<String> partFilter,
                                                               JsonArray partsJsonArrayInBar) {
    Map<String, List<Pattern>> ret = new LinkedHashMap<>();
    for (JsonElement elem : partsJsonArrayInBar) {
      if (!elem.isJsonObject()) {
        throw typeMismatchException(elem, OBJECT);
      }
      JsonObject partObj  = elem.getAsJsonObject();
      String     partName = CompatJsonUtils.asString(partObj, Keyword.$name);
      if (!partFilter.test(partName))
        continue;
      ret.computeIfAbsent(partName, k -> new ArrayList<>())
         .add(Pattern.createPattern(partObj, bar.noteMaps));
    }
    return ret;
  }

  static List<String> resolveLabelsForBar(JsonObject barJsonObject) {
    if (CompatJsonUtils.hasPath(barJsonObject, Keyword.$labels)) {
      return StreamSupport.stream(CompatJsonUtils.asJsonArray(barJsonObject, Keyword.$labels).spliterator(), false)
                          .map(CompatJsonUtils::asString)
                          .toList();
    }
    return emptyList();
  }

  private static Groove resolveGrooveForBar(JsonObject barJsonObject, Fraction barLength, Map<String, Groove> grooves) {
    Groove g = Groove.defaultGrooveOf(barLength);
    if (CompatJsonUtils.hasPath(barJsonObject, Keyword.$groove)) {
      String grooveName = CompatJsonUtils.asString(barJsonObject, Keyword.$groove.name());
      g = grooves.get(grooveName);
      if (g == null) {
        throw grooveNotDefinedException(asJsonElement(barJsonObject, Keyword.$groove), grooveName);
      }
    }
    return g;
  }

  static Fraction extractBeatsFractionFrom(JsonObject barJsonObject) {
    Fraction beats;
    try {
      beats = parseFraction(CompatJsonUtils.asString(barJsonObject, Keyword.$beats));
    } catch (FractionFormatException e) {
      throw illegalFormatException(asJsonElement(barJsonObject, Keyword.$beats), FRACTION_EXAMPLE);
    }
    beats = beats == null ? Fraction.ONE : beats;
    return beats;
  }

  /**
   * @param barJsonObject A JSON object that models a bar in a musical score.
   * @return JSON array under `$parts` element
   */
  private static JsonArray getPartsInBarAsJsonArray(JsonObject barJsonObject) {
    return findJsonArray(barJsonObject, path(Keyword.$parts)).orElseThrow(() -> requiredElementMissingException(barJsonObject,
                                                                                                               Keyword.$parts));
  }

}
