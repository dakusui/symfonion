package com.github.dakusui.symfonion.tests.song;

import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.song.Groove;
import com.github.dakusui.symfonion.song.PartMeasure;
import com.github.dakusui.symfonion.testutils.TestBase;
import com.github.dakusui.symfonion.utils.Fraction;
import org.junit.Test;

import static com.github.dakusui.symfonion.song.PartMeasure.parseNoteLength;
import static org.junit.Assert.assertEquals;

public class GrooveTest extends TestBase {

  @Test
  public void test_A01() throws SymfonionException {
    Groove      groove = testGrooveA();
    Groove.Unit unit;

    unit = groove.resolve(Fraction.ZERO);
    assertEquals(0, unit.pos());
    assertEquals(1, unit.accentDelta());
  }

  @Test
  public void test_A02() throws SymfonionException {
    Groove      groove = testGrooveA();
    Groove.Unit unit;

    unit = groove.resolve(new Fraction(1, 32));
    assertEquals(12, unit.pos());
    assertEquals(0, unit.accentDelta());
  }

  @Test
  public void test_A03() throws SymfonionException {
    Groove      groove = testGrooveA();
    Groove.Unit unit;

    unit = groove.resolve(parseNoteLength("8"));
    assertEquals(48, unit.pos());
    assertEquals(3, unit.accentDelta());
  }

  @Test
  public void test_A04() throws SymfonionException {
    Groove      groove = testGrooveA();
    Groove.Unit unit;

    unit = groove.resolve(parseNoteLength("8."));
    assertEquals(72, unit.pos());
    assertEquals(4, unit.accentDelta());
  }

  @Test
  public void test_A05() throws SymfonionException {
    Groove      groove = testGrooveA();
    Groove.Unit unit;

    unit = groove.resolve(parseNoteLength("4"));
    assertEquals(96, unit.pos());
    assertEquals(0, unit.accentDelta());
  }

  @Test
  public void test_A06() throws SymfonionException {
    Groove      groove = testGrooveA();
    Groove.Unit unit;

    unit = groove.resolve(parseNoteLength("2"));
    assertEquals(192, unit.pos());
    assertEquals(0, unit.accentDelta());
  }

  Groove testGrooveA() throws SymfonionException {
    return new Groove.Builder().add(PartMeasure.parseNoteLength("16"), 24, 1)
                               .add(PartMeasure.parseNoteLength("16"), 24, 2)
                               .add(PartMeasure.parseNoteLength("16"), 24, 3)
                               .add(PartMeasure.parseNoteLength("16"), 24, 4)
                               .build();
  }

  @Test
  public void test_B01() throws SymfonionException {
    System.out.println("hello");
    Groove      groove = testGrooveB();
    Groove.Unit unit;

    unit = groove.resolve(Fraction.ZERO);
    assertEquals(0, unit.pos());
    assertEquals(1, unit.accentDelta());
  }

  @Test
  public void test_B02() throws SymfonionException {
    Groove      groove = testGrooveB();
    Groove.Unit unit;

    unit = groove.resolve(new Fraction(1, 32));
    assertEquals(13, unit.pos());
    assertEquals(0, unit.accentDelta());
  }

  @Test
  public void test_B03a() throws SymfonionException {
    Groove      groove = testGrooveB();
    Groove.Unit unit;

    unit = groove.resolve(parseNoteLength("16"));
    assertEquals(26, unit.pos());
    assertEquals(2, unit.accentDelta());
  }

  @Test
  public void test_B03b() throws SymfonionException {
    Groove      groove = testGrooveB();
    Groove.Unit unit;

    unit = groove.resolve(parseNoteLength("8"));

    assertEquals(48, unit.pos());
    assertEquals(3, unit.accentDelta());
  }

  @Test
  public void test_B04() throws SymfonionException {
    Groove      groove = testGrooveB();
    Groove.Unit unit;

    unit = groove.resolve(parseNoteLength("8."));
    assertEquals(73, unit.pos());
    assertEquals(4, unit.accentDelta());
  }

  @Test
  public void test_B05() throws SymfonionException {
    Groove      groove = testGrooveB();
    Groove.Unit unit;

    unit = groove.resolve(parseNoteLength("4"));
    assertEquals(96, unit.pos());
    assertEquals(0, unit.accentDelta());
  }

  @Test
  public void test_B06() throws SymfonionException {
    Groove      groove = testGrooveB();
    Groove.Unit unit;

    unit = groove.resolve(parseNoteLength("2"));
    assertEquals(192, unit.pos());
    assertEquals(0, unit.accentDelta());
  }

  Groove testGrooveB() throws SymfonionException {
    return new Groove.Builder().add(PartMeasure.parseNoteLength("16"), 26, 1)
                               .add(PartMeasure.parseNoteLength("16"), 22, 2)
                               .add(PartMeasure.parseNoteLength("16"), 25, 3)
                               .add(PartMeasure.parseNoteLength("16"), 23, 4)
                               .build();
  }

  Groove createGroove() {
    return new Groove.Builder()
        .add(PartMeasure.parseNoteLength("16"), 26, 1)
        .add(PartMeasure.parseNoteLength("16"), 22, 2)
        .add(PartMeasure.parseNoteLength("16"), 25, 3)
        .add(PartMeasure.parseNoteLength("16"), 23, 4)
        .build();
  }
}
