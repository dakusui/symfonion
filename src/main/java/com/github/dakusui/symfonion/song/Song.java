package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.exceptions.ExceptionContext;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.compat.json.CompatJsonException;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.valid8j.pcond.forms.Predicates;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.JSON_ELEMENT_ROOT;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.exceptionContext;
import static com.github.dakusui.symfonion.compat.exceptions.ExceptionContext.entry;
import static com.github.dakusui.symfonion.song.CompatSong.Builder.initNoteMaps;
import static com.github.dakusui.symfonion.song.CompatSong.Builder.initParts;
import static com.github.valid8j.classic.Requires.requireNonNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * //@formatter:off
 * [source, JSON]
 * .The **Song** file format
 * ----
 * {
 *   "$parts": { "<partName1>": "<object:PartDefinition1>"
 *             },
 *   "$noteMaps": {
 *                  "<noteMapName1>": "<object:NoteMap>",
 *                  "<noteMapName2>": "<object:NoteMap>"
 *                },
 *   "$sequence": [
 *                  "<object:Measure1>",
 *                  "<object:Measure2>",
 *                  "...",
 *                  "<object:MeasureN>"
 *                ]
 * }
 * ----
 *
 * The each "<Measure>" should be a JSON object and look like as follows:
 * ----
 * {
*    "$beats": "16/16",
 *   "$parts": {
 *     "piano": {
 *     },
 *     "guitar": {
 *     }
 *   },
 *   "$groove": [ {}, {}, "...", {}],
 *   "$labels": [ "label1" ]
 * }
 * ----
 *
 * //@formatter:on
 *
 * @see Measure
 */
public class Song {
  private final Map<String, Part> parts;
  private final List<Measure>     measures;
  private final JsonObject        rootJsonObject;

  Song(Map<String, Part> parts,
       List<Measure> bars,
       JsonObject rootJsonObject) {
    this.parts          = requireNonNull(parts);
    this.measures       = requireNonNull(bars);
    this.rootJsonObject = requireNonNull(rootJsonObject);
  }

  /**
   * Returns bars.
   *
   * @return Bars.
   */
  public List<Measure> measures() {
    return unmodifiableList(this.measures);
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

  /**
   * Returns a root JSON object to which this bar belongs.
   *
   * @return A root JSON object.
   */
  public JsonObject rootJsonObject() {
    return this.rootJsonObject;
  }

  public static class Builder {
    private final JsonObject json;

    private Predicate<Measure> measureFilter = Predicates.alwaysTrue();
    private Predicate<String>  partFilter    = Predicates.alwaysTrue();

    public Builder(JsonObject jsonObject) {
      this.json = requireNonNull(jsonObject);
    }

    public Builder measureFilter(Predicate<Measure> barFilter) {
      this.measureFilter = requireNonNull(barFilter);
      return this;
    }

    public Builder partFilter(Predicate<String> partFilter) {
      this.partFilter = requireNonNull(partFilter);
      return this;
    }

    public Song build() throws CompatJsonException, SymfonionException {
      try (ExceptionContext ignored = exceptionContext(entry(JSON_ELEMENT_ROOT, json))) {
        Map<String, NoteMap> noteMaps = initNoteMaps(json);
        return new Song(initParts(this.json),
                        initMeasures(json,
                                     noteMaps,
                                     this.measureFilter,
                                     this.partFilter),
                        json);
      }
    }

    private List<Measure> initMeasures(JsonObject json,
                                       Map<String, NoteMap> noteMaps,
                                       Predicate<Measure> measureFilter,
                                       Predicate<String> partFilter) {
      List<Measure> ret      = new LinkedList<>();
      var           sequence = CompatJsonUtils.asJsonArray(json, Keyword.$sequence);
      for (var entry : sequence)
        ret.add(new Measure(entry.getAsJsonObject(), noteMaps));
      return unmodifiableList(ret);
    }
  }
}