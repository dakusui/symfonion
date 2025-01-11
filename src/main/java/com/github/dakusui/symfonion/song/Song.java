package com.github.dakusui.symfonion.song;

import com.github.dakusui.logias.Logias;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.compat.exceptions.ExceptionContext;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.compat.json.CompatJsonException;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.utils.Utils;
import com.github.valid8j.classic.Requires;
import com.github.valid8j.pcond.forms.Predicates;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.function.Predicate;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.JSON_ELEMENT_ROOT;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.exceptionContext;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.typeMismatchException;
import static com.github.dakusui.symfonion.compat.exceptions.ExceptionContext.entry;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionTypeMismatchException.ARRAY;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionTypeMismatchException.OBJECT;
import static com.github.valid8j.classic.Requires.requireNonNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * //@formatter:off
 * This class models the logical aspect of the entire musical piece, described by the application syntax.
 *
 * Entries that directly matter in this class are as follows:
 *
 * .Entries `Song` class processes
 * ----
 * {
 *   "$noteMaps": { "<noteMapName>": "<NoteMap>"},
 *   "$parts": { "<partName>": "<Part>" },
 *   "$patterns": { "<patternName>": "<Pattern>"},
 *   "$grooves": { "<grooveName>": ["<Groove>", "<Groove>", "..."] },
 *   "$sequence": [ "<Bar>", "<Bar>", "..." ]
 * }
 * ----
 *
 * Each element in `$sequence` array is a {@link Bar} object.
 * A bar contains notes and its related information played during a certain period of time.
 *
 * //@formatter:on
 */
public class Song {
  private final Context              logiasContext;
  private final Map<String, Part>    parts;
  private final Map<String, Pattern> patterns;
  private final Map<String, Groove>  grooves;
  private final List<Bar>            bars;
  private final JsonObject           rootJsonObject;

  /**
   * Creates an object of this class.
   *
   * @param logiasContext  A context, in which JSON array can be executed as an S-expression.
   * @param parts          Parts of a musical work.
   * @param patterns       Patterns referenced from `bars`.
   * @param grooves        Grooves referenced from `bars`.
   * @param bars           Bars of a musical work.
   * @param rootJsonObject A root JSON object that `barJsonObject` belongs to.
   */
  public Song(Context logiasContext,
              Map<String, Part> parts,
              Map<String, Pattern> patterns,
              Map<String, Groove> grooves,
              List<Bar> bars,
              JsonObject rootJsonObject) {
    this.logiasContext  = logiasContext;
    this.parts          = Requires.requireNonNull(parts);
    this.patterns       = Requires.requireNonNull(patterns);
    this.grooves        = requireNonNull(grooves);
    this.bars           = requireNonNull(bars);
    this.rootJsonObject = requireNonNull(rootJsonObject);
  }

  /**
   * Returns a pattern for `patternName`.
   *
   * @param patternName A name of a pattern to be looked up.
   * @return A pattern for `patternName`.
   */
  public Pattern pattern(String patternName) {
    return this.patterns.get(patternName);
  }

  /**
   * Returns bars.
   *
   * @return Bars.
   */
  public List<Bar> bars() {
    return unmodifiableList(this.bars);
  }

