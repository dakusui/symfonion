package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.json.JsonInvalidPathException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.symfonion.utils.*;
import com.github.dakusui.symfonion.exceptions.FractionFormatException;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.exceptions.SymfonionIllegalFormatException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.Map.Entry;

import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.*;
import static com.github.dakusui.symfonion.exceptions.SymfonionTypeMismatchException.ARRAY;

public class Bar {
  private final Map<String, Groove> grooves;
  private final Map<String, Pattern> patterns;
  private final JsonObject rootJsonObject;
  Fraction beats;
  Map<String, List<List<Pattern>>> patternLists = new HashMap<String, List<List<Pattern>>>();
  Groove groove;
  private JsonObject json = null;
  
  
  public Bar(JsonObject jsonObject, JsonObject root, Map<String, Groove> grooves, Map<String, Pattern> patterns) throws SymfonionException, JsonException {
    this.grooves = grooves;
    this.patterns = patterns;
    this.json = jsonObject;
    this.rootJsonObject = root;
    init(jsonObject, root);
  }
  
  private void init(JsonObject jsonObject, JsonObject root) throws SymfonionException, JsonException {
    try {
      this.beats = Utils.parseFraction(JsonUtils.asString(jsonObject, Keyword.$beats));
    } catch (FractionFormatException e) {
      throw illegalFormatException(
          JsonUtils.asJsonElement(jsonObject, Keyword.$beats),
          root,
          SymfonionIllegalFormatException.FRACTION_EXAMPLE);
    }
    this.beats = this.beats == null ? Fraction.one : this.beats;
    this.groove = Groove.DEFAULT_INSTANCE;
    Groove g = Groove.DEFAULT_INSTANCE;
    if (JsonUtils.hasPath(jsonObject, Keyword.$groove)) {
      String grooveName = JsonUtils.asString(jsonObject, Keyword.$groove.name());
      g = grooves.get(grooveName);
      if (g == null) {
        throw grooveNotDefinedException(JsonUtils.asJsonElement(jsonObject, Keyword.$groove), root, grooveName);
      }
    }
    this.groove = g;
    JsonObject patternsJsonObject = JsonUtils.asJsonObject(jsonObject, Keyword.$patterns);
    if (patternsJsonObject == null) {
      throw requiredElementMissingException(jsonObject, json, Keyword.$patterns);
    }
    for (Entry<String, JsonElement> stringJsonElementEntry : patternsJsonObject.entrySet()) {
      String partName = stringJsonElementEntry.getKey();
      List<List<Pattern>> patterns = new LinkedList<List<Pattern>>();
      JsonArray partPatternsJsonArray = JsonUtils.asJsonArray(patternsJsonObject, partName);
      if (!partPatternsJsonArray.isJsonArray()) {
        throw typeMismatchException(partPatternsJsonArray, json, ARRAY);
      }
      int len = partPatternsJsonArray.size();
      for (int j = 0; j < len; j++) {
        JsonElement jsonPatterns = partPatternsJsonArray.get(j);
        String patternNames = jsonPatterns.getAsString();
        List<Pattern> p = new LinkedList<Pattern>();
        for (String each : patternNames.split(";")) {
          Pattern cur = this.patterns.get(each);
          if (cur == null) {
            throw patternNotFound(jsonPatterns, this.rootJsonObject(), patternNames);
          }
          p.add(cur);
        }
        patterns.add(p);
      }
      patternLists.put(partName, patterns);
    }
  }
  
  public Set<String> partNames() {
    return Collections.unmodifiableSet(this.patternLists.keySet());
  }
  
  public List<List<Pattern>> part(String instrumentName) {
    return Collections.unmodifiableList(this.patternLists.get(instrumentName));
  }
  
  public Fraction beats() {
    return this.beats;
  }
  
  public Groove groove() {
    return this.groove;
  }
  
  public JsonElement lookUpJsonNode(String partName) {
    try {
      return JsonUtils.asJsonElement(this.json, Keyword.$patterns, partName);
    } catch (JsonInvalidPathException e) {
      return null;
    }
  }
  
  public JsonObject rootJsonObject() {
    return this.rootJsonObject;
  }
}
