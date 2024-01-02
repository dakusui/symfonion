package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.utils.Fraction;
import com.github.dakusui.symfonion.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.json.JsonUtils.asJsonElement;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.illegalFormatException;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.noteMapNotFoundException;
import static com.github.dakusui.symfonion.exceptions.SymfonionIllegalFormatException.NOTE_LENGTH_EXAMPLE;


public class Pattern {
  public static class Parameters {
    static final Fraction QUARTER = new Fraction(1, 4);
    double gate = 0.8;
    Fraction length = QUARTER;
    int transpose = 0;
    int velocityBase = 64;
    int velocityDelta = 10;
    int arpeggio;
    
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
      this.velocityBase = JsonUtils.asIntWithDefault(json, 64, Keyword.$velocitybase);
      this.velocityDelta = JsonUtils.asIntWithDefault(json, 5, Keyword.$velocitydelta);
      this.gate = JsonUtils.asDoubleWithDefault(json, 0.8, Keyword.$gate);
      this.length = Utils.parseNoteLength(JsonUtils.asStringWithDefault(json, "4", Keyword.$length));
      if (this.length == null) {
        throw illegalFormatException(
            asJsonElement(json, Keyword.$length),
            NOTE_LENGTH_EXAMPLE
        );
      }
      this.transpose = JsonUtils.asIntWithDefault(json, 0, Keyword.$transpose);
      this.arpeggio = JsonUtils.asIntWithDefault(json, 0, Keyword.$arpeggio);
    }
    
    public Fraction length() {
      return this.length;
    }
    
    public int transpose() {
      return this.transpose;
    }
    
    public int velocitybase() {
      return this.velocityBase;
    }
    
    public int velocitydelta() {
      return this.velocityDelta;
    }
    
    public int arpegio() {
      return this.arpeggio;
    }
  }
  
  public static Pattern createPattern(JsonObject json, JsonObject root, Map<String, NoteMap> noteMaps) throws SymfonionException, JsonException {
    NoteMap noteMap = NoteMap.defaultNoteMap;
    if (JsonUtils.hasPath(json, Keyword.$notemap)) {
      String noteMapName = JsonUtils.asString(json, Keyword.$notemap);
      noteMap = noteMaps.get(noteMapName);
      if (noteMap == null) {
        throw noteMapNotFoundException(asJsonElement(json, Keyword.$notemap), noteMapName);
      }
    }
    Pattern ret = new Pattern(noteMap);
    ret.init(json, root);
    return ret;
  }
  
  List<Stroke> body = null;
  NoteMap noteMap;
  Parameters params = null;
  
  Pattern(NoteMap noteMap) {
    this.noteMap = noteMap;
  }
  
  protected void init(JsonObject json, JsonObject root) throws SymfonionException, JsonException {
    // Initialize 'body'.
    this.body = new LinkedList<>();
    this.params = new Parameters(json);
    JsonArray bodyJSON;
    if (asJsonElement(json, Keyword.$body).isJsonPrimitive()) {
      bodyJSON = new JsonArray();
      bodyJSON.add(asJsonElement(json, Keyword.$body));
    } else {
      bodyJSON = JsonUtils.asJsonArray(json, Keyword.$body);
    }
    int len = bodyJSON.size();
    for (int i = 0; i < len; i++) {
      JsonElement cur = bodyJSON.get(i);
      Stroke stroke = new Stroke(cur, root, params, this.noteMap);
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
