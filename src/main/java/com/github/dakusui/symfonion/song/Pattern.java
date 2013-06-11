package com.github.dakusui.symfonion.song;

import static com.github.dakusui.symfonion.core.SymfonionIllegalFormatException.NOTELENGTH_EXAMPLE;

import java.util.Collections;
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


public class Pattern {
	public static class Parameters {
		static final Fraction QUARTER = new Fraction(1, 4);;
		double gate = 0.8;
		Fraction length = QUARTER;
		int transpose = 0;
		int velocitybase  = 64;
		int velocitydelta = 10;
		int arpegio;
		public Parameters(JsonObject json) throws SymfonionException {
			init(json);
		}
		public double gate() {
			return this.gate;
		}
		public void init(JsonObject json) throws SymfonionException {
			if (json == null) {
				json = JsonUtil.toJson("{}").getAsJsonObject();
			}
			this.velocitybase = JsonUtil.asIntWithDefault(json, 64, Keyword.$velocitybase);
			this.velocitydelta = JsonUtil.asIntWithDefault(json, 5, Keyword.$velocitydelta);
			this.gate = JsonUtil.asDoubleWithDefault(json, 0.8, Keyword.$gate);
			this.length = Util.parseNoteLength(JsonUtil.asStringWithDefault(json, "4", Keyword.$length));
			if (this.length == null) {
				ExceptionThrower.throwIllegalFormatException(
						JsonUtil.asJson(json, Keyword.$length), 
						NOTELENGTH_EXAMPLE
				);
			}
			this.transpose = JsonUtil.asIntWithDefault(json, 0, Keyword.$transpose);
			this.arpegio = JsonUtil.asIntWithDefault(json, 0, Keyword.$arpegio);
		}
		public Fraction length() {
			return this.length;
		}
		public int transpose() {
			return this.transpose;
		}
		public int velocitybase() {
			return this.velocitybase;
		}
		public int velocitydelta() {
			return this.velocitydelta;
		}
		public int arpegio() {
			return this.arpegio;
		}
	}

	public static Pattern createPattern(String name, JsonObject json, Song song) throws SymfonionException {
		String noteMapName = JsonUtil.asString(json, Keyword.$notemap);
		NoteMap noteMap = NoteMap.defaultNoteMap;
		if (noteMapName != null) {
			noteMap = song.noteMap(noteMapName);
			if (noteMap == null) {
				ExceptionThrower.throwNoteMapNotFoundException(
						JsonUtil.asJson(json, Keyword.$notemap), 
						noteMapName
						);
			}
		}
		Pattern ret = new Pattern(noteMap);
		ret.init(json);
		return ret;
	}
	
	List<Stroke> body = null;
	NoteMap noteMap = null;
	Parameters params = null;
	Pattern(NoteMap noteMap) {
		this.noteMap = noteMap;
	}

	protected void init(JsonObject json) throws SymfonionException {
		// Initialize 'body'.
		this.body = new LinkedList<Stroke>();
		this.params = new Parameters(json);
		JsonArray bodyJSON = JsonUtil.asJsonArray(json, Keyword.$body);
		int len = bodyJSON.size();
		for (int i = 0; i < len; i++) {
			JsonElement cur = bodyJSON.get(i);
			Stroke stroke = new Stroke(
					cur,
					params,
					this.noteMap
			);
			body.add(stroke);
		}
	}

	public List<Stroke> strokes() {
		return Collections.unmodifiableList(this.body);
	}

	public Parameters parameters() {
		return this.params;
	}
}
