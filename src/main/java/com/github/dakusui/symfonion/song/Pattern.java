package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.exceptions.ExceptionContext;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.compat.json.CompatJsonException;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.PART_MEASURE_JSON;
import static com.github.dakusui.symfonion.compat.exceptions.ExceptionContext.entry;
import static com.github.dakusui.symfonion.compat.json.CompatJsonUtils.asJsonArray;
import static com.github.dakusui.symfonion.compat.json.CompatJsonUtils.asJsonElement;


/**
 * A reusable unit which consists of a sequence of part measures.
 *
 * @see PartMeasure
 */
public class Pattern {
  private final List<PartMeasure>     body;
  private final PartMeasureParameters params;

  Pattern(JsonObject jsonObject, NoteMap noteMap) {
    // Initialize 'body'.
    this.body   = new LinkedList<>();
    this.params = new PartMeasureParameters(jsonObject);
    JsonArray bodyJSON;
    if (asJsonElement(jsonObject, Keyword.$body).isJsonPrimitive()) {
      bodyJSON = new JsonArray();
      bodyJSON.add(asJsonElement(jsonObject, Keyword.$body));
    } else {
      bodyJSON = asJsonArray(jsonObject, Keyword.$body);
    }
    int len = bodyJSON.size();
    for (int i = 0; i < len; i++) {
      JsonElement cur = bodyJSON.get(i);
      try (var ignored = exceptionContext(entry(PART_MEASURE_JSON, cur))) {
        body.add(new PartMeasure(cur, params, noteMap));
      }
    }
  }

  /**
   * Returns a list of part measures which this `Pattern` consists of.
   *
   * @return A list of part measures.
   */
  public List<PartMeasure> partMeasures() {
    return Collections.unmodifiableList(this.body);
  }

  /**
   * Returns a `Parameters` object that defines default values of part measures in this object.
   *
   * @return A `Parameters` object.
   * @see PartMeasureParameters
   */
  public PartMeasureParameters parameters() {
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
   *   "$body": "{part measure string}",
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
   * @see PartMeasureParameters
   * @see PartMeasure
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
