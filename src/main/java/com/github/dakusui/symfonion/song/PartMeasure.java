package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException;
import com.github.dakusui.symfonion.compat.json.*;
import com.github.dakusui.symfonion.core.MidiCompiler;
import com.github.dakusui.symfonion.core.MidiCompilerContext;
import com.github.dakusui.symfonion.utils.Fraction;
import com.github.dakusui.symfonion.utils.Utils;
import com.google.gson.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException.NOTE_LENGTH_EXAMPLE;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionTypeMismatchException.PRIMITIVE;
import static com.github.dakusui.symfonion.compat.json.CompatJsonUtils.asJsonArrayWithDefault;
import static com.github.dakusui.symfonion.compat.json.CompatJsonUtils.asStringWithDefault;
import static com.github.dakusui.symfonion.utils.Fraction.ZERO;
import static com.github.valid8j.fluent.Expectations.*;

/**
 * A class that models a measure of a part.
 */
public class PartMeasure {
  public static final  java.util.regex.Pattern NOTE_LENGTH_REGEX_PATTERN
                                                                   = java.util.regex.Pattern.compile("(?<num>[1-9][0-9]*)(?<dots>\\.*)(?<articulation>[~^']?)");
  private static final java.util.regex.Pattern NOTES_REGEX_PATTERN = java.util.regex.Pattern.compile("(?<noteName>[A-Zac-z])(?<accidentals>[#b]*)(?<octaveShifts>[><]*)(?<accents>[+\\-]*)?");
  private static final int                     UNDEFINED_NUM       = -1;
  private final        PartMeasureParameters   defaultValues;
  private final        double                  gate;
  private final        int[]                   volume;
  private final        int[]                   pan;
  private final        int[]                   reverb;
  private final        int[]                   chorus;
  private final        int[]                   pitch;
  private final        int[]                   modulation;
  private final        int                     pgno;
  private final        String                  bkno;
  private final        int                     tempo;
  private final        JsonArray               sysex;
  private final        int[]                   aftertouch;
  private final        StrokeSequence          strokeSequence;

  /**
   * The ```partMeasureJson``` needs to be a JSON element which can promote to the following object
   * with ```CompatJsonUtils#asJsonArrayWithPromotion( JsonElement, Object...)```.
   *
   * // @formatter:off
   * [source, JSON]
   * .partMeasureJson
   * ----
   * {
   *   "$notes": "<strokeSequence:array>",
   *   "$length": "<length:string@{Fraction}>",
   *   "$gate": "<gate:number@{double}>",
   *   "$volume": "<volume:array@{int}>",
   *   "$pan": "<pan:array@{int}>",
   *   "$reverb": "<reverb:array@{int}>",
   *   "$chorus": "<chorus:array@{int}>",
   *   "$modulation": "<modulation:array@{int}>",
   *   "$aftertouch": "<aftertouch:array@{int}>",
   *   "$sysex": "<sysex:array@{int}>"
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
   * @param partMeasureJson A JSON object to be interpreted as a stroke.
   * @param params Default values of strokes when omitted.
   * @throws SymfonionException Failed to process `partMeasureJson`.
   * @throws CompatJsonException Failed to interpret `partMeasureJson`.
   *
   */
  public PartMeasure(JsonElement partMeasureJson, PartMeasureParameters params) throws SymfonionException, CompatJsonException {
    this(CompatJsonUtils.asJsonObjectWithPromotion(partMeasureJson, new String[]{Keyword.$notes.name(), Keyword.$length.name()}), params);
  }

  public PartMeasure(JsonObject obj, PartMeasureParameters defaultValues) throws SymfonionException, CompatJsonException {
    this.tempo          = CompatJsonUtils.hasPath(obj, Keyword.$tempo) ? CompatJsonUtils.asInt(obj, Keyword.$tempo) : UNDEFINED_NUM;
    this.pgno           = CompatJsonUtils.hasPath(obj, Keyword.$program) ? CompatJsonUtils.asInt(obj, Keyword.$program) : UNDEFINED_NUM;
    this.bkno           = resolveBankNumber(obj);
    this.volume         = getIntArray(obj, Keyword.$volume);
    this.pan            = getIntArray(obj, Keyword.$pan);
    this.reverb         = getIntArray(obj, Keyword.$reverb);
    this.chorus         = getIntArray(obj, Keyword.$chorus);
    this.pitch          = getIntArray(obj, Keyword.$pitch);
    this.modulation     = getIntArray(obj, Keyword.$modulation);
    this.aftertouch     = getIntArray(obj, Keyword.$aftertouch);
    this.sysex          = asJsonArrayWithDefault(obj, null, Keyword.$sysex);
    this.gate           = resolveGate(obj, defaultValues);
    this.strokeSequence = parseStrokeSequence(asStringWithDefault(obj, null, Keyword.$notes),
                                              resolveDefaultStrokeLength(obj, defaultValues),
                                              defaultValues.noteMap);
    this.defaultValues  = defaultValues;
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
    final int   ch    = context.channel();
    long absolutePosition = convertRelativePositionInPartMeasureToAbsolutePositionInTicks(context,
                                                                                          ZERO);
    long partMeasureLength = calculatePartMeasureLengthInTicks(this, context);
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
    renderArrayedValues(volume, absolutePosition, partMeasureLength, compiler, (v, pos) -> track.add(compiler.createVolumeChangeEvent(ch, v, pos)));
    renderArrayedValues(pan, absolutePosition, partMeasureLength, compiler, (v, pos) -> track.add(compiler.createPanChangeEvent(ch, v, pos)));
    renderArrayedValues(reverb, absolutePosition, partMeasureLength, compiler, (v, pos) -> track.add(compiler.createReverbEvent(ch, v, pos)));
    renderArrayedValues(chorus, absolutePosition, partMeasureLength, compiler, (v, pos) -> track.add(compiler.createChorusEvent(ch, v, pos)));
    renderArrayedValues(pitch, absolutePosition, partMeasureLength, compiler, (v, pos) -> track.add(compiler.createPitchBendEvent(ch, v, pos)));
    renderArrayedValues(modulation, absolutePosition, partMeasureLength, compiler, (v, pos) -> track.add(compiler.createModulationEvent(ch, v, pos)));
    renderArrayedValues(aftertouch, absolutePosition, partMeasureLength, compiler, (v, pos) -> track.add(compiler.createAfterTouchChangeEvent(ch, v, pos)));
    int      transpose      = this.defaultValues().transpose();
    int      arpegioDelay   = this.defaultValues().arpeggio();
    int      delta          = 0;
    Fraction relPosInStroke = Fraction.subtract(ZERO, this.strokeSequence.pickUpLength());
    for (Stroke stroke : this.strokes()) {
      absolutePosition = convertRelativePositionInPartMeasureToAbsolutePositionInTicks(context, relPosInStroke);
      long absolutePositionWhereNoteFinishes = convertRelativePositionInPartMeasureToAbsolutePositionInTicks(context,
                                                                                                             Fraction.add(relPosInStroke, stroke.length()));
      long noteLengthInTicks                 = absolutePositionWhereNoteFinishes - absolutePosition;
      for (Note note : stroke.notes()) {
        int key = note.key() + transpose;
        int velocity = Math.max(0, Math.min(127, this.defaultValues().velocityBase() +
                                                 note.accent() * this.defaultValues().velocityDelta() +
                                                 context.groove().calculateGrooveAccent(relPosInStroke,
                                                                                        context.relativeStrokePositionInBar())));
        track.add(compiler.createNoteOnEvent(ch, key, velocity, absolutePosition + delta));
        track.add(compiler.createNoteOffEvent(ch, key, (long) (absolutePosition + delta + noteLengthInTicks * this.gate())));
        compiler.noteProcessed();
        delta += arpegioDelay;
      }
      compiler.noteSetProcessed();
      relPosInStroke = Fraction.add(relPosInStroke, stroke.length());
    }
  }

  /**
   * Returns the length of this object.
   *
   * @return The length of a stroke.
   */
  public Fraction length() {
    return strokeSequence.length();
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

  /**
   * Returns a `PartMeasureParameters` object that defines default values of this `PartMeasure` object.
   *
   * @return A `PartMeasureParameters` object.
   * @see PartMeasureParameters
   */
  public PartMeasureParameters defaultValues() {
    return this.defaultValues;
  }

  List<Stroke> strokes() {
    return this.strokeSequence.strokes();
  }

  private static StrokeSequence parseStrokeSequence(String strokes, Fraction defaultNoteLength, NoteMap noteMap) {
    if (strokes == null) return new StrokeSequence(ZERO, defaultNoteLength, new LinkedList<>());
    List<Stroke> noteSets        = new LinkedList<>();
    Fraction     currentPosition = ZERO;
    Fraction     pickUpLength    = ZERO;
    for (String stroke : strokes.splitWithDelimiters("[;\\|]", 0)) {
      if (";".equals(stroke)) continue;
      if ("|".equals(stroke)) {
        pickUpLength    = currentPosition;
        currentPosition = ZERO;
        continue;
      }
      Stroke result = parseStroke(stroke, defaultNoteLength, noteMap);
      noteSets.add(new Stroke(result.length(), result.notes()));
      currentPosition = Fraction.add(currentPosition, result.length());
    }
    return new StrokeSequence(pickUpLength, currentPosition, noteSets);
  }

  private static Fraction resolveDefaultStrokeLength(JsonObject obj, PartMeasureParameters params) {
    Fraction len = params.length();
    if (CompatJsonUtils.hasPath(obj, Keyword.$length)) {
      len = validateLength(CompatJsonUtils.asJsonElement(obj, Keyword.$length));
    }
    return len;
  }

  private static double resolveGate(JsonObject obj, PartMeasureParameters params) {
    double gate = params.gate();
    if (CompatJsonUtils.hasPath(obj, Keyword.$gate)) {
      gate = CompatJsonUtils.asDouble(obj, Keyword.$gate);
    }
    return gate;
  }

  private static String resolveBankNumber(JsonObject obj) {
    final String bkno;
    if (CompatJsonUtils.hasPath(obj, Keyword.$bank)) {
      bkno = CompatJsonUtils.asString(obj, Keyword.$bank);
      // Checks if this.bkno can be parsed as a double value.
      assert bkno != null;
      //noinspection ResultOfMethodCallIgnored
      Double.parseDouble(bkno);
    } else bkno = null;
    return bkno;
  }

  private static long convertRelativePositionInPartMeasureToAbsolutePositionInTicks(MidiCompilerContext midiCompilerContext,
                                                                                    Fraction relativePositionInStroke) {
    return midiCompilerContext.groove().calculateAbsolutePositionInTicks(relativePositionInStroke,
                                                                         midiCompilerContext.relativeStrokePositionInBar(),
                                                                         midiCompilerContext.absoluteBarPositionInTicks());
  }

  private static long calculatePartMeasureLengthInTicks(PartMeasure partMeasure, MidiCompilerContext midiCompilerContext) {
    return convertRelativePositionInPartMeasureToAbsolutePositionInTicks(midiCompilerContext, partMeasure.length());
  }


  private static Fraction validateLength(JsonElement lenJSON) {
    Fraction len;
    if (lenJSON.isJsonPrimitive()) {
      len = parseNoteLength(lenJSON.getAsString());
      if (len == null) {
        throw illegalFormatException(lenJSON, NOTE_LENGTH_EXAMPLE);
      }
    } else {
      throw typeMismatchException(lenJSON, PRIMITIVE);
    }
    return len;
  }

  private static Stroke parseStroke(String nn, Fraction defaultNoteLength, NoteMap noteMap) {
    LinkedList<Note> ns = new LinkedList<>();
    return new Stroke(Optional.ofNullable(parseStroke(ns, nn, noteMap))
                              .map(PartMeasure::parseNoteLength)
                              .orElse(defaultNoteLength),
                      ns);
  }

  /**
   * Returns the 'length' portion of the string <code>stroke</code>.
   *
   * @param noteMap A note map that stores a map from  a code (`A`, `B`, `C`, ...) to a note.
   * @param out     A list that stores parsed result will be added.
   * @return The remaining part that was not parsed by this invocation.
   * `null` if this invocation parses the entire `notes` string.
   */
  private static String parseStroke(List<Note> out, String stroke, NoteMap noteMap) throws SymfonionException {
    Matcher m = NOTES_REGEX_PATTERN.matcher(stroke);
    int     i;
    for (i = 0; m.find(i); i = m.end()) {
      if (i != m.start()) {
        throw syntaxErrorInNotePattern(stroke, i, m);
      }
      int n_ = noteMap.noteFor(m.group("noteName"));
      if (n_ >= 0) {
        int  n  = n_ + Utils.count('#', m.group("accidentals")) - Utils.count('b', m.group("accidentals")) + Utils.count('>', m.group("octaveShifts")) * 12 - Utils.count('<', m.group("octaveShifts")) * 12;
        int  a  = Utils.count('+', m.group("accents")) - Utils.count('-', m.group("accents"));
        Note nn = new Note(n, a);
        out.add(nn);
      }
    }
    Matcher n   = NOTE_LENGTH_REGEX_PATTERN.matcher(stroke);
    String  ret = null;
    if (n.find(i)) {
      ret = stroke.substring(n.start(), n.end());
      i   = n.end();
    }
    if (i != stroke.length()) {
      throw illegalNoteFormat(stroke, i);
    }
    return ret;

  }

  private static void renderArrayedValues(int[] values, long pos, long partMeasureLength, MidiCompiler compiler, EventCreator creator) throws InvalidMidiDataException {
    if (values == null) {
      return;
    }
    long step = partMeasureLength / values.length;
    for (int i = 0; i < values.length; i++) {
      creator.createEvent(values[i], pos + step * i);
      compiler.controlEventProcessed();
    }
  }

  public static Fraction parseNoteLength(String length) {
    Matcher  m   = NOTE_LENGTH_REGEX_PATTERN.matcher(length);
    Fraction ret = null;
    if (m.matches()) {
      int l = Integer.parseInt(m.group("num"));
      ret = new Fraction(1, l);
      int dots = Utils.count('.', m.group("dots"));
      for (int i = 0; i < dots; i++) {
        l *= 2;
        ret = Fraction.add(ret, new Fraction(1, l));
      }
    } else if ("0".equals(length)) {
      ret = new Fraction(0, 1);
    }
    return ret;
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
      ret    = new int[1];
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
        if (p.isBoolean() || p.isNumber()) ret.add(p);
        else if (p.isString()) {
          String s = p.getAsString();
          for (int j = 0; j < s.length(); j++) {
            if (s.charAt(j) == '.') ret.add(JsonNull.INSTANCE);
            else throw syntaxErrorWhenExpandingDotsIn(arr);
          }
        }
      } else if (cur.isJsonNull()) ret.add(cur);
      else throw typeMismatchWhenExpandingDotsIn(arr);
    }
    return ret;
  }

  private static int[] interpolate(JsonArray arr) {
    int[]     ret;
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
    int end   = 0;
    for (int i = 0; i < tmp.length; i++) {
      if (tmp[i] != null) {
        start = ret[i] = tmp[i];
      } else {
        int j = i + 1;
        while (j < tmp.length) {
          if (tmp[j] != null) {
            end    = tmp[j];
            ret[j] = end;
            break;
          }
          j++;
        }
        int step   = (end - start) / (j - i);
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

  private record StrokeSequence(Fraction pickUpLength,
                                Fraction length,
                                List<Stroke> strokes) {
    StrokeSequence {
      assert preconditions(value(pickUpLength).satisfies(x -> x.toBe().notNull())
                                              .satisfies(x -> x.invokeStatic(Fraction.class, "compare", parameter(), ZERO)
                                                               .asInteger()
                                                               .toBe()
                                                               .greaterThanOrEqualTo(0)),
                           value(length).satisfies(x -> x.toBe().notNull())
                                        .satisfies(x -> x.invokeStatic(Fraction.class, "compare", parameter(), ZERO)
                                                         .asInteger()
                                                         .toBe()
                                                         .greaterThanOrEqualTo(0)),
                           value(strokes).toBe()
                                         .notNull());
    }
  }

  private record Stroke(Fraction length, List<Note> notes) {
  }
}
