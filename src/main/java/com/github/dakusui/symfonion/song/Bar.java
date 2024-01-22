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
  Fraction beats;
  Map<String, List<List<Pattern>>> patternLists = new HashMap<>();
  Groove groove;
  private final JsonObject barJsonObject;


  public Bar(JsonObject barJsonObject, JsonObject root, Map<String, Groove> grooves, Map<String, NoteMap> noteMaps, Map<String, Pattern> patterns) throws SymfonionException, JsonException {
    this.patterns = patterns;
    this.noteMaps = noteMaps;
    this.barJsonObject = barJsonObject;
    this.rootJsonObject = root;
    try (Context ignored = context($(JSON_ELEMENT_ROOT, root))) {
      try {
        this.beats = Fraction.parseFraction(JsonUtils.asString(barJsonObject, Keyword.$beats));
      } catch (FractionFormatException e) {
        throw illegalFormatException(asJsonElement(barJsonObject, Keyword.$beats), FRACTION_EXAMPLE);
      }
      this.beats = this.beats == null ? Fraction.one : this.beats;
      this.groove = Groove.DEFAULT_INSTANCE;
      Groove g = Groove.DEFAULT_INSTANCE;
      if (JsonUtils.hasPath(barJsonObject, Keyword.$groove)) {
        String grooveName = JsonUtils.asString(barJsonObject, Keyword.$groove.name());
        g = grooves.get(grooveName);
        if (g == null) {
          throw grooveNotDefinedException(asJsonElement(barJsonObject, Keyword.$groove), grooveName);
        }
      }
      this.groove = g;
      JsonObject patternsJsonObjectInBar = getPatternsInBarAsJsonObject(barJsonObject);
      for (Entry<String, JsonElement> patternEntryJsonElement : patternsJsonObjectInBar.entrySet()) {
        List<List<Pattern>> patternLists1 = new LinkedList<>();
        String partName = patternEntryJsonElement.getKey();
        JsonArray partPatternsJsonArray = getPatternsForPartJsonElements(partName, patternsJsonObjectInBar);
        int len = partPatternsJsonArray.size();
        for (int j = 0; j < len; j++) {
          JsonElement jsonPatterns = partPatternsJsonArray.get(j);
          String patternNames = jsonPatterns.getAsString();
          try (Context ignored2 = context($(REFERENCING_JSON_NODE, jsonPatterns))){
            patternLists1.add(createPattern(patternNames, jsonPatterns));
          }
        }
        this.patternLists.put(partName, patternLists1);
      }
    }
  }

  private static JsonArray getPatternsForPartJsonElements(String partName, JsonObject patternsJsonObjectInBar) {
    JsonArray partPatternsJsonArray = JsonUtils.asJsonArray(patternsJsonObjectInBar, partName);
    if (!partPatternsJsonArray.isJsonArray()) {
      throw typeMismatchException(partPatternsJsonArray, ARRAY);
    }
    return partPatternsJsonArray;
  }

  private static JsonObject getPatternsInBarAsJsonObject(JsonObject jsonObject) {
    JsonObject patternsJsonObjectInBar = JsonUtils.asJsonObject(jsonObject, Keyword.$patterns);
    if (patternsJsonObjectInBar == null) {
      throw requiredElementMissingException(jsonObject, Keyword.$patterns);
    }
    return patternsJsonObjectInBar;
  }

  private List<Pattern> createPattern(String patternNames, JsonElement jsonPatterns) {
    List<Pattern> patternList;
    if (patternNames.startsWith("$inline:")) {
      patternList = singletonList(Pattern.createPattern(JsonUtils.toJson(patternNames.substring("$inline:".length())).getAsJsonObject(), this.noteMaps));
    } else {
      patternList = new LinkedList<>();
      for (String eachPatternName : patternNames.split(";")) {
        Pattern cur;
        cur = this.patterns.get(eachPatternName);
        if (cur == null) {
          throw patternNotFound(jsonPatterns, patternNames);
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
      return asJsonElement(this.barJsonObject, Keyword.$patterns, partName);
    } catch (JsonInvalidPathException e) {
      return null;
    }
  }

  public JsonObject rootJsonObject() {
    return this.rootJsonObject;
  }
}
