package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.exceptions.ExceptionContext;
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

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.REFERENCING_JSON_NODE;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.ExceptionContext.entry;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException.FRACTION_EXAMPLE;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionTypeMismatchException.ARRAY;
import static com.github.dakusui.symfonion.compat.json.CompatJsonUtils.asJsonElement;
import static com.github.dakusui.symfonion.utils.Fraction.parseFraction;
import static java.util.Collections.*;

/**
 * // @formatter:off
 * A class that models a "bar" in a musical score.
 *
 * // @formatter:on
 */
public class Bar {
  private final Map<String, Pattern>               patterns;
  private final Map<String, NoteMap>               noteMaps;
  private final Fraction                           beats;
  private final Map<String, List<PatternSequence>> patternSequencePiles;
  private final Groove                             groove;
  private final List<String>                       labels;
  private final JsonObject                         barJsonObject;


  /**
   * Creates a `Bar` object.
   *
   * `barJsonObject` stores content of the bar.
   *
   * [source, JSON]
   * .barJsonObject
   * ----
   * {
   * "$beats": "<beatsDefiningString>",
   * "$parts": {
   * "<partName>": ["<patternName>;<patternName>",
   * "<patternName>",
   * "$inline:<inlined pattern>",
   * "..."],
   * },
   * "$groove": "<grooveName>",
   * "$noteMap": "<noteMapName>",
   * "$labels": ["<label1>", "<label2>", "..."]
   * }
   * ----
   *
   * `root` is used only for composing messages on errors.
   *
   * @param barJsonObject A JSON object from which a bar is created.
   * @param grooves       A map that defines a groove on which this bar should be played.
   * @param noteMaps      A note map with which this bar should be played.
   * @param patterns      A map that holds that defines patterns this bar references.
   * @param partFilter    A predicate that filters parts to be played in this bar.
   * @throws SymfonionException  Data processing was failed.
   * @throws CompatJsonException Invalid JSON is given.
   */
  public Bar(JsonObject barJsonObject,
             Map<String, Groove> grooves,
             Map<String, NoteMap> noteMaps,
             Map<String, Pattern> patterns,
             Predicate<String> partFilter) throws SymfonionException,
                                                  CompatJsonException {
    this.patterns      = patterns;
    this.noteMaps      = noteMaps;
    this.barJsonObject = barJsonObject;
    this.beats         = resolveBeatsForBar(barJsonObject);
    this.groove        = resolveGrooveForBar(barJsonObject, this.beats, grooves);
    this.labels        = resolveLabelsForBar(barJsonObject);
      /*
        partName -> [ patternName ]
       */
    this.patternSequencePiles = composePatternSequencesMap(this,
                                                           partFilter,
                                                           getPartsInBarAsJsonObject(barJsonObject));
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
    return unmodifiableSet(this.patternSequencePiles.keySet());
  }

