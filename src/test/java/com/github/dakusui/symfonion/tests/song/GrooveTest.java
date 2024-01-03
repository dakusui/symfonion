package com.github.dakusui.symfonion.tests.song;

import static org.junit.Assert.*;

import com.github.dakusui.symfonion.song.Groove;
import com.github.dakusui.symfonion.testutils.TestBase;
import org.junit.Test;

import com.github.dakusui.symfonion.utils.Fraction;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.utils.Utils;

import static com.github.dakusui.symfonion.utils.Utils.parseNoteLength;

public class GrooveTest extends TestBase {

	@Test
	public void test_A01() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(Fraction.zero);
		assertEquals(0, unit.pos());
		assertEquals(1, unit.accentDelta());
	}
	
	@Test
	public void test_A02() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(new Fraction(1, 32));
		assertEquals(12, unit.pos());
		assertEquals(0, unit.accentDelta());
	}
	
	@Test
	public void test_A03() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("8"));
		assertEquals(48, unit.pos());
		assertEquals(3, unit.accentDelta());
	}

	@Test
	public void test_A04() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("8."));
		assertEquals(72, unit.pos());
		assertEquals(4, unit.accentDelta());
	}
	
	@Test
	public void test_A05() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("4"));
		assertEquals(96, unit.pos());
		assertEquals(0, unit.accentDelta());
	}
	
	@Test
	public void test_A06() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("2"));
		assertEquals(192, unit.pos());
		assertEquals(0, unit.accentDelta());
	}

	Groove testGrooveA() throws SymfonionException {
		Groove groove = new Groove();
		groove.add(Utils.parseNoteLength("16"), 24, 1);
		groove.add(Utils.parseNoteLength("16"), 24, 2);
		groove.add(Utils.parseNoteLength("16"), 24, 3);
		groove.add(Utils.parseNoteLength("16"), 24, 4);
		return groove;
	}
	
	@Test
	public void test_B01() throws SymfonionException {
		System.out.println("hello");
		Groove groove = testGrooveB();
		Groove.Unit unit;
		
		unit = groove.resolve(Fraction.zero);
		assertEquals(0, unit.pos());
		assertEquals(1, unit.accentDelta());
	}
	
	@Test
	public void test_B02() throws SymfonionException {
		Groove groove = testGrooveB();
		Groove.Unit unit;
		
		unit = groove.resolve(new Fraction(1, 32));
		assertEquals(13, unit.pos());
		assertEquals(0, unit.accentDelta());
	}
	
	@Test
	public void test_B03a() throws SymfonionException {
		Groove groove = testGrooveB();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("16"));
		assertEquals(26, unit.pos());
		assertEquals(2, unit.accentDelta());
	}

	@Test
	public void test_B03b() throws SymfonionException {
		Groove groove = testGrooveB();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("8"));
		assertEquals(48, unit.pos());
		assertEquals(3, unit.accentDelta());
	}

	@Test
	public void test_B04() throws SymfonionException {
		Groove groove = testGrooveB();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("8."));
		assertEquals(73, unit.pos());
		assertEquals(4, unit.accentDelta());
	}
	
	@Test
	public void test_B05() throws SymfonionException {
		Groove groove = testGrooveB();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("4"));
		assertEquals(96, unit.pos());
		assertEquals(0, unit.accentDelta());
	}
	
	@Test
	public void test_B06() throws SymfonionException {
		Groove groove = testGrooveB();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("2"));
		assertEquals(192, unit.pos());
		assertEquals(0, unit.accentDelta());
	}

	Groove testGrooveB() throws SymfonionException {
		Groove groove = new Groove();
		groove.add(Utils.parseNoteLength("16"), 26, 1);
		groove.add(Utils.parseNoteLength("16"), 22, 2);
		groove.add(Utils.parseNoteLength("16"), 25, 3);
		groove.add(Utils.parseNoteLength("16"), 23, 4);
		return groove;
	}
}
