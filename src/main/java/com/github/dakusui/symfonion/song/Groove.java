package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.utils.Fraction;
import com.github.dakusui.symfonion.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.illegalFormatException;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.typeMismatchException;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException.NOTE_LENGTH_EXAMPLE;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionTypeMismatchException.OBJECT;
import static com.github.dakusui.symfonion.utils.Fraction.ZERO;
import static com.github.valid8j.classic.Requires.requireNonNull;
import static java.util.Collections.emptyList;

/**
 * A class that models a musical "groove", which gives slightly different stresses and lengths of notes in a score.
 * A groove is modeled as a sequence of beats.
 */
public class Groove {
  /**
   * A default instance of this class.
   */
  public static final Groove DEFAULT_INSTANCE = new Groove(emptyList());

  private final List<Beat> beats;

  private final int resolution;


  /**
   * Creates an object of this class.
   *
   * @param beats Beats with which this groove is defined.
   */
  Groove(List<Beat> beats) {
    this.resolution = 384;
    this.beats      = requireNonNull(beats);
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
    /*
    assert preconditions(value(offset).toBe().notNull(),
                         value(offset).invokeStatic(Fraction.class, "compare",
                                                    parameter(),
                                                    ZERO)
                                      .asInteger()
                                      .toBe().greaterThanOrEqualTo(0));
     */
    long pos = 0;

    Fraction rest = foldPosition(offset.clone(), this.length());
    Fraction shift = offset.isNegative() ? Fraction.subtract(rest, offset)
                                         : ZERO;
    int i = 0;
    while (Fraction.compare(rest, ZERO) > 0) {
      if (i >= this.beats.size()) {
        break;
      }
      Beat beat = this.beats.get(i);
      rest = Fraction.subtract(rest, beat.length);
      pos += beat.ticks;
      i++;
    }
    long p;
    int  d = 0;
    if (Fraction.compare(rest, ZERO) < 0) {
      Beat beat = this.beats.get(i - 1);
      p = (long) (pos + Fraction.div(rest, beat.length).doubleValue() * beat.ticks);
    } else if (Fraction.compare(rest, ZERO) == 0) {
      if (i < this.beats.size()) {
        d = this.beats.get(i).accent;
      }
      p = pos;
    } else {
      p = (pos + (long) (rest.doubleValue() * this.resolution));
    }
    return new Unit(p - (long) (shift.doubleValue() * this.resolution), d);
  }

  private static Fraction foldPosition(Fraction position, Fraction grooveLength) {
    Fraction ret = position;
    while (ret.isNegative())
      ret = Fraction.add(grooveLength, ret);
    return ret;
  }

  /**
   * Creates a new `Groove` object from a given JsonArray object.
   *
   * The `grooveDef` should look like as follows:
   *
   * [source, JSON]
   * ----
   * [
   * {
   * "$length": "1/16",
   * "$ticks": 24,
   * "$accent": 10
   * },
   * "..."
   * ]
   * ----
   *
   * @param grooveDef A definition of a groove.
   * @return A new groove object
   */
  public static Groove createGroove(JsonArray grooveDef) {
    Groove.Builder b = new Groove.Builder();
    for (JsonElement elem : grooveDef) {
      if (!elem.isJsonObject()) {
        throw typeMismatchException(elem, OBJECT);
      }
      JsonObject cur    = elem.getAsJsonObject();
      String     len    = CompatJsonUtils.asString(cur, Keyword.$length);
      long       ticks  = CompatJsonUtils.asLong(cur, Keyword.$ticks);
      int        accent = CompatJsonUtils.asInt(cur, Keyword.$accent);

      Fraction f = Utils.parseNoteLength(len);
      if (f == null) {
        throw illegalFormatException(CompatJsonUtils.asJsonElement(cur, Keyword.$length), NOTE_LENGTH_EXAMPLE);
      }
      b.add(f, ticks, accent);
    }
    return b.build();
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

