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
   *
   * Following is a list of available parameters.:
   *
   * * `$velocitybase`
   * * `$velocitydelta`
   * * `$gate`
   * * `$length`
   * * `$transpose`
   * * `$arpeggio`
   */
  public static class Parameters {
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

    /**
     * Returns a `$gate` value.
     * @return a `$gate` value
     */
    public double gate() {
      return this.gate;
    }

    /**
     * Returns a `$length` value.
     * @return a `$length` value
     */
    public Fraction length() {
      return this.length;
    }

    /**
     * Returns a `$transpose` value.
     * @return a `$transpose` value
     */
    public int transpose() {
      return this.transpose;
    }

    /**
     * Returns a `$velocitybase` value.
     * @return a `$velocitybase` value
     */
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

    /**
     * Returns a `$arpeggio` value.
     *
     * @return a `$arpeggio` value
     */
    public int arpeggio() {
      return this.arpeggio;
    }
  }

  private final List<Stroke> body;
  private final Parameters params;

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

  /**
   * // @formatter:off
   * Creates an object of this class from a given `jsonObject` and `noteMap`.
   * The `jsonObject` can be either:
   *
   * [source, JSON]
   * ----
   * {
   *   "$body": "{stroke string}",
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
   *   ],
   *   "$length": "<bodyValue>",
   *   "$gate": "<gateValue>",
   *   "$otherParameter": "<otherParameterValue>",
   * }
   * ----
   * // @formatter:on
   *
   * `<otherParameterValue>` can be one of `Pattern.Parameters`.
   * This method creates `Pattern` object and a note map (`Map<String, NoteMap>`) used for it is chosen from the `noteMaps`
   * passed to this method.
   * If no matching entry is found in `noteMaps`, an exception will be thrown.
   *
   * @param jsonObject A JSON object which contains the definition of a `Pattern` object.
   * @param noteMaps note maps available for this pattern.
   * @see NoteMap
   * @see Pattern.Parameters
   * @see Stroke
   */
  public static Pattern createPattern(JsonObject jsonObject, Map<String, NoteMap> noteMaps) throws SymfonionException, CompatJsonException {
    NoteMap noteMap = NoteMap.defaultNoteMap;
    if (CompatJsonUtils.hasPath(jsonObject, Keyword.$notemap)) {
      String noteMapName = CompatJsonUtils.asString(jsonObject, Keyword.$notemap);
      noteMap = noteMaps.get(noteMapName);
      if (noteMap == null) {
        throw noteMapNotFoundException(asJsonElement(jsonObject, Keyword.$notemap), noteMapName);
      }
    }
    return new Pattern(jsonObject, noteMap);
  }
}
