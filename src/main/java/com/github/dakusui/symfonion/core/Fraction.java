package com.github.dakusui.symfonion.core;

/*
 * A class to implement simple Fraction functions
 * there is basically a constructor (which reduces)
 */


import java.io.Serializable;


public class Fraction extends Object implements Cloneable, Serializable
{
	static final	long	serialVersionUID = 9185757132113L;

	/* some useful constant fractions */
	public static final	Fraction	zero = new Fraction(0, 1);
	public static final	Fraction	one = new Fraction(1, 1);
	public static final	Fraction	two = new Fraction(2, 1);
	public static final	Fraction	three = new Fraction(3, 1);
	public static final	Fraction	four = new Fraction(4, 1);

	private int numer = 0;
	private int denom = 0;

	public Fraction(int numer, int denom) {
		if (denom == 0)
			throw new NumberFormatException("denominator is zero");
		this.numer = numer;
		this.denom = denom;
		this.reduce();
	}

	public Object clone() {
		return new Fraction(this.numer, this.denom);
	}

	public int getNumerator() {
		return this.numer;
	}

	public int getDenominator() {
		return this.denom;
	}

	public void reduce() {
		int	d;

		while (true) {
			d = this.gcd(this.numer, this.denom);
			if (d == 1)
				return;
			this.numer /= d;
			this.denom /= d;
		}
	}

	private int gcd(int a, int b) {
		int	t;

		while (b != 0) {
			t = a;
			a = b;
			b = t%a;
		}
		return (a);
	}

	public String toString() {
		return ("(" + this.numer + "/" + this.denom + ")");
	}

	public double doubleValue() {
		return (1.0*this.numer/this.denom);
	}

	public int wholePortion() {
		return (int)this.doubleValue();
	}

	public Fraction fractionPortion() {
		Fraction f = new Fraction(this.wholePortion(), 1);
		return subtract(this, f);
	}

	public static Fraction add(Fraction f1, Fraction f2) {
		int	n,
			d;

		n = f1.numer*f2.denom + f2.numer*f1.denom;
		d = f1.denom*f2.denom;
		return new Fraction(n, d);
	}

	public static Fraction subtract(Fraction f1, Fraction f2) {
		int	n,
			d;

		n = f1.numer*f2.denom - f2.numer*f1.denom;
		d = f1.denom*f2.denom;
		return new Fraction(n, d);
	}


	public static Fraction addCoef(int c1, Fraction f1,
	                               int c2, Fraction f2) {
		int	n,
			d;

		n = c1*f1.numer*f2.denom + c2*f2.numer*f1.denom;
		d = f1.denom*f2.denom;
		return new Fraction(n, d);
	}

	public static Fraction mult(Fraction f1, Fraction f2) {
		return new Fraction(f1.numer*f2.numer, f1.denom*f2.denom);
	}

	public static Fraction div(Fraction f1, Fraction f2) {
		return new Fraction(f1.numer*f2.denom, f1.denom*f2.numer);
	}

	public static int compare(Fraction f1, Fraction f2) {
		double	delta;

		if (f1.numer == f2.numer && f1.denom == f2.denom)
			return (0);
		delta = f1.doubleValue() - f2.doubleValue();
		if (delta > 0)
			return 1;
		else if (delta < 0)
			return -1;
		else
			throw new IllegalStateException();
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

	public static void main(String[] args) {
		Fraction	f1,
				f2,
				s;

		System.out.println("duh");
		f1 = new Fraction(3, 7);
		System.out.println("f1: " + f1.toString());
		f2 = new Fraction(18, 4);
		System.out.println("f1: " + f1.toString());
		System.out.println("f2: " + f2.toString());
		s = Fraction.add(f1, f2);
		System.out.println("f1+f2: " + s.toString());
		s = Fraction.addCoef(3, f1, -1, f2);
		System.out.println("3*f1+f2: " + s.toString());
		f2 = new Fraction(18, 1);
	}
}