package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.compat.json.JsonFormatException;
import com.github.dakusui.symfonion.compat.json.JsonInvalidPathException;
import com.github.dakusui.symfonion.compat.json.JsonTypeMismatchException;
import com.github.dakusui.symfonion.utils.Fraction;
import com.github.dakusui.symfonion.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException.NOTE_LENGTH_EXAMPLE;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionTypeMismatchException.OBJECT;

/**
 * A class that models a musical "groove", which gives slightly different stresses and lengths of notes in a score.
 */
public class Groove {
  /**
   * A default instance of this class.
   */
  public static final Groove DEFAULT_INSTANCE = new Groove();

  record Beat(Fraction length, long ticks, int accent) {
  }

  public record Unit(long pos, int accentDelta) {
    public int accent() {
      return this.accentDelta;
    }
  }

  List<Beat> beats = new LinkedList<>();

  private final int resolution;

  public Groove() {
    this(384);
  }

  public Groove(int resolution) {
    this.resolution = resolution;
  }

  public Unit resolve(Fraction offset) {
    if (offset == null) {
      String msg = "offset cannot be null. (Groove#resolve)";
      throw runtimeException(msg, null);
    }
    if (Fraction.compare(offset, Fraction.zero) < 0) {
      String msg = "offset cannot be negative. (Groove#resolve)";
      throw runtimeException(msg, null);
    }
    long pos = 0;

    Fraction rest = offset.clone();
    int i = 0;
    while (Fraction.compare(rest, Fraction.zero) > 0) {
      if (i >= this.beats.size()) {
        break;
      }
      Beat beat = this.beats.get(i);
      rest = Fraction.subtract(rest, beat.length);
      pos += beat.ticks;
      i++;
    }
    long p;
    int d = 0;
    if (Fraction.compare(rest, Fraction.zero) < 0) {
      Beat beat = this.beats.get(i - 1);
      p = (long) (pos + Fraction.div(rest, beat.length).doubleValue() * beat.ticks);
    } else if (Fraction.compare(rest, Fraction.zero) == 0) {
      if (i < this.beats.size()) {
        d = this.beats.get(i).accent;
      }
      p = pos;
    } else {
      p = (pos + (long) (rest.doubleValue() * this.resolution));
    }
    return new Unit(p, d);
  }

  public void add(Fraction length, long ticks, int accent) {
    if (this == DEFAULT_INSTANCE) {
      throw runtimeException("Groove.DEFAULT_INSTANCE is immutable.", null);
    }
    beats.add(new Beat(length, ticks, accent));
  }

  public static Groove createGroove(JsonArray grooveDef) throws SymfonionException, JsonTypeMismatchException, JsonInvalidPathException, JsonFormatException {
    Groove ret = new Groove();
    for (JsonElement elem : grooveDef) {
      if (!elem.isJsonObject()) {
        throw typeMismatchException(elem, OBJECT);
      }
      JsonObject cur = elem.getAsJsonObject();
      String len = CompatJsonUtils.asString(cur, Keyword.$length);
      long ticks = CompatJsonUtils.asLong(cur, Keyword.$ticks);
      int accent = CompatJsonUtils.asInt(cur, Keyword.$accent);

      Fraction f = Utils.parseNoteLength(len);
      if (f == null) {
        throw illegalFormatException(CompatJsonUtils.asJsonElement(cur, Keyword.$length), NOTE_LENGTH_EXAMPLE);
      }
      ret.add(f, ticks, accent);
    }
    return ret;
  }
}

