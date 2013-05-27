package com.github.dakusui.symfonion.song;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.dakusui.symfonion.core.Fraction;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.core.Util;

import static com.github.dakusui.symfonion.core.Util.parseNoteLength;

public class GrooveTest {
	@Test
	public void test_A01() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(Fraction.zero);
		assertEquals(0, unit.pos);
		assertEquals(1, unit.accentDelta);
	}
	
	public void test_A02() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(new Fraction(1, 32));
		assertEquals(12, unit.pos);
		assertEquals(0, unit.accentDelta);
	}
	
	public void test_A03() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("8"));
		assertEquals(48, unit.pos);
		assertEquals(3, unit.accentDelta);
	}
	public void test_A04() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("8."));
		assertEquals(72, unit.pos);
		assertEquals(1, unit.accentDelta);
	}
	
	public void test_A05() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("4"));
		assertEquals(0, unit.pos);
		assertEquals(1, unit.accentDelta);
	}
	
	public void test_A06() throws SymfonionException {
		Groove groove = testGrooveA();
		Groove.Unit unit;
		
		unit = groove.resolve(parseNoteLength("2"));
		assertEquals(192, unit.pos);
		assertEquals(0, unit.accentDelta);
	}

	Groove testGrooveA() throws SymfonionException {
		Groove groove = new Groove();
		groove.add(Util.parseNoteLength("16"), 24, 1);
		groove.add(Util.parseNoteLength("16"), 24, 2);
		groove.add(Util.parseNoteLength("16"), 24, 3);
		groove.add(Util.parseNoteLength("16"), 24, 4);
		return groove;
	}
	
	Groove testGrooveB() throws SymfonionException {
		Groove groove = new Groove();
		groove.add(Util.parseNoteLength("16"), 26, 1);
		groove.add(Util.parseNoteLength("16"), 22, 2);
		groove.add(Util.parseNoteLength("16"), 25, 3);
		groove.add(Util.parseNoteLength("16"), 24, 4);
		return groove;
	}
}
