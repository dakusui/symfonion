package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.symfonion.core.Fraction;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.core.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.symfonion.core.ExceptionThrower.illegalFormatException;
import static com.github.dakusui.symfonion.core.ExceptionThrower.noteMapNotFoundException;
import static com.github.dakusui.symfonion.core.SymfonionIllegalFormatException.NOTELENGTH_EXAMPLE;


public class Pattern {
  public static class Parameters {
    static final Fraction QUARTER = new Fraction(1, 4);
    double   gate          = 0.8;
    Fraction length        = QUARTER;
    int      transpose     = 0;
    int      velocitybase  = 64;
    int      velocitydelta = 10;
    int arpegio;

    public Parameters(JsonObject json) throws SymfonionException, JsonException {
      init(json);
    }

    public double gate() {
      return this.gate;
    }

    private void init(JsonObject json) throws SymfonionException, JsonException {
      if (json == null) {
        json = JsonUtils.toJson("{}").getAsJsonObject();
      }
      this.velocitybase = JsonUtils.asIntWithDefault(json, 64, Keyword.$velocitybase);
      this.velocitydelta = JsonUtils.asIntWithDefault(json, 5, Keyword.$velocitydelta);
      this.gate = JsonUtils.asDoubleWithDefault(json, 0.8, Keyword.$gate);
      this.length = Util.parseNoteLength(JsonUtils.asStringWithDefault(json, "4", Keyword.$length));
      if (this.length == null) {
        throw illegalFormatException(
            JsonUtils.asJsonElement(json, Keyword.$length),
            NOTELENGTH_EXAMPLE
        );
      }
      this.transpose = JsonUtils.asIntWithDefault(json, 0, Keyword.$transpose);
      this.arpegio = JsonUtils.asIntWithDefault(json, 0, Keyword.$arpegio);
    }

    public Fraction length() {
      return this.length;
    }

    public int transpose() {
      return this.transpose;
    }

    public int velocitybase() {
      return this.velocitybase;
    }

    public int velocitydelta() {
      return this.velocitydelta;
    }

    public int arpegio() {
      return this.arpegio;
    }
  }

  public static Pattern createPattern(JsonObject json, Song song) throws SymfonionException, JsonException {
    NoteMap noteMap = NoteMap.defaultNoteMap;
    if (JsonUtils.hasPath(json, Keyword.$notemap)) {
      String noteMapName = JsonUtils.asString(json, Keyword.$notemap);
      noteMap = song.noteMap(noteMapName);
      if (noteMap == null) {
        throw noteMapNotFoundException(
            JsonUtils.asJsonElement(json, Keyword.$notemap),
            noteMapName
        );
      }
    }
    Pattern ret = new Pattern(noteMap);
    ret.init(json);
    return ret;
  }

  List<Stroke> body    = null;
  NoteMap      noteMap = null;
  Parameters   params  = null;

  Pattern(NoteMap noteMap) {
    this.noteMap = noteMap;
  }

  protected void init(JsonObject json) throws SymfonionException, JsonException {
    // Initialize 'body'.
    this.body = new LinkedList<Stroke>();
    this.params = new Parameters(json);
    JsonArray bodyJSON;
    if (JsonUtils.asJsonElement(json, Keyword.$body).isJsonPrimitive()) {
      bodyJSON = new JsonArray();
      bodyJSON.add(JsonUtils.asJsonElement(json, Keyword.$body));
    } else {
      bodyJSON = JsonUtils.asJsonArray(json, Keyword.$body);
    }
    int len = bodyJSON.size();
    for (int i = 0; i < len; i++) {
      JsonElement cur = bodyJSON.get(i);
      Stroke stroke = new Stroke(
          cur,
          params,
          this.noteMap
      );
      body.add(stroke);
    }
  }

  public List<Stroke> strokes() {
    return Collections.unmodifiableList(this.body);
  }

  public Parameters parameters() {
    return this.params;
  }
}
