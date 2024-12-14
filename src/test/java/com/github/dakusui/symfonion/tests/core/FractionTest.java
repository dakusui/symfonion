package com.github.dakusui.symfonion.tests.core;

import com.github.dakusui.symfonion.utils.Fraction;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class FractionTest {
  public static void main(String[] args) {
    Fraction f1,
        f2,
        s;

    System.out.println("duh");
    f1 = new Fraction(3, 7);
    System.out.println("f1: " + f1);
    f2 = new Fraction(18, 4);
    System.out.println("f1: " + f1);
    System.out.println("f2: " + f2);
    s = Fraction.add(f1, f2);
    System.out.println("f1+f2: " + s);
    f2 = new Fraction(18, 1);
    System.out.println("18/1: " + f2);
  }

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
    assertEquals(0, Fraction.compare(Fraction.ZERO, Fraction.subtract(new Fraction(1, 16), new Fraction(1, 16))));
  }

  @Test
  public void test_subtract_02() {
    assertEquals(Double.valueOf(-0.5), Double.valueOf(Fraction.subtract(new Fraction(1, 2), Fraction.one).doubleValue()));
  }

  @Test
  public void test_multiply_01() {
    int a = 2;
    int b = 3;
    assertEquals(1.0d, Fraction.multi(new Fraction(a, b), new Fraction(b, a)).doubleValue(), 0.001d);
  }

  @Test
  public void test_fractionPortion_91() {
    int a = 4;
    int b = 3;
    assertEquals(0.333333d, new Fraction(a, b).fractionPortion().doubleValue(), 0.001d);
  }

  @Test
  public void test_toString() {
    int a = 3;
    int b = 5;
    assertEquals("3/5", new Fraction(a, b).toString());
  }

  @Test
  public void test_max_1() {
    int a = 3;
    int b = 5;
    assertEquals(new Fraction(b, a), Fraction.max(new Fraction(a, b), new Fraction(b, a)));
  }

  @Test
  public void test_max_2() {
    int a = 3;
    int b = 5;
    assertEquals(new Fraction(b, a), Fraction.max(new Fraction(b, a), new Fraction(a, b)));
  }

  @Test
  public void test_min_1() {
    int a = 3;
    int b = 5;
    assertEquals(new Fraction(a, b), Fraction.min(new Fraction(a, b), new Fraction(b, a)));
  }

  @Test
  public void test_min_2() {
    int a = 3;
    int b = 5;
    assertEquals(new Fraction(a, b), Fraction.min(new Fraction(b, a), new Fraction(a, b)));
  }

  @Test
  public void test_equals_1() {
    int a = 3;
    int b = 13;
    assertEquals(new Fraction(a, b), new Fraction(a, b));
  }

  @Test
  public void test_equals_2() {
    int a = 3;
    int b = 13;
    assertEquals(new Fraction(a, b), new Fraction(a * 2, b * 2));
  }

  @Test
  public void test_equals_3() {
    int a = 3;
    int b = 13;
    assertNotEquals(new Fraction(a, b), new Fraction(a, b + 1));
  }

  /**
   * This test examines if equalTo works with non-Fraction object.
   */
  @SuppressWarnings({"SimplifiableAssertion", "EqualsBetweenInconvertibleTypes"})
  @Test
  public void test_equals_4() {
    int a = 1;
    int b = 1;
    assertFalse(new Fraction(a, b).equals(1));
  }

  @Test
  public void test_hashCode_1() {
    int a = 3;
    int b = 13;
    assertEquals(new Fraction(a, b).hashCode(), new Fraction(a, b).hashCode());
  }

  @Test
  public void test_hashCode_2() {
    int a = 3;
    int b = 13;
    assertEquals(new Fraction(a, b).hashCode(), new Fraction(a * 2, b * 2).hashCode());
  }

  @Test
  public void test_hashCode_3() {
    int a = 3;
    int b = 13;
    assertNotEquals(new Fraction(a, b).hashCode(), new Fraction(a, b + 1).hashCode());
  }
}