  /**
   * Returns all known part names.
   *
   * @return A list of part names.
   */
  public Set<String> partNames() {
    return unmodifiableSet(this.parts.keySet());
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

  /**
   * Returns a root JSON object to which this bar belongs.
   *
   * @return A root JSON object.
   */
  public JsonObject rootJsonObject() {
    return this.rootJsonObject;
  }

  public static class Builder {
    private final Context    logiasContext;
    private final JsonObject json;

    private Predicate<Bar>    barFilter  = Predicates.alwaysTrue();
    private Predicate<String> partFilter = Predicates.alwaysTrue();

    public Builder(Context logiasContext, JsonObject jsonObject) {
      this.logiasContext = requireNonNull(logiasContext);
      this.json          = requireNonNull(jsonObject);
    }

    public Builder barFilter(Predicate<Bar> barFilter) {
      this.barFilter = requireNonNull(barFilter);
      return this;
    }

    public Builder partFilter(Predicate<String> partFilter) {
      this.partFilter = requireNonNull(partFilter);
      return this;
    }

    public Song build() throws CompatJsonException, SymfonionException {
      try (ExceptionContext ignored = exceptionContext(entry(JSON_ELEMENT_ROOT, json))) {
        Map<String, NoteMap> noteMaps = initNoteMaps(json);
        Map<String, Groove>  grooves  = initGrooves(json);
        Map<String, Pattern> patterns = initPatterns(json, noteMaps);
        return new Song(loadMidiDeviceProfile(json, logiasContext),
                        initParts(this.json),
                        patterns,
                        grooves,
                        initBars(json,
                                 grooves,
                                 noteMaps,
                                 patterns,
                                 this.barFilter,
                                 this.partFilter),
                        json);
      }
    }

    private static Context loadMidiDeviceProfile(JsonObject json, Context logiasContext) throws SymfonionException, CompatJsonException {
      JsonElement tmp = CompatJsonUtils.asJsonObjectWithDefault(json, new JsonObject(), Keyword.$settings);
      if (!tmp.isJsonObject()) {
        throw typeMismatchException(tmp, OBJECT);
      }
      String profileName = CompatJsonUtils.asStringWithDefault(tmp.getAsJsonObject(), "", Keyword.$mididevice);
      Logias logias      = new Logias(logiasContext);
      if (!"".equals(profileName)) {
        JsonObject       deviceDef = CompatJsonUtils.toJson(Utils.loadResource(profileName + ".json")).getAsJsonObject();
        Iterator<String> i         = CompatJsonUtils.keyIterator(deviceDef);
        while (i.hasNext()) {
          String      k = i.next();
          JsonElement v = deviceDef.get(k);
          logiasContext.bind(k, logias.run(logias.buildSexp(v)));
        }
      }
      return logiasContext;
    }

    private static List<Bar> initBars(
        JsonObject json,
        Map<String, Groove> grooves,
        Map<String, NoteMap> noteMaps,
        Map<String, Pattern> patterns,
        Predicate<Bar> barFilter,
        Predicate<String> partFilter) throws SymfonionException, CompatJsonException {
      List<Bar> bars = new LinkedList<>();
      try (ExceptionContext ignored = exceptionContext(entry(JSON_ELEMENT_ROOT, json))) {
        JsonElement tmp = CompatJsonUtils.asJsonElement(json, Keyword.$sequence);
        if (!tmp.isJsonArray()) {
          throw typeMismatchException(tmp, ARRAY);
        }
        JsonArray seqJson = tmp.getAsJsonArray();
        int       len     = seqJson.getAsJsonArray().size();
        for (int i = 0; i < len; i++) {
          JsonElement barJson = seqJson.get(i);
          if (!barJson.isJsonObject()) {
            throw typeMismatchException(seqJson, OBJECT);
          }
          Bar bar = new Bar(barJson.getAsJsonObject(),
                            grooves,
                            noteMaps,
                            patterns,
                            partFilter);
          if (barFilter.test(bar))
            bars.add(bar);
        }
      }
      return bars;
    }

    /**
     * Returns a map of note-map name to note-map.
     *
     * @param json A JSON object that defines note-maps.
     * @return A map of note-map name to note-map.
     * @throws SymfonionException  An error found in {@code json} argument.
     * @throws CompatJsonException An error found in {@code json} argument.
     */
    private static Map<String, NoteMap> initNoteMaps(JsonObject json) throws SymfonionException, CompatJsonException {
      Map<String, NoteMap> noteMaps     = new HashMap<>();
      final JsonObject     noteMapsJSON = CompatJsonUtils.asJsonObjectWithDefault(json, new JsonObject(), Keyword.$notemaps);

      Iterator<String> i = CompatJsonUtils.keyIterator(noteMapsJSON);
      noteMaps.put(Keyword.$normal.toString(), NoteMap.defaultNoteMap);
      noteMaps.put(Keyword.$percussion.toString(), NoteMap.defaultPercussionMap);
      while (i.hasNext()) {
        String  name = i.next();
        NoteMap cur  = new NoteMap(CompatJsonUtils.asJsonObject(noteMapsJSON, name));
        noteMaps.put(name, cur);
      }
      return noteMaps;
    }

    private static Map<String, Pattern> initPatterns(JsonObject json, Map<String, NoteMap> noteMaps) throws SymfonionException, CompatJsonException {
      Map<String, Pattern> patterns     = new HashMap<>();
      JsonObject           patternsJSON = CompatJsonUtils.asJsonObjectWithDefault(json, new JsonObject(), Keyword.$patterns);

      try (ExceptionContext ignored = exceptionContext(entry(JSON_ELEMENT_ROOT, json))) {
        Iterator<String> i = CompatJsonUtils.keyIterator(patternsJSON);
        while (i.hasNext()) {
          String  name = i.next();
          Pattern cur  = Pattern.createPattern(CompatJsonUtils.asJsonObject(patternsJSON, name), noteMaps);
          patterns.put(name, cur);
        }
      }
      return patterns;
    }

    private static Map<String, Part> initParts(JsonObject json) throws SymfonionException, CompatJsonException {
      Map<String, Part> parts = new HashMap<>();
      if (CompatJsonUtils.hasPath(json, Keyword.$parts)) {
        JsonObject       instrumentsJSON = CompatJsonUtils.asJsonObject(json, Keyword.$parts);
        Iterator<String> i               = CompatJsonUtils.keyIterator(instrumentsJSON);
        while (i.hasNext()) {
          String name = i.next();
          Part   cur  = new Part(name, CompatJsonUtils.asJsonObject(instrumentsJSON, name));
          parts.put(name, cur);
        }
      }
      return parts;
    }

    private static Map<String, Groove> initGrooves(JsonObject json) throws SymfonionException, CompatJsonException {
      Map<String, Groove> grooves = new HashMap<>();
      if (CompatJsonUtils.hasPath(json, Keyword.$grooves)) {
        JsonObject groovesJSON = CompatJsonUtils.asJsonObject(json, Keyword.$grooves);

        Iterator<String> i = CompatJsonUtils.keyIterator(groovesJSON);
        while (i.hasNext()) {
          String name = i.next();
          Groove cur  = Groove.createGroove(CompatJsonUtils.asJsonArray(groovesJSON, name));
          grooves.put(name, cur);
        }
      }
      return grooves;
    }
  }
}
