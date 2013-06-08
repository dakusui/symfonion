package com.github.dakusui.symfonion.song;

import static com.github.dakusui.symfonion.core.SymfonionIllegalFormatException.FRACTION_EXAMPLE;
import static com.github.dakusui.symfonion.core.SymfonionTypeMismatchException.ARRAY;
import static com.github.dakusui.symfonion.core.SymfonionTypeMismatchException.PRIMITIVE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.dakusui.symfonion.core.ExceptionThrower;
import com.github.dakusui.symfonion.core.Fraction;
import com.github.dakusui.symfonion.core.JsonUtil;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.core.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Bar {
	Fraction beats;
	Map<String, List<Pattern>> patternLists = new HashMap<String, List<Pattern>>();
	Map<String, JsonElement> locations = new HashMap<String, JsonElement>();
	Groove groove;
	private Song song;
	
	public Bar(JsonObject jsonObject, Song song) throws SymfonionException {
		this.song = song;
		init(jsonObject);
	}

	public void init(JsonObject jsonObject) throws SymfonionException {
		JsonElement fractionJson = JsonUtil.asJson(jsonObject, Keyword.$beats);
		if (!fractionJson.isJsonPrimitive()) {
			ExceptionThrower.throwTypeMismatchException(fractionJson, PRIMITIVE);
		}
		this.beats = Util.parseFraction(fractionJson.getAsString());
		if (this.beats == null) {
			ExceptionThrower.throwIllegalFormatException(fractionJson, FRACTION_EXAMPLE);
		}
		this.groove = Groove.DEFAULT_INSTANCE;
		if (jsonObject.has(Keyword.$groove.name())) {
			JsonElement grooveJson = JsonUtil.asJson(jsonObject, Keyword.$groove.name());
			String grooveName = grooveJson.getAsString();
			Groove g = Groove.DEFAULT_INSTANCE;
			if (grooveName != null) {
				g = song.groove(grooveName);
				if (g == null) {
					ExceptionThrower.throwGrooveNotDefinedException(grooveJson, grooveName);
				}
			}
			this.groove = g;
		}
		JsonObject patternsJsonObject = JsonUtil.asJsonObject(jsonObject, Keyword.$patterns);
		Iterator<Entry<String, JsonElement>> i = patternsJsonObject.entrySet().iterator();
		while (i.hasNext()) {
			String partName = i.next().getKey();
			List<Pattern> patterns = new LinkedList<Pattern>();
			JsonArray partPatternsJsonArray = JsonUtil.asJsonArray(patternsJsonObject, partName);
			if (!partPatternsJsonArray.isJsonArray()) {
				ExceptionThrower.throwTypeMismatchException(partPatternsJsonArray, ARRAY);
			}
			int len = partPatternsJsonArray.size();
			for (int j = 0; j < len; j++) {
				JsonElement jsonPattern = partPatternsJsonArray.get(j);
				String patternName = jsonPattern.getAsString();
				locations.put(partName, jsonPattern);
				Pattern cur = song.pattern(patternName);
				if (cur == null) {
					ExceptionThrower.throwPatternNotFound(jsonPattern, patternName);
				}
				patterns.add(cur);
			}
			patternLists.put(partName, patterns);
		}
	}
	
	public Set<String> partNames() {
		return Collections.unmodifiableSet(this.patternLists.keySet());
	}
	
	public List<Pattern> part(String instrumentName) {
		return Collections.unmodifiableList(this.patternLists.get(instrumentName));
	}

	public Fraction beats() {
		return this.beats;
	}
	
	public Groove groove() {
		return this.groove;
	}

	public JsonElement location(String partName) {
		return null;
	}
}
