package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException;
import com.github.dakusui.symfonion.compat.json.*;
import com.github.dakusui.symfonion.core.MidiCompiler;
import com.github.dakusui.symfonion.core.MidiCompilerContext;
import com.github.dakusui.symfonion.song.Pattern.Parameters;
import com.github.dakusui.symfonion.utils.Fraction;
import com.github.dakusui.symfonion.utils.Utils;
import com.google.gson.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException.NOTE_LENGTH_EXAMPLE;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionTypeMismatchException.PRIMITIVE;

/**
 * A class that models a stroke, which is one action of a human in a musical piece.
 */
public class Stroke {
  private static final java.util.regex.Pattern NOTES_REGEX_PATTERN = java.util.regex.Pattern.compile("([A-Zac-z])([#b]*)([><]*)([+\\-]*)?");
  private static final int UNDEFINED_NUM = -1;
  private final Fraction length;
  private final List<NoteSet> notes = new LinkedList<>();
  private final double gate;
  private final NoteMap noteMap;
  private final int[] volume;
  private final int[] pan;
  private final int[] reverb;
  private final int[] chorus;
  private final int[] pitch;
  private final int[] modulation;
  private final int pgno;
  private String bkno = null;
  private final int tempo;
  private final JsonArray sysex;
  private final int[] aftertouch;
  private final JsonElement strokeJson;

