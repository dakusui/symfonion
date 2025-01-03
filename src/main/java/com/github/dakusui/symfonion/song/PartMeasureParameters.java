package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.compat.json.CompatJsonException;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.utils.Fraction;
import com.github.dakusui.symfonion.utils.Utils;
import com.google.gson.JsonObject;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.illegalFormatException;
import static com.github.dakusui.symfonion.compat.exceptions.SymfonionIllegalFormatException.NOTE_LENGTH_EXAMPLE;
import static com.github.dakusui.symfonion.compat.json.CompatJsonUtils.asJsonElement;

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
public class PartMeasureParameters {
  final double   gate;
  final Fraction length;
  final int      transpose;
  final int      velocityBase;
  final int      velocityDelta;
  final int      arpeggio;

  public PartMeasureParameters(JsonObject json) throws SymfonionException, CompatJsonException {
    if (json == null) {
      json = CompatJsonUtils.toJson("{}").getAsJsonObject();
    }
    this.velocityBase  = CompatJsonUtils.asIntWithDefault(json, 64, Keyword.$velocitybase);
    this.velocityDelta = CompatJsonUtils.asIntWithDefault(json, 5, Keyword.$velocitydelta);
    this.gate          = CompatJsonUtils.asDoubleWithDefault(json, 0.8, Keyword.$gate);
    this.length        = Utils.parseNoteLength(CompatJsonUtils.asStringWithDefault(json, "16", Keyword.$length));
    if (this.length == null) {
      throw illegalFormatException(asJsonElement(json, Keyword.$length), NOTE_LENGTH_EXAMPLE);
    }
    this.transpose = CompatJsonUtils.asIntWithDefault(json, 0, Keyword.$transpose);
    this.arpeggio  = CompatJsonUtils.asIntWithDefault(json, 0, Keyword.$arpeggio);
  }

  /**
   * Returns a `$gate` value.
   *
   * @return a `$gate` value
   */
  public double gate() {
    return this.gate;
  }

  /**
   * Returns a `$length` value.
   *
   * @return a `$length` value
   */
  public Fraction length() {
    return this.length;
  }

  /**
   * Returns a `$transpose` value.
   *
   * @return a `$transpose` value
   */
  public int transpose() {
    return this.transpose;
  }

  /**
   * Returns a `$velocitybase` value.
   *
   * @return a `$velocitybase` value
   */
  public int velocityBase() {
    return this.velocityBase;
  }

  /**
   * A step value with which the velocity of a part measure is increased for a single accent sign.
   *
   * @return A step value for a velocity of a part measure.
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
