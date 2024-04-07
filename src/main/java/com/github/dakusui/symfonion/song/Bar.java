package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.CompatJsonException;
import com.github.dakusui.json.JsonInvalidPathException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.symfonion.compat.exceptions.FractionFormatException;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.utils.Fraction;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.github.dakusui.json.JsonUtils.asJsonElement;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.JSON_ELEMENT_ROOT;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.REFERENCING_JSON_NODE;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException.FRACTION_EXAMPLE;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionTypeMismatchException.ARRAY;
import static java.util.Collections.singletonList;

public class Bar {
  private final Map<String, Pattern> patterns;
  private final JsonObject rootJsonObject;
  private final Map<String, NoteMap> noteMaps;
  final Fraction beats;
  final Map<String, List<List<Pattern>>> patternLists = new HashMap<>();
  final Groove groove;
  final List<String> labels;
  private final JsonObject barJsonObject;


  /**
   * Creates a `Bar` object.
   *
   * @param barJsonObject A JSON object from which a bar is created.
   * @param root A root JSON object that `barJsonObject` belongs to.
   * @param grooves A map that defines a groove on which this bar should be played.
   * @param noteMaps A note map with which this bar should be played.
   * @param patterns A map that holds that defines patterns this bar references.
   * @param partFilter A predicate that filters parts to be played in this bar.
   * @throws SymfonionException Data processing was failed.
   * @throws CompatJsonException Invalid JSON is given.
   */
  public Bar(JsonObject barJsonObject, JsonObject root, Map<String, Groove> grooves, Map<String, NoteMap> noteMaps, Map<String, Pattern> patterns, Predicate<String> partFilter) throws SymfonionException, CompatJsonException {
    this.patterns = patterns;
    this.noteMaps = noteMaps;
    this.barJsonObject = barJsonObject;
    this.rootJsonObject = root;
    try (Context ignored = context($(JSON_ELEMENT_ROOT, root))) {
      this.beats = resolveBeatsForBar(barJsonObject);
      this.groove = resolveGrooveForBar(barJsonObject, grooves);
      this.labels = resolveLabelsForBar(barJsonObject);
      /*
        partName -> [ patternName ]
       */
      JsonObject patternsJsonObjectInBar = getPartsInBarAsJsonObject(barJsonObject);
      for (Entry<String, JsonElement> patternEntryJsonElement : patternsJsonObjectInBar.entrySet()) {
        if (!partFilter.test(patternEntryJsonElement.getKey()))
          continue;
        List<List<Pattern>> patternLists = new LinkedList<>();
        String partName = patternEntryJsonElement.getKey();
        JsonArray partPatternsJsonArray = getPatternsForPartJsonElements(partName, patternsJsonObjectInBar);
        for (JsonElement jsonPatterns : partPatternsJsonArray) {
          String patternName = jsonPatterns.getAsString();
          try (Context ignored2 = context($(REFERENCING_JSON_NODE, jsonPatterns))) {
            patternLists.add(createPattern(patternName));
          }
        }
        this.patternLists.put(partName, patternLists);
      }
    }
  }

  private List<String> resolveLabelsForBar(JsonObject barJsonObject) {
    if (JsonUtils.hasPath(barJsonObject, Keyword.$labels)) {
      return StreamSupport.stream(JsonUtils.asJsonArray(barJsonObject, Keyword.$labels).spliterator(), false).map(JsonUtils::asString).toList();
    }
    return Collections.emptyList();
  }

  private static Groove resolveGrooveForBar(JsonObject barJsonObject, Map<String, Groove> grooves) {
    Groove g = Groove.DEFAULT_INSTANCE;
    if (JsonUtils.hasPath(barJsonObject, Keyword.$groove)) {
      String grooveName = JsonUtils.asString(barJsonObject, Keyword.$groove.name());
      g = grooves.get(grooveName);
      if (g == null) {
        throw grooveNotDefinedException(asJsonElement(barJsonObject, Keyword.$groove), grooveName);
      }
    }
    return g;
  }

  private Fraction resolveBeatsForBar(JsonObject barJsonObject) {
    Fraction beats;
    try {
      beats = Fraction.parseFraction(JsonUtils.asString(barJsonObject, Keyword.$beats));
    } catch (FractionFormatException e) {
      throw illegalFormatException(asJsonElement(barJsonObject, Keyword.$beats), FRACTION_EXAMPLE);
    }
    beats = beats == null ? Fraction.one : beats;
    return beats;
  }

  private static JsonArray getPatternsForPartJsonElements(String partName, JsonObject patternsJsonObjectInBar) {
    JsonArray partPatternsJsonArray = JsonUtils.asJsonArray(patternsJsonObjectInBar, partName);
    if (!partPatternsJsonArray.isJsonArray()) {
      throw typeMismatchException(partPatternsJsonArray, ARRAY);
    }
    return partPatternsJsonArray;
  }

  private static JsonObject getPartsInBarAsJsonObject(JsonObject jsonObject) {
    JsonObject patternsJsonObjectInBar = JsonUtils.asJsonObject(jsonObject, Keyword.$parts);
    if (patternsJsonObjectInBar == null) {
      throw requiredElementMissingException(jsonObject, Keyword.$parts);
    }
    return patternsJsonObjectInBar;
  }

  /**
   * Creates a list of patterns from a string that holds pattern names.
   * In the `patternNames` variable name pattern names are joined by `;`.
   *
   * @param patternNames A string that holds pattern names.
   * @return
   */
  private List<Pattern> createPattern(String patternNames) {
    List<Pattern> patternList;
    if (patternNames.startsWith("$inline:")) {
      patternList = singletonList(Pattern.createPattern(JsonUtils.toJson(patternNames.substring("$inline:".length())).getAsJsonObject(), this.noteMaps));
    } else {
      patternList = new LinkedList<>();
      for (String eachPatternName : patternNames.split(";")) {
        Pattern cur;
        cur = this.patterns.get(eachPatternName);
        if (cur == null) {
          throw patternNotFound(patternNames);
        }
        patternList.add(cur);
      }
    }
    return patternList;
  }

  /**
   * Returns a set of part names.
   * @return A set of part names.
   */
  public Set<String> partNames() {
    return Collections.unmodifiableSet(this.patternLists.keySet());
  }

  /**
   * Returns a list of a list of patterns for the `partName`.
   * TODO
   *
   * @param partName A part name for which patterns should be returned.
   * @return A list of a list of patterns.
   */
  public List<List<Pattern>> part(String partName) {
    return Collections.unmodifiableList(this.patternLists.get(partName));
  }

  public Fraction beats() {
    return this.beats;
  }

  public Groove groove() {
    return this.groove;
  }

  public JsonElement lookUpJsonNode(String partName) {
    try {
      return asJsonElement(this.barJsonObject, Keyword.$parts, partName);
    } catch (JsonInvalidPathException e) {
      return null;
    }
  }

  /**
   * Returns a root JSON object to which this bar belongs.
   *
   * @return A root JSON object.
   */
  public JsonObject rootJsonObject() {
    return this.rootJsonObject;
  }

  /**
   * Returns labels attached to this bar object.
   *
   * @return A list of labels.
   */
  public List<String> labels() {
    return this.labels;
  }
}
