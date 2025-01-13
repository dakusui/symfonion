package com.github.dakusui.symfonion.song;

import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.compat.exceptions.ExceptionContext;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.compat.json.CompatJsonException;
import com.github.valid8j.pcond.forms.Predicates;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.JSON_ELEMENT_ROOT;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.exceptionContext;
import static com.github.dakusui.symfonion.compat.exceptions.ExceptionContext.entry;
import static com.github.dakusui.symfonion.song.CompatSong.Builder.*;
import static com.github.valid8j.classic.Requires.requireNonNull;
import static java.util.Collections.*;

/**
 * //@formatter:off
 * [source, JSON]
 * .The **Song** file format
 * ----
 * {
 *   "$noteMaps": { "<noteMapName>": "<NoteMap>"},
 *   "$parts": { "<partName>": "<Part>" },
 *   "$patterns": { "<patternName>": "<Pattern>"},
 *   "$grooves": { "<grooveName>": ["<Groove>", "<Groove>", "..."] },
 *   "$sequence": [ "<Measure>", "<Measure>", "..." ]
 * }
 * ----
 *
 * The each "<Measure>" should be a JSON object and look like as follows:
 * ----
 * {
 *
 * }
 * ----
 *
 * //@formatter:on
 *
 * @see Measure
 */
public class Song {
  private final Context             logiasContext;
  private final Map<String, Part>   parts;
  private final Map<String, Groove> grooves;
  private final List<Measure>       measures;
  private final JsonObject          rootJsonObject;

  Song(Context logiasContext,
       Map<String, Part> parts,
       Map<String, Groove> grooves,
       List<Measure> bars,
       JsonObject rootJsonObject) {
    this.logiasContext  = logiasContext;
    this.parts          = requireNonNull(parts);
    this.grooves        = requireNonNull(grooves);
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

    private Predicate<Measure> measureFilter = Predicates.alwaysTrue();
    private Predicate<String>  partFilter    = Predicates.alwaysTrue();

    public Builder(Context logiasContext, JsonObject jsonObject) {
      this.logiasContext = requireNonNull(logiasContext);
      this.json          = requireNonNull(jsonObject);
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
        Map<String, Groove>  grooves  = initGrooves(json);
        return new Song(loadMidiDeviceProfile(json, logiasContext),
                        initParts(this.json),
                        grooves,
                        initMeasures(json,
                                     grooves,
                                     noteMaps,
                                     this.measureFilter,
                                     this.partFilter),
                        json);
      }
    }

    private List<Measure> initMeasures(JsonObject json, Map<String, Groove> grooves, Map<String, NoteMap> noteMaps, Predicate<Measure> measureFilter, Predicate<String> partFilter) {
      return emptyList();
    }
  }
}