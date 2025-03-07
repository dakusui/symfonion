package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.utils.Fraction;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.illegalFormatException;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException.NOTE_LENGTH_EXAMPLE;
import static com.github.dakusui.symfonion.utils.Fraction.ZERO;
import static com.github.valid8j.classic.Requires.requireNonNull;
import static com.github.valid8j.fluent.Expectations.*;
import static com.github.valid8j.pcond.forms.Functions.parameter;
import static java.util.Collections.singletonList;

/**
 * A class that models a musical "groove", which gives slightly different stresses and lengths of notes in a score.
 * A groove is modeled as a sequence of beats.
 */
public class Groove {
  public static final int        TICKS_FOR_QUARTER_NOTE = 384;
  private final       List<Beat> beats;

  private final int resolution;


  /**
   * Creates an object of this class.
   *
   * @param beats Beats with which this groove is defined.
   */
  Groove(List<Beat> beats) {
    this.resolution = TICKS_FOR_QUARTER_NOTE;
    this.beats      = requireNonNull(beats);
  }

  public int calculateGrooveAccent(Fraction relPosInStroke, Fraction relativePositionInBar) {
    Unit unit = resolveRelativePositionInStroke(this, relPosInStroke, relativePositionInBar);
    return unit.accentDelta();
  }

  public long calculateAbsolutePositionInTicks(Fraction relativePositionInStroke, Fraction relativePositionInBar, long barPositionInTicks) {
    Unit unit                         = resolveRelativePositionInStroke(this, relativePositionInStroke, relativePositionInBar);
    long relativePositionInBarInTicks = unit.pos();
    return barPositionInTicks + relativePositionInBarInTicks;
  }

  public Fraction length() {
    Fraction ret = ZERO;
    for (Beat beat : beats) {
      ret = Fraction.add(ret, beat.length());
    }
    return ret;
  }

  /**
   * Resolves a position and accent, where a note at the given offset from a bar should be played, if this `Groove` object
   * is applied.
   *
   * @param offset An offset from the beginning of a bar.
   * @return An object that indicates how a note at `offset` should be played.
   */
  public Unit resolve(Fraction offset) {
    assert preconditions(value(offset).toBe().notNull());
    return computeUnit(offset, this.length(), this.beats, this.resolution);
  }

  private static Unit computeUnit(Fraction offset, Fraction grooveLength, List<Beat> grooveBeats, int grooveResolution) {
    Fraction rest = foldPosition(offset.clone(), grooveLength);
    long     pos  = 0;
    Fraction shift = offset.isNegative() ? Fraction.subtract(rest, offset) : ZERO;
    int i = 0;
    while (Fraction.compare(rest, ZERO) > 0) {
      if (i >= grooveBeats.size()) {
        break;
      }
      Beat beat = grooveBeats.get(i);
      rest = Fraction.subtract(rest, beat.length);
      pos += beat.ticks;
      i++;
    }
    long p;
    int  d = 0;
    if (Fraction.compare(rest, ZERO) == 0) {
      if (i < grooveBeats.size()) {
        d = grooveBeats.get(i).accent;
      }
      p = pos;
    } else if (Fraction.compare(rest, ZERO) < 0) {
      Beat beat = grooveBeats.get(i - 1);
      p = (long) (pos + Fraction.div(rest, beat.length).doubleValue() * beat.ticks);
    } else {
      p = (pos + (long) (rest.doubleValue() * grooveResolution));
    }
    return new Unit(p - (long) (shift.doubleValue() * grooveResolution), d);
  }

  private static Fraction foldPosition(Fraction position, Fraction grooveLength) {
    assert preconditions(value(position).toBe().notNull(), value(grooveLength).toBe().notNull(), value(grooveLength).invokeStatic(Fraction.class, "compare", parameter(), ZERO).asInteger().toBe().greaterThan(0));
    Fraction ret = position;
    while (ret.isNegative()) ret = Fraction.add(grooveLength, ret);
    assert postconditions(value(ret).toBe().notNull(), value(ret).invokeStatic(Fraction.class, "compare", parameter(), ZERO).asInteger().toBe().greaterThanOrEqualTo(0));
    return ret;
  }

  /**
   * // @formatter:off
   * Creates a new `Groove` object from a given JsonArray object.
   *
   * The `grooveDef` should look like as follows:
   *
   * [source, JSON]
   * .grooveDef
   * ----
   * [
   *   {
   *     "$length": "1/16",
   *     "$ticks": 24,
   *     "$accent": 10
   *   },
   *   "..."
   * ]
   * ----
   *
   * @param grooveDef A definition of a groove.
   * @return A new groove object
   * // @formatter:on
   */
  public static Groove createGroove(JsonArray grooveDef) {
    Groove.Builder b = new Groove.Builder();
    for (JsonElement elem : grooveDef) {
      JsonObject cur    = CompatJsonUtils.requireJsonObject(elem);
      String     len    = CompatJsonUtils.asString(cur, Keyword.$length);
      long       ticks  = CompatJsonUtils.asLong(cur, Keyword.$ticks);
      int        accent = CompatJsonUtils.asInt(cur, Keyword.$accent);

      Fraction f = PartMeasure.parseNoteLength(len);
      if (f == null) {
        throw illegalFormatException(CompatJsonUtils.asJsonElement(cur, Keyword.$length), NOTE_LENGTH_EXAMPLE);
      }
      b.add(f, ticks, accent);
    }
    return b.build();
  }

  static Groove defaultGrooveOf(Fraction barLength) {
    return new Groove(singletonList(new Beat(barLength, (long) Fraction.multi(barLength, new Fraction(TICKS_FOR_QUARTER_NOTE, 1)).doubleValue(), 0)));
  }

  private static Unit resolveRelativePositionInStroke(Groove groove, Fraction relativePositionInStroke, Fraction relativePositionInBar) {
    return groove.resolve(Fraction.add(relativePositionInBar, relativePositionInStroke));
  }

  /**
   * A builder for `Groove` class.
   */
  public static class Builder {
    final List<Beat> beats = new LinkedList<>();

    /**
     * Creates an object of this class.
     */
    public Builder() {
    }

    /**
     * Adds a beat to this groove.
     *
     * @param fraction A "logical" length of a beat.
     * @param ticks    Indicates how many ticks should the `fraction` be played
     * @param accent   A delta how should the `beat` be stressed. This can be negative.
     * @return This object.
     */
    public Builder add(Fraction fraction, long ticks, int accent) {
      beats.add(new Beat(fraction, ticks, accent));
      return this;
    }

    /**
     * Builds an object of `Groove`.
     *
     * @return A new `Groove` object.
     */
    public Groove build() {
      return new Groove(beats);
    }
  }

  /**
   * A record that models each element in a groove.
   *
   * @param length A length of a beat on a score.
   * @param ticks  Actual length of the beat in ticks.
   * @param accent An accent of the groove.
   */
  record Beat(Fraction length, long ticks, int accent) {
  }

  /**
   * A class that stores a result of {@link Groove#resolve(Fraction)}.
   *
   * @param pos         A position at which a given note should be played. Ticks from the beginning of the bar.
   * @param accentDelta A delta that indicates how much a given note should be stressed.
   */
  public record Unit(long pos, int accentDelta) {
  }
}