  /**
   *
   * // @formatter:off
   *
   * The ```strokeJson``` needs to be a JSON element which can promote to the following object with ```CompatJsonUtils#asJsonArrayWithPromotion( JsonElement, Object...)```
   * [source, JSON]
   * .strokeJson
   * ----
   * {
   *   "$notes": "NOTES: String",
   *   "$length": "LENGTH: Fraction String",
   *   "$gate": "GATE: DOUBLE",
   *   "$volume": "VOLUME: Int Array",
   *   "$pan": "PAN: Int Array",
   *   "$reverb": "REVERB: Int Array",
   *   "$chorus": "CHORUS: Int Array",
   *   "$modulation": "MODULATION: Int Array",
   *   "$aftertouch": "AFTER TOUCH: Int Array",
   *   "$sysex": "SYSTEM EXCLUSIVE: Array",
   * }
   * ----
   *
   * If it doesn't have a corresponding element, it will be considered `null` is specified.
   * The **NOTES String** should match the following regular expression or such strings concatenated by `;`.
   *
   * `([A-Zac-z])([#b]*)([><]*)([\\+\\-]*)?`
   *
   * That is, the data this class host is actually a sequence of strokes, not merely a stroke.
   * Historical reason introduced this naming inconsistency.
   *
   * // Using `@see` at the bottom of this comment will break `mvn javadoc:javadoc` for unknown reason.
   * See also {@code Pattern.Parameters} and {@code CompatJsonUtils#asJsonArrayWithDefault(JsonElement, JsonArray, Object...)}.
   *
   * // @formatter:on
   *
   * @param strokeJson A JSON object to be interpreted as a stroke.
   * @param params Default values of strokes when omitted.
   * @param noteMap An object that defines mappings from a character to a MIDI note number.
   * @throws SymfonionException Failed to process `strokeJson`.
   * @throws CompatJsonException Failed to interpret `strokeJson`.
   *
   */
  public Stroke(JsonElement strokeJson, Parameters params, NoteMap noteMap) throws SymfonionException, CompatJsonException {
    String notes;
    Fraction len = params.length();
    double gate = params.gate();
    this.strokeJson = strokeJson;
    JsonObject obj = CompatJsonUtils.asJsonObjectWithPromotion(strokeJson, new String[]{
        Keyword.$notes.name(),
        Keyword.$length.name()
    });
    notes = CompatJsonUtils.asStringWithDefault(obj, null, Keyword.$notes);
    if (CompatJsonUtils.hasPath(obj, Keyword.$length)) {
      JsonElement lenJSON = CompatJsonUtils.asJsonElement(obj, Keyword.$length);
      if (lenJSON.isJsonPrimitive()) {
        len = Utils.parseNoteLength(lenJSON.getAsString());
        if (len == null) {
          throw illegalFormatException(lenJSON, NOTE_LENGTH_EXAMPLE);
        }
      } else {
        throw typeMismatchException(lenJSON, PRIMITIVE);
      }
    }
    if (CompatJsonUtils.hasPath(obj, Keyword.$gate)) {
      gate = CompatJsonUtils.asDouble(obj, Keyword.$gate);
    }
    this.tempo = CompatJsonUtils.hasPath(obj, Keyword.$tempo) ? CompatJsonUtils.asInt(obj, Keyword.$tempo) : UNDEFINED_NUM;
    this.pgno = CompatJsonUtils.hasPath(obj, Keyword.$program) ? CompatJsonUtils.asInt(obj, Keyword.$program) : UNDEFINED_NUM;
    if (CompatJsonUtils.hasPath(obj, Keyword.$bank)) {
      this.bkno = CompatJsonUtils.asString(obj, Keyword.$bank);
      // Checks if this.bkno can be parsed as a double value.
      assert this.bkno != null;
      //noinspection ResultOfMethodCallIgnored
      Double.parseDouble(this.bkno);
    }
    this.volume = getIntArray(obj, Keyword.$volume);
    this.pan = getIntArray(obj, Keyword.$pan);
    this.reverb = getIntArray(obj, Keyword.$reverb);
    this.chorus = getIntArray(obj, Keyword.$chorus);
    this.pitch = getIntArray(obj, Keyword.$pitch);
    this.modulation = getIntArray(obj, Keyword.$modulation);
    this.aftertouch = getIntArray(obj, Keyword.$aftertouch);
    this.sysex = CompatJsonUtils.asJsonArrayWithDefault(obj, null, Keyword.$sysex);
    /*
     * } else {
     * // unsupported
     * }
     */
    this.noteMap = noteMap;
    this.gate = gate;
    Fraction strokeLen = Fraction.zero;
    if (notes != null) {
      for (String nn : notes.split(";")) {
        NoteSet ns = new NoteSet();
        Fraction nsLen;
        String l;
        if ((l = parseNotes(nn, ns)) != null) {
          nsLen = Utils.parseNoteLength(l);
        } else {
          nsLen = len;
        }
        ns.setLength(nsLen);
        this.notes.add(ns);
        strokeLen = Fraction.add(strokeLen, nsLen);
      }
    }
    if (Fraction.zero.equals(strokeLen)) {
      strokeLen = len;
    }
    this.length = strokeLen;
  }

  /**
   * Returns the length of a stroke in this object.
   * @return The length of a stroke.
   */
  public Fraction length() {
    return length;
  }

  /**
   * Returns the gate of a stroke in this object.
   * It shows the ratio to keep pressing a key to the entire length of a stroke.
   *
   * @return The gate of a stroke.
   */
  public double gate() {
    return this.gate;
  }

  public List<NoteSet> noteSets() {
    return this.notes;
  }

  /*
   * Returns the 'length' portion of the string <code>s</code>.
   */
  private String parseNotes(String s, List<Note> notes) throws SymfonionException {
    Matcher m = NOTES_REGEX_PATTERN.matcher(s);
    int i;
    for (i = 0; m.find(i); i = m.end()) {
      if (i != m.start()) {
        throw syntaxErrorInNotePattern(s, i, m);
      }
      int n_ = this.noteMap.note(m.group(1), this.strokeJson);
      if (n_ >= 0) {
        int n =
            n_ +
                Utils.count('#', m.group(2)) - Utils.count('b', m.group(2)) +
                Utils.count('>', m.group(3)) * 12 - Utils.count('<', m.group(3)) * 12;
        int a = Utils.count('+', m.group(4)) - Utils.count('-', m.group(4));
        Note nn = new Note(n, a);
        notes.add(nn);
      }
    }
    Matcher n = Utils.lengthPattern.matcher(s);
    String ret = null;
    if (n.find(i)) {
      ret = s.substring(n.start(), n.end());
      i = n.end();
    }
    if (i != s.length()) {
      String msg = s.substring(0, i) + "`" + s.substring(i) + "' isn't a valid note expression. Notes must be like 'C', 'CEG8.', and so on.";
      throw illegalFormatException(this.strokeJson, msg);
    }
    return ret;

  }

