package com.github.dakusui.symfonion.utils;


import com.github.dakusui.symfonion.compat.exceptions.FractionFormatException;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.throwFractionFormatException;
import static com.github.valid8j.classic.Requires.requireArgument;
import static com.github.valid8j.pcond.forms.Predicates.isEqualTo;
import static com.github.valid8j.pcond.forms.Predicates.not;


/**
 * A class to implement simple Fraction functions
 * there is basically a constructor (which reduces)
 *
 * @param numerator   A numerator of a fraction
 * @param denominator A denominator of a fraction
 */
public record Fraction(int numerator, int denominator) implements Cloneable, Serializable {
  public static final  Pattern FRACTION_PATTERN = Pattern.compile("([0-9]+)/([1-9][0-9]*)");
  @Serial
  private static final long    serialVersionUID = 9185757132113L;

  /* some useful constant fractions */
  public static final Fraction ZERO = new Fraction(0, 1);
  public static final Fraction one  = new Fraction(1, 1);

  /**
   * Creates an object of this class.
   *
   * @param numerator   A numerator
   * @param denominator A denominator
   */
  public Fraction(int numerator, int denominator) {
    requireArgument(denominator, not(isEqualTo(0)));
    int n = numerator;
    int d = denominator;
    int gcd;

    while (true) {
      gcd = this.gcd(n, d);
      if (gcd == 1)
        break;
      n /= gcd;
      d /= gcd;
    }
    this.numerator   = n;
    this.denominator = d;
  }

  /**
   * Parses a given string `str` and creates a `Fraction` object
   * The str must match with a regular expression:
   *
   * - `([0-9]+)/([1-9][0-9]*)`
   *
   * If it doesn't, A `FractionFormatException` will be thrown.
   *
   * @param str A string containing a fraction.
   * @return A `Fraction` object.
   * @see FractionFormatException
   */
  public static Fraction parseFraction(String str) throws FractionFormatException {
    if (str == null) {
      return null;
    }
    Matcher m = FRACTION_PATTERN.matcher(str);
    if (!m.matches()) {
      throw throwFractionFormatException(str);
    }
    return new Fraction(
        Integer.parseInt(m.group(1)),
        Integer.parseInt(m.group(2))
    );
  }

  /**
   * Creates a clone of this object
   *
   * @return A cloned object.
   */
  @Override
  public Fraction clone() {
    Fraction ret;
    try {
      ret = (Fraction) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
    return ret;
  }

  private int gcd(int a, int b) {
    int t;

    while (b != 0) {
      t = a;
      a = b;
      b = t % a;
    }
    return (a);
  }

  /**
   * Returns a double representation of this object.
   *
   * @return value of this objet in `double`.
   */
  public double doubleValue() {
    return (1.0 * this.numerator() / this.denominator());
  }

  /**
   * Returns a whole portion of this fraction.
   *
   * @return A whole portion of this fraction.
   */
  public int wholePortion() {
    return (int) this.doubleValue();
  }

  /**
   * Returns a fraction portion of this fraction.
   *
   * @return A fraction portion of this fraction.
   */
  public Fraction fractionPortion() {
    Fraction f = new Fraction(this.wholePortion(), 1);
    return subtract(this, f);
  }

  @Override
  public boolean equals(Object anotherObject) {
    if (!(anotherObject instanceof Fraction another))
      return false;
    return this.denominator == another.denominator && this.numerator == another.numerator;
  }

  @Override
  public String toString() {
    return (this.numerator() + "/" + this.denominator());
  }

  /**
   * Returns the addition of `f1` and `f2`.
   *
   * @param f1 A first fraction
   * @param f2 A second fraction
   * @return Result of the addition.
   */
  public static Fraction add(Fraction f1, Fraction f2) {
    int n,
        d;

    n = f1.numerator * f2.denominator + f2.numerator * f1.denominator;
    d = f1.denominator * f2.denominator;
    return new Fraction(n, d);
  }

  /**
   * Returns the subtraction of `f1` and `f2
   *
   * @param f1 A fraction
   * @param f2 A fraction to be subtracted from `f1`.
   * @return Result of the subtraction
   */
  public static Fraction subtract(Fraction f1, Fraction f2) {
    int n,
        d;

    n = f1.numerator * f2.denominator - f2.numerator * f1.denominator;
    d = f1.denominator * f2.denominator;
    return new Fraction(n, d);
  }


  /**
   * Multiplies two fractions.
   *
   * @param f1 A fraction.
   * @param f2 Another fraction.
   * @return A multiplied result.
   */
  public static Fraction multi(Fraction f1, Fraction f2) {
    return new Fraction(f1.numerator * f2.numerator, f1.denominator * f2.denominator);
  }

  /**
   * Divides `f1` by `f2`.
   *
   * @param f1 A fraction to be divided.
   * @param f2 A divisor.
   * @return Result of the division.
   */
  public static Fraction div(Fraction f1, Fraction f2) {
    return new Fraction(f1.numerator * f2.denominator, f1.denominator * f2.numerator);
  }

  /**
   * Returns the comparison result.
   *
   * @param f1 A fraction.
   * @param f2 A fraction to be compared with `f1`.
   * @return negative - `f1` is little than `f2`/ 0 - `f1` and `f2` are equal/ positive - `f1`is greater than `f2`.
   */
  public static int compare(Fraction f1, Fraction f2) {
    Fraction sub = subtract(f1, f2);
    return sub.numerator * sub.denominator;
  }

  /**
   * Returns a greater one from `f1`and `f2`.
   * When `f1` and `f2`, either of them may be returned.
   *
   * @param f1 A fraction
   * @param f2 Another fraction
   * @return Greater one of `f1` and `f2`.
   */
  public static Fraction max(Fraction f1, Fraction f2) {
    if (f1.doubleValue() - f2.doubleValue() >= 0)
      return f1;
    else
      return f2;
  }

  /**
   * Returns a non-greater fraction from `f1` and `f2`.
   *
   * @param f1 A fraction
   * @param f2 Another fraction
   * @return A non-greater fraction.
   */
  public static Fraction min(Fraction f1, Fraction f2) {
    if (f1.doubleValue() - f2.doubleValue() <= 0)
      return f1;
    else
      return f2;
  }
}