package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.compat.json.CompatJsonException;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.utils.Fraction;
import com.github.dakusui.symfonion.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.illegalFormatException;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.noteMapNotFoundException;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException.NOTE_LENGTH_EXAMPLE;
import static com.github.dakusui.symfonion.compat.json.CompatJsonUtils.asJsonElement;


/**
 * A reusable unit which consists of a sequence of strokes.
 *
 * @see Stroke
 */
public class Pattern {
  /**
   * A class that models a set of default parameter applied to each `Stroke` in this `Pattern`.
   */
  public static class Parameters {
    static final Fraction QUARTER = new Fraction(1, 4);
    final double gate;
    final Fraction length;
    final int transpose;
    final int velocityBase;
    final int velocityDelta;
    final int arpeggio;

    public Parameters(JsonObject json) throws SymfonionException, CompatJsonException {
      if (json == null) {
        json = CompatJsonUtils.toJson("{}").getAsJsonObject();
      }
      this.velocityBase = CompatJsonUtils.asIntWithDefault(json, 64, Keyword.$velocitybase);
      this.velocityDelta = CompatJsonUtils.asIntWithDefault(json, 5, Keyword.$velocitydelta);
      this.gate = CompatJsonUtils.asDoubleWithDefault(json, 0.8, Keyword.$gate);
      this.length = Utils.parseNoteLength(CompatJsonUtils.asStringWithDefault(json, "16", Keyword.$length));
      if (this.length == null) {
        throw illegalFormatException(asJsonElement(json, Keyword.$length), NOTE_LENGTH_EXAMPLE);
      }
      this.transpose = CompatJsonUtils.asIntWithDefault(json, 0, Keyword.$transpose);
      this.arpeggio = CompatJsonUtils.asIntWithDefault(json, 0, Keyword.$arpeggio);
    }

    public double gate() {
      return this.gate;
    }

    public Fraction length() {
      return this.length;
    }

    public int transpose() {
      return this.transpose;
    }

    public int velocityBase() {
      return this.velocityBase;
    }

    /**
     * A step value with which the velocity of a stroke is increased for a single accent sign.
     *
     * @return A step value for a velocity of a stroke.
     */
    public int velocityDelta() {
      return this.velocityDelta;
    }

    public int arpeggio() {
      return this.arpeggio;
    }
  }

  private final List<Stroke> body;
  private final Parameters params;

  /**
   * // @formatter:off
   * Creates an object of this class from a given `jsonObject` and `noteMap`.
   * The `jsonObject` can be either:
   *
   * [source, JSON]
   * ----
   * {
   *   "$body": "{stroke string}"
   * }
   * ----
   *
   * Or:
   * [source, JSON]
   * ----
   * {
   *   "$body": [
   *     "{stroke 1}",
   *     "{stroke 2}"
   *   ]
   * }
   * ----
   * // @formatter:on
   *
   * @param jsonObject A JSON object which contains the definition of a `Pattern` object.
   * @param noteMap    A note map used to create an object of this class.
   * @see NoteMap
   * a@see Stroke
   */
  Pattern(JsonObject jsonObject, NoteMap noteMap) {
    // Initialize 'body'.
    this.body = new LinkedList<>();
    this.params = new Parameters(jsonObject);
    JsonArray bodyJSON;
    if (asJsonElement(jsonObject, Keyword.$body).isJsonPrimitive()) {
      bodyJSON = new JsonArray();
      bodyJSON.add(asJsonElement(jsonObject, Keyword.$body));
    } else {
      bodyJSON = CompatJsonUtils.asJsonArray(jsonObject, Keyword.$body);
    }
    int len = bodyJSON.size();
    for (int i = 0; i < len; i++) {
      JsonElement cur = bodyJSON.get(i);
      Stroke stroke = new Stroke(cur, params, noteMap);
      body.add(stroke);
    }
  }

  /**
   * Returns a list of strokes which this `Pattern` consists of.
   *
   * @return A list of strokes.
   */
  public List<Stroke> strokes() {
    return Collections.unmodifiableList(this.body);
  }

  /**
   * Returns a `Parameters` object that defines default values of strokes in this object.
   *
   * @return A `Parameters` object.
   * @see Parameters
   */
  public Parameters parameters() {
    return this.params;
  }

  public static Pattern createPattern(JsonObject json, Map<String, NoteMap> noteMaps) throws SymfonionException, CompatJsonException {
    NoteMap noteMap = NoteMap.defaultNoteMap;
    if (CompatJsonUtils.hasPath(json, Keyword.$notemap)) {
      String noteMapName = CompatJsonUtils.asString(json, Keyword.$notemap);
      noteMap = noteMaps.get(noteMapName);
      if (noteMap == null) {
        throw noteMapNotFoundException(asJsonElement(json, Keyword.$notemap), noteMapName);
      }
    }
    return new Pattern(json, noteMap);
  }
}
