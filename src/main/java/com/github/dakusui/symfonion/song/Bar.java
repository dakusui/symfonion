package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.json.JsonInvalidPathException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.symfonion.exceptions.FractionFormatException;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.utils.Fraction;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.github.dakusui.json.JsonUtils.asJsonElement;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.*;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.ContextKey.JSON_ELEMENT_ROOT;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.ContextKey.REFERENCING_JSON_NODE;
import static com.github.dakusui.symfonion.exceptions.SymfonionIllegalFormatException.FRACTION_EXAMPLE;
import static com.github.dakusui.symfonion.exceptions.SymfonionTypeMismatchException.ARRAY;
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


  public Bar(JsonObject barJsonObject, JsonObject root, Map<String, Groove> grooves, Map<String, NoteMap> noteMaps, Map<String, Pattern> patterns, Predicate<String> partFilter) throws SymfonionException, JsonException {
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

  public Set<String> partNames() {
    return Collections.unmodifiableSet(this.patternLists.keySet());
  }

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

  public JsonObject rootJsonObject() {
    return this.rootJsonObject;
  }

  public List<String> labels() {
    return this.labels;
  }
}