  private void renderValues(int[] values, long pos, long strokeLen, MidiCompiler compiler, EventCreator creator) throws
      InvalidMidiDataException {
    if (values == null) {
      return;
    }
    long step = strokeLen / values.length;
    for (int i = 0; i < values.length; i++) {
      creator.createEvent(values[i], pos + step * i);
      compiler.controlEventProcessed();
    }
  }

  /**
   * Compiles this object using a given `compiler` and store its result in the `context`.
   *
   * @param compiler A compiler with which this object is processed.
   * @param context  A context in which the compiled result is stored.
   * @throws InvalidMidiDataException Failed to generate MIDI data.
   */
  public void compile(final MidiCompiler compiler, MidiCompilerContext context) throws InvalidMidiDataException {
    final Track track = context.track();
    final int ch = context.channel();
    long absolutePosition = context.convertRelativePositionInStrokeToAbsolutePosition(Fraction.zero);
    long strokeLen = context.getStrokeLengthInTicks(this);
    if (tempo != UNDEFINED_NUM) {
      track.add(compiler.createTempoEvent(this.tempo, absolutePosition));
      compiler.controlEventProcessed();
    }
    if (bkno != null) {
      int msb = Integer.parseInt(bkno.substring(0, this.bkno.indexOf('.')));
      track.add(compiler.createBankSelectMSBEvent(ch, msb, absolutePosition));
      if (this.bkno.indexOf('.') != -1) {
        int lsb = Integer.parseInt(bkno.substring(this.bkno.indexOf('.') + 1));
        track.add(compiler.createBankSelectLSBEvent(ch, lsb, absolutePosition));
      }
      compiler.controlEventProcessed();
    }
    if (pgno != UNDEFINED_NUM) {
      track.add(compiler.createProgramChangeEvent(ch, this.pgno, absolutePosition));
      compiler.controlEventProcessed();
    }
    if (sysex != null) {
      MidiEvent ev = compiler.createSysexEvent(ch, sysex, absolutePosition);
      if (ev != null) {
        track.add(ev);
        compiler.sysexEventProcessed();
      }
    }
    renderValues(volume, absolutePosition, strokeLen, compiler, (v, pos) -> track.add(compiler.createVolumeChangeEvent(ch, v, pos)));
    renderValues(pan, absolutePosition, strokeLen, compiler, (v, pos) -> track.add(compiler.createPanChangeEvent(ch, v, pos)));
    renderValues(reverb, absolutePosition, strokeLen, compiler, (v, pos) -> track.add(compiler.createReverbEvent(ch, v, pos)));
    renderValues(chorus, absolutePosition, strokeLen, compiler, (v, pos) -> track.add(compiler.createChorusEvent(ch, v, pos)));
    renderValues(pitch, absolutePosition, strokeLen, compiler, (v, pos) -> track.add(compiler.createPitchBendEvent(ch, v, pos)));
    renderValues(modulation, absolutePosition, strokeLen, compiler, (v, pos) -> track.add(compiler.createModulationEvent(ch, v, pos)));
    renderValues(aftertouch, absolutePosition, strokeLen, compiler, (v, pos) -> track.add(compiler.createAfterTouchChangeEvent(ch, v, pos)));
    int transpose = context.params().transpose();
    int arpegiodelay = context.params().arpeggio();
    int delta = 0;
    Fraction relPosInStroke = Fraction.zero;
    for (NoteSet noteSet : this.noteSets()) {
      absolutePosition = context.convertRelativePositionInStrokeToAbsolutePosition(relPosInStroke);
      long absolutePositionWhereNoteFinishes = context.convertRelativePositionInStrokeToAbsolutePosition(
          Fraction.add(
              relPosInStroke,
              noteSet.getLength()
          )
      );
      long noteLengthInTicks = absolutePositionWhereNoteFinishes - absolutePosition;
      for (Note note : noteSet) {
        int key = note.key() + transpose;
        int velocity = Math.max(
            0,
            Math.min(
                127,
                context.params().velocityBase() +
                    note.accent() * context.params().velocityDelta() +
                    context.getGrooveAccent(relPosInStroke)
            )
        );
        track.add(compiler.createNoteOnEvent(ch, key, velocity, absolutePosition + delta));
        track.add(compiler.createNoteOffEvent(ch, key, (long) (absolutePosition + delta + noteLengthInTicks * this.gate())));
        compiler.noteProcessed();
        delta += arpegiodelay;
      }
      compiler.noteSetProcessed();
      relPosInStroke = Fraction.add(relPosInStroke, noteSet.getLength());
    }
  }

