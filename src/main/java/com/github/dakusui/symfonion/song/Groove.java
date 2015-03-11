package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.*;
import com.github.dakusui.symfonion.core.ExceptionThrower;
import com.github.dakusui.symfonion.core.Fraction;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.core.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.symfonion.core.SymfonionIllegalFormatException.NOTELENGTH_EXAMPLE;
import static com.github.dakusui.symfonion.core.SymfonionTypeMismatchException.OBJECT;

public class Groove {
	public static final Groove DEFAULT_INSTANCE = new Groove();
	
	static class Beat {
		long ticks;
		int  accent;
		Fraction length;

		public Beat(Fraction length, long ticks, int accent) {
			this.length = length;
			this.ticks = ticks;
			this.accent = accent;
		}
	}
	
	public static class Unit {
		long pos;
		int accentDelta;
		
		public long pos() {
			return this.pos;
		}
		
		public int accent() {
			return this.accentDelta;
		}
	}
	
	List<Beat> beats = new LinkedList<Beat>();
	
	private int resolution;
	
	public Groove() {
		this(384);
	}
	
	public Groove(int resolution) {
		this.resolution = resolution;
	}
	
	public Unit resolve(Fraction offset) {
		if (offset == null) {
			String msg = "offset cannot be null. (Groove#resolve)";
			ExceptionThrower.throwRuntimeException(msg, null);
		}
		if (Fraction.compare(offset, Fraction.zero) < 0) {
			String msg = "offset cannot be negative. (Groove#resolve)";
			ExceptionThrower.throwRuntimeException(msg, null);
		}
		Unit ret = new Unit();
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
		if (Fraction.compare(rest, Fraction.zero) < 0) {
			Beat beat = this.beats.get(i - 1);
			ret.pos = (long) (pos + Fraction.div(rest, beat.length).doubleValue() * beat.ticks); 
		} else if (Fraction.compare(rest, Fraction.zero) == 0) {
			if (i < this.beats.size()) {
				ret.accentDelta = this.beats.get(i).accent;
			}
			ret.pos = pos;
		} else {
			ret.pos = (pos + (long)(rest.doubleValue() * this.resolution));
		}
		return ret;
	}
	
	public void add(Fraction length, long ticks, int accent) {
		if (this == DEFAULT_INSTANCE) {
			ExceptionThrower.throwRuntimeException("Groove.DEFAULT_INSTANCE is immutable.", null);
		}
		beats.add(new Beat(length, ticks, accent));
	}

	public static Groove createGroove(JsonArray grooveDef) throws SymfonionException, JsonTypeMismatchException, JsonInvalidPathException, JsonFormatException {
		Groove ret = new Groove();
		for (JsonElement elem : grooveDef) {
			if (!elem.isJsonObject()) {
				ExceptionThrower.throwTypeMismatchException(elem, OBJECT);
			}
			JsonObject cur = elem.getAsJsonObject();
			String len = JsonUtils.asString(cur, Keyword.$length);
			long   ticks = JsonUtils.asLong(cur, Keyword.$ticks);
			int    accent = JsonUtils.asInt(cur, Keyword.$accent);
			
			Fraction f = Util.parseNoteLength(len);
			if (f == null) {
				ExceptionThrower.throwIllegalFormatException(
						JsonUtils.asJsonElement(cur, Keyword.$length),
						NOTELENGTH_EXAMPLE);
			}
			ret.add(f, ticks, accent);
		}
		return ret;
	}
}

