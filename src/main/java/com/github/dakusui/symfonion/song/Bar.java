package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.json.JsonInvalidPathException;
import com.github.dakusui.json.JsonPathNotFoundException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.symfonion.core.*;
import com.github.dakusui.symfonion.core.exceptions.FractionFormatException;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.core.exceptions.SymfonionIllegalFormatException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.Map.Entry;

import static com.github.dakusui.symfonion.core.exceptions.ExceptionThrower.*;
import static com.github.dakusui.symfonion.core.exceptions.SymfonionTypeMismatchException.ARRAY;

public class Bar {
  Fraction beats;
  Map<String, List<List<Pattern>>> patternLists = new HashMap<String, List<List<Pattern>>>();
  Groove groove;
  private Song song;
  private JsonObject json = null;


  public Bar(JsonObject jsonObject, Song song) throws SymfonionException, JsonException {
    this.song = song;
    this.json = jsonObject;
    init(jsonObject);
  }

  private void init(JsonObject jsonObject) throws SymfonionException, JsonException {
    try {
      this.beats = Utils.parseFraction(JsonUtils.asString(jsonObject, Keyword.$beats));
    } catch (FractionFormatException e) {
      throw illegalFormatException(
          JsonUtils.asJsonElement(jsonObject, Keyword.$beats),
          SymfonionIllegalFormatException.FRACTION_EXAMPLE);
    }
    this.beats = this.beats == null ? Fraction.one : this.beats;
    this.groove = Groove.DEFAULT_INSTANCE;
    Groove g = Groove.DEFAULT_INSTANCE;
    if (JsonUtils.hasPath(jsonObject, Keyword.$groove)) {
      String grooveName = JsonUtils.asString(jsonObject, Keyword.$groove.name());
      g = song.groove(grooveName);
      if (g == null) {
        throw grooveNotDefinedException(
            JsonUtils.asJsonElement(jsonObject, Keyword.$groove),
            grooveName
        );
      }
    }
    this.groove = g;
    JsonObject patternsJsonObject = JsonUtils.asJsonObject(jsonObject, Keyword.$patterns);
    if (patternsJsonObject == null) {
      throw requiredElementMissingException(jsonObject, Keyword.$patterns);
    }
    for (Entry<String, JsonElement> stringJsonElementEntry : patternsJsonObject.entrySet()) {
      String partName = stringJsonElementEntry.getKey();
      List<List<Pattern>> patterns = new LinkedList<List<Pattern>>();
      JsonArray partPatternsJsonArray = JsonUtils.asJsonArray(patternsJsonObject, partName);
      if (!partPatternsJsonArray.isJsonArray()) {
        throw typeMismatchException(partPatternsJsonArray, ARRAY);
      }
      int len = partPatternsJsonArray.size();
      for (int j = 0; j < len; j++) {
        JsonElement jsonPatterns = partPatternsJsonArray.get(j);
        String patternNames = jsonPatterns.getAsString();
        List<Pattern> p = new LinkedList<Pattern>();
        for (String each : patternNames.split(";")) {
          Pattern cur = song.pattern(each);
          if (cur == null) {
            throw patternNotFound(jsonPatterns, patternNames);
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

  public JsonElement location(String partName) {
    try {
      return JsonUtils.asJsonElement(this.json, Keyword.$patterns, partName);
    } catch (JsonPathNotFoundException e) {
      return null;
    } catch (JsonInvalidPathException e) {
      return null;
    }
  }
}