  /**
   * Returns a list of pattern sequences (`PatternSequence`) for the `partName`.
   *
   * @param partName A part name for which patterns should be returned.
   * @return A list of pattern sequences.
   */
  public List<PatternSequence> patternSequencePileForPart(String partName) {
    return this.patternSequencePiles.get(partName)
                                    .stream()
                                    .map(PatternSequence::create)
                                    .toList();
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
   * Find up a `JsonElement`, which matches `partName` under `barJsonObject`.
   *
   * @param partName partName used for searching for `JsonElement`.
   * @return A matching JSON element. If not found, `null` will be returned.
   * @see Bar#Bar(JsonObject, Map, Map, Map, Predicate, JsonObject)
   */
  public JsonElement lookUpJsonNode(String partName) {
    return asJsonElement(this.barJsonObject, Keyword.$parts, partName);
  }

  /**
   * // @formatter:off
   * Composes a map of pattern sequences.
   *
   * ----
   * Map<String, List<List<Pattern>>>
   * ^^^^^^       ^^^^^^^^^^^^^   Layered Patterns
   * |  ^^^^^^^^^^^^^^^^^^^  A sequence of layered patterns
   * +---------------------  A part name
   * ----
   *
   * [source, JSON]
   * .partsJsonObjectInBar
   * ----
   * {
   *   "<partName>": [
   *     "patternName;patternName",
   *     "$inline:patternDefinition",
   *     "..."
   *    ]
   * }
   * ----
   * // @formatter:on
   *
   * @param bar                  A bar object from which pattern sequence map is created.
   * @param partFilter           A predicate that filters a part to be rendered.
   * @param partsJsonObjectInBar A JSON object associated with `$parts` key in the JSON object from which `bar` was created.
   * @return A map from part name to a list of pattern sequences.
   */
  private static Map<String, List<PatternSequence>> composePatternSequencesMap(Bar bar,
                                                                               Predicate<String> partFilter,
                                                                               JsonObject partsJsonObjectInBar) {
    Map<String, List<PatternSequence>> ret = new HashMap<>();
    for (String eachPartName : partsJsonObjectInBar.keySet()) {
      if (!partFilter.test(eachPartName))
        continue;
      ret.put(eachPartName,
              composePatternSequencePile(bar, patternSequenceJsonArrayForPart(eachPartName,
                                                                              partsJsonObjectInBar)));
    }
    return ret;
  }

  private static JsonArray patternSequenceJsonArrayForPart(String partName,
                                                           JsonObject patternsJsonObjectInBar) {
    JsonArray partPatternsJsonArray = CompatJsonUtils.asJsonArray(patternsJsonObjectInBar, partName);
    if (!partPatternsJsonArray.isJsonArray()) {
      throw typeMismatchException(partPatternsJsonArray, ARRAY);
    }
    return partPatternsJsonArray;
  }

  private static List<PatternSequence> composePatternSequencePile(Bar bar,
                                                                  JsonArray patternSequenceSequenceJsonArray) {
    final List<PatternSequence> patternSequenceSequence = new LinkedList<>();
    for (JsonElement patternSequenceJsonElement : patternSequenceSequenceJsonArray) {
      String sequencedPatternNames = patternSequenceJsonElement.getAsString();
      try (ExceptionContext ignored = exceptionContext(entry(REFERENCING_JSON_NODE, patternSequenceJsonElement))) {
        patternSequenceSequence.add(Bar.createPatternPile(sequencedPatternNames, bar.patterns, bar.noteMaps));
      }
    }
    return patternSequenceSequence;
  }

  private static List<String> resolveLabelsForBar(JsonObject barJsonObject) {
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

  private static Fraction resolveBeatsForBar(JsonObject barJsonObject) {
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
   * @return JSON object under `$parts` element
   */
  private static JsonObject getPartsInBarAsJsonObject(JsonObject barJsonObject) {
    JsonObject patternsJsonObjectInBar = CompatJsonUtils.asJsonObject(barJsonObject, Keyword.$parts);
    if (patternsJsonObjectInBar == null) {
      throw requiredElementMissingException(barJsonObject, Keyword.$parts);
    }
    return patternsJsonObjectInBar;
  }

  /**
   * Creates a list of patterns from a string that holds pattern names.
   * In the `patternNames` variable name pattern names are joined by `;`.
   *
   * @param patternNames A string that holds pattern names.
   * @param patterns     A map from a pattern name to a pattern.
   * @param noteMaps     A map from a note map name to a note map.
   * @return A pattern sequence created for pattern names.
   */
  private static PatternSequence createPatternPile(String patternNames, Map<String, Pattern> patterns, Map<String, NoteMap> noteMaps) {
    List<Pattern> patternList;
    if (patternNames.startsWith("$inline:")) {
      patternList = singletonList(Pattern.createPattern(CompatJsonUtils.toJson(patternNames.substring("$inline:".length())).getAsJsonObject(), noteMaps));
    } else {
      patternList = new LinkedList<>();
      for (String eachPatternName : patternNames.split(";")) {
        patternList.add(patternNameToPattern(patternNames, patterns, eachPatternName));
      }
    }
    return PatternSequence.create(patternList);
  }

  private static Pattern patternNameToPattern(String patternNames, Map<String, Pattern> patterns, String eachPatternName) {
    Pattern cur;
    cur = patterns.get(eachPatternName);
    if (cur == null) {
      throw patternNotFound(patternNames);
    }
    return cur;
  }
}