  private static int[] getIntArray(JsonObject cur, Keyword kw) throws JsonInvalidPathException, JsonTypeMismatchException, JsonFormatException {
    int[] ret;
    if (!CompatJsonUtils.hasPath(cur, kw)) {
      return null;
    }
    JsonElement json = CompatJsonUtils.asJsonElement(cur, kw);
    if (json.isJsonArray()) {
      JsonArray arr = json.getAsJsonArray();
      ret = interpolate(expandDots(arr));
    } else {
      ret = new int[1];
      ret[0] = CompatJsonUtils.asInt(cur, kw);
    }
    return ret;
  }

  private static JsonArray expandDots(JsonArray arr) throws SymfonionIllegalFormatException {
    JsonArray ret = new JsonArray();
    for (int i = 0; i < arr.size(); i++) {
      JsonElement cur = arr.get(i);
      if (cur.isJsonPrimitive()) {
        JsonPrimitive p = cur.getAsJsonPrimitive();
        if (p.isBoolean() || p.isNumber())
          ret.add(p);
        else if (p.isString()) {
          String s = p.getAsString();
          for (int j = 0; j < s.length(); j++) {
            if (s.charAt(j) == '.')
              ret.add(JsonNull.INSTANCE);
            else
              throw syntaxErrorWhenExpandingDotsIn(arr);
          }
        }
      } else if (cur.isJsonNull())
        ret.add(cur);
      else
        throw CompatExceptionThrower.typeMismatchWhenExpandingDotsIn(arr);
    }
    return ret;
  }

  private static int[] interpolate(JsonArray arr) {
    int[] ret;
    Integer[] tmp = new Integer[arr.size()];
    for (int i = 0; i < tmp.length; i++) {
      if (arr.get(i) == null || arr.get(i).isJsonNull()) {
        tmp[i] = null;
      } else {
        tmp[i] = arr.get(i).getAsInt();
      }
    }
    ret = new int[arr.size()];
    int start = 0;
    int end = 0;
    for (int i = 0; i < tmp.length; i++) {
      if (tmp[i] != null) {
        start = ret[i] = tmp[i];
      } else {
        int j = i + 1;
        while (j < tmp.length) {
          if (tmp[j] != null) {
            end = tmp[j];
            ret[j] = end;
            break;
          }
          j++;
        }
        int step = (end - start) / (j - i);
        int curval = start;
        for (int k = i; k < j; k++) {
          curval += step;
          ret[k] = curval;
        }
        i = j;
      }
    }
    return ret;
  }

  /**
   * An interface to abstract an operation to create a MIDI event.
   */
  interface EventCreator {
    void createEvent(int v, long pos) throws InvalidMidiDataException;
  }
}
