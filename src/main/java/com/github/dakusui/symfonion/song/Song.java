package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.logias.Logias;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.core.Utils;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.valid8j.Requires;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

import static com.github.dakusui.symfonion.core.exceptions.ExceptionThrower.typeMismatchException;
import static com.github.dakusui.symfonion.core.exceptions.SymfonionTypeMismatchException.ARRAY;
import static com.github.dakusui.symfonion.core.exceptions.SymfonionTypeMismatchException.OBJECT;
import static com.github.dakusui.valid8j.Requires.requireNonNull;

public class Song {
  
  public static class Builder {
    private final Context logiasContext;
    private final JsonObject json;
    
    public Builder(Context logiasContext, JsonObject jsonObject) {
      this.logiasContext = requireNonNull(logiasContext);
      this.json = requireNonNull(jsonObject);
    }
    
    private static Context loadMidiDeviceProfile(JsonObject json, Context logiasContext) throws SymfonionException, JsonException {
      JsonElement tmp = JsonUtils.asJsonObjectWithDefault(json, new JsonObject(), Keyword.$settings);
      if (!tmp.isJsonObject()) {
        throw typeMismatchException(tmp, json, OBJECT);
      }
      String profileName = JsonUtils.asStringWithDefault(tmp.getAsJsonObject(), "", Keyword.$mididevice);
      Logias logias = new Logias(logiasContext);
      if (!"".equals(profileName)) {
        JsonObject deviceDef = JsonUtils.toJson(Utils.loadResource(profileName + ".json")).getAsJsonObject();
        Iterator<String> i = JsonUtils.keyIterator(deviceDef);
        while (i.hasNext()) {
          String k = i.next();
          JsonElement v = deviceDef.get(k);
          logiasContext.bind(k, logias.run(logias.buildSexp(v)));
        }
      }
      return logiasContext;
    }
    
    private static List<Bar> initSequence(JsonObject json, Map<String, Groove> grooves, Map<String, Pattern> patterns) throws SymfonionException, JsonException {
      List<Bar> bars = new LinkedList<>();
      JsonElement tmp = JsonUtils.asJsonElement(json, Keyword.$sequence);
      if (!tmp.isJsonArray()) {
        throw typeMismatchException(tmp, json, ARRAY);
      }
      JsonArray seqJson = tmp.getAsJsonArray();
      int len = seqJson.getAsJsonArray().size();
      for (int i = 0; i < len; i++) {
        JsonElement barJson = seqJson.get(i);
        if (!barJson.isJsonObject()) {
          throw typeMismatchException(seqJson, json, OBJECT);
        }
        Bar bar = new Bar(barJson.getAsJsonObject(), json, grooves, patterns);
        bars.add(bar);
      }
      return bars;
    }
    
    private static Map<String, NoteMap> initNoteMaps(JsonObject json) throws SymfonionException, JsonException {
      Map<String, NoteMap> noteMaps = new HashMap<>();
      final JsonObject noteMapsJSON = JsonUtils.asJsonObjectWithDefault(json, new JsonObject(), Keyword.$notemaps);
      
      Iterator<String> i = JsonUtils.keyIterator(noteMapsJSON);
      noteMaps.put(Keyword.$normal.toString(), NoteMap.defaultNoteMap);
      noteMaps.put(Keyword.$percussion.toString(), NoteMap.defaultPercussionMap);
      while (i.hasNext()) {
        String name = i.next();
        NoteMap cur = new NoteMap(JsonUtils.asJsonObject(noteMapsJSON, name));
        noteMaps.put(name, cur);
      }
      return noteMaps;
    }
    
    private static Map<String, Pattern> initPatterns(JsonObject json, Map<String, NoteMap> noteMaps) throws SymfonionException, JsonException {
      Map<String, Pattern> patterns = new HashMap<>();
      JsonObject patternsJSON = JsonUtils.asJsonObjectWithDefault(json, new JsonObject(), Keyword.$patterns);
      
      Iterator<String> i = JsonUtils.keyIterator(patternsJSON);
      while (i.hasNext()) {
        String name = i.next();
        Pattern cur = Pattern.createPattern(JsonUtils.asJsonObject(patternsJSON, name), json, noteMaps);
        patterns.put(name, cur);
      }
      return patterns;
    }
    
    private static Map<String, Part> initParts(JsonObject json) throws SymfonionException, JsonException {
      Map<String, Part> parts = new HashMap<>();
      if (JsonUtils.hasPath(json, Keyword.$parts)) {
        JsonObject instrumentsJSON = JsonUtils.asJsonObject(json, Keyword.$parts);
        Iterator<String> i = JsonUtils.keyIterator(instrumentsJSON);
        while (i.hasNext()) {
          String name = i.next();
          Part cur = new Part(name, JsonUtils.asJsonObject(instrumentsJSON, name));
          parts.put(name, cur);
        }
      }
      return parts;
    }
    
    private static Map<String, Groove> initGrooves(JsonObject json) throws SymfonionException, JsonException {
      Map<String, Groove> grooves = new HashMap<>();
      if (JsonUtils.hasPath(json, Keyword.$grooves)) {
        JsonObject groovesJSON = JsonUtils.asJsonObject(json, Keyword.$grooves);
        
        Iterator<String> i = JsonUtils.keyIterator(groovesJSON);
        while (i.hasNext()) {
          String name = i.next();
          Groove cur = Groove.createGroove(JsonUtils.asJsonArray(groovesJSON, name), json);
          grooves.put(name, cur);
        }
      }
      return grooves;
    }
    
    public Song build() throws JsonException, SymfonionException {
      Map<String, NoteMap> noteMaps = initNoteMaps(json);
      Map<String, Groove> grooves = initGrooves(json);
      Map<String, Pattern> patterns = initPatterns(json, noteMaps);
      return new Song(
          loadMidiDeviceProfile(json, logiasContext),
          initParts(this.json),
          patterns,
          noteMaps,
          grooves,
          initSequence(json, grooves, patterns)
      );
    }
  }
  
  private final Context logiasContext;
  private final Map<String, Part> parts;
  private final Map<String, Pattern> patterns;
  private final Map<String, NoteMap> noteMaps;
  private final Map<String, Groove> grooves;
  private final List<Bar> bars;
  
  
  public Song(Context logiasContext,
              Map<String, Part> parts,
              Map<String, Pattern> patterns,
              Map<String, NoteMap> noteMaps,
              Map<String, Groove> grooves,
              List<Bar> bars
  ) {
    this.logiasContext = logiasContext;
    this.parts = Requires.requireNonNull(parts);
    this.patterns = Requires.requireNonNull(patterns);
    this.noteMaps = requireNonNull(noteMaps);
    this.grooves = requireNonNull(grooves);
    this.bars = requireNonNull(bars);
  }
  
  
  public Pattern pattern(String patternName) {
    return this.patterns.get(patternName);
  }
  
  public NoteMap noteMap(String noteMapName) {
    return this.noteMaps.get(noteMapName);
  }
  
  public List<Bar> bars() {
    return Collections.unmodifiableList(this.bars);
  }
  
  public Set<String> partNames() {
    return Collections.unmodifiableSet(this.parts.keySet());
  }
  
  public Part part(String name) {
    return this.parts.get(name);
  }
  
  public Context getLogiasContext() {
    return this.logiasContext;
  }
  
  public Groove groove(String grooveName) {
    return this.grooves.get(grooveName);
  }
}
