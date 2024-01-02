package com.github.dakusui.symfonion.utils;



import com.github.dakusui.symfonion.exceptions.FractionFormatException;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.throwFractionFormatException;
import static com.github.dakusui.valid8j.Requires.requireArgument;
import static com.github.dakusui.valid8j_pcond.forms.Predicates.isEqualTo;
import static com.github.dakusui.valid8j_pcond.forms.Predicates.not;


/**
 * A class to implement simple Fraction functions
 * there is basically a constructor (which reduces)
 */
public record Fraction(int numerator, int denominator) implements Cloneable, Serializable {
  public static final Pattern fractionPattern = Pattern.compile("([0-9]+)/([1-9][0-9]*)");
  @Serial
  private static final long serialVersionUID = 9185757132113L;

  /* some useful constant fractions */
  public static final Fraction zero = new Fraction(0, 1);
  public static final Fraction one = new Fraction(1, 1);

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
    this.numerator = n;
    this.denominator = d;
  }

  public static Fraction parseFraction(String str) throws FractionFormatException {
    if (str == null) {
      return null;
    }
    Matcher m = fractionPattern.matcher(str);
    if (!m.matches()) {
      throw throwFractionFormatException(str);
    }
    return new Fraction(
        Integer.parseInt(m.group(1)),
        Integer.parseInt(m.group(2))
    );
  }

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

  public double doubleValue() {
    return (1.0 * this.numerator() / this.denominator());
  }

  public int wholePortion() {
    return (int) this.doubleValue();
  }

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

  public static Fraction add(Fraction f1, Fraction f2) {
    int n,
        d;

    n = f1.numerator * f2.denominator + f2.numerator * f1.denominator;
    d = f1.denominator * f2.denominator;
    return new Fraction(n, d);
  }

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

  public static Fraction div(Fraction f1, Fraction f2) {
    return new Fraction(f1.numerator * f2.denominator, f1.denominator * f2.numerator);
  }

  public static int compare(Fraction f1, Fraction f2) {
    Fraction sub = subtract(f1, f2);
    return sub.numerator * sub.denominator;
  }

  public static Fraction max(Fraction f1, Fraction f2) {
    if (f1.doubleValue() - f2.doubleValue() >= 0)
      return f1;
    else
      return f2;
  }

  public static Fraction min(Fraction f1, Fraction f2) {
    if (f1.doubleValue() - f2.doubleValue() <= 0)
      return f1;
    else
      return f2;
  }

}