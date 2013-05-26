package com.github.dakusui.symfonion.song;

import java.util.LinkedList;
import java.util.List;

import com.github.dakusui.symfonion.core.ExceptionThrower;
import com.github.dakusui.symfonion.core.Fraction;
import com.github.dakusui.symfonion.core.JsonUtil;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.core.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
		Unit ret = new Unit();
		long pos = 0;
		int accentDelta = 0;
		if (Fraction.compare(Fraction.zero, offset) < 0) {
			
		}
		if (Fraction.compare(Fraction.zero, offset) == 0) {
			if (beats.size() > 0) {
				ret.accentDelta = this.beats.get(0).accent;
			} else {
				ret.accentDelta = 0;
			}
			return ret;
		}
		Fraction rest = (Fraction) offset.clone();
		for (Beat u : this.beats) {
			System.out.print(rest);
			if (Fraction.compare(rest, Fraction.zero) > 0) {
				System.out.print(".");
				pos += u.ticks;
			} else {
				System.out.println("*");
				Fraction modulo = Fraction.subtract(Fraction.zero, rest);
				System.out.print("modulo:" + modulo + ",coeff:" + Fraction.div(modulo, u.length).doubleValue() + ",u.ticks:" + u.ticks + "|");
				pos += Fraction.div(modulo, u.length).doubleValue() * u.ticks;
				System.out.println("pos:" + pos);
				if (Fraction.compare(Fraction.zero, modulo) == 0) {
					accentDelta = u.accent;
				}
				break;
			}
			rest = Fraction.subtract(rest, u.length);
		}
		if (Fraction.compare(rest, Fraction.zero) > 0) {
			System.out.println(";");
			pos += this.resolution * rest.doubleValue();
		}

		ret.pos = pos;
		ret.accentDelta = accentDelta;
		return ret;
	}
	
	public void add(Fraction length, long ticks, int accent) {
		if (this == DEFAULT_INSTANCE) {
			ExceptionThrower.throwRuntimeException("Groove.DEFAULT_INSTANCE is immutable.", null);
		}
		beats.add(new Beat(length, ticks, accent));
	}

	public static Groove createGroove(String grooveName, JsonArray grooveDef,
			Song song) throws SymfonionException {
		Groove ret = new Groove();
		for (JsonElement elem : grooveDef) {
			if (!elem.isJsonObject()) {
				String msg = "";
				ExceptionThrower.throwSyntaxException(msg, null);
			}
			JsonObject cur = elem.getAsJsonObject();
			String len = JsonUtil.asString(cur, Keyword.$length);
			long   ticks = JsonUtil.asLong(cur, Keyword.$ticks);
			int    accent = JsonUtil.asInt(cur, Keyword.$accent);
			
			ret.add(Util.parseNoteLength(len), ticks, accent);
		}
		return ret;
	}
}

