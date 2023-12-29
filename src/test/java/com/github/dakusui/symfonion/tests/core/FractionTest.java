package com.github.dakusui.symfonion.tests.core;

import static org.junit.Assert.*;

import com.github.dakusui.symfonion.core.Fraction;
import org.junit.Assert;
import org.junit.Test;

public class FractionTest {
	@Test
	public void test_compare01() {
		Assert.assertEquals(0, Fraction.compare(Fraction.one, Fraction.one));
	}
	
	@Test
	public void test_compare_02() {
		assertTrue(Fraction.compare(new Fraction(1, 2), Fraction.one) < 0);
	}
	
	@Test
	public void test_compare_03() {
		assertEquals(0, Fraction.compare(Fraction.zero, Fraction.subtract(new Fraction(1, 16), new Fraction(1, 16))));
	}

	@Test
	public void test_subtract_02() {
		assertEquals(Double.valueOf(-0.5), Double.valueOf(Fraction.subtract(new Fraction(1, 2), Fraction.one).doubleValue()));
	}
}
