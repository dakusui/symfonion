package com.github.dakusui.symfonion.song;

import static com.github.dakusui.symfonion.core.SymfonionTypeMismatchException.ARRAY;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.dakusui.json.JsonInvalidPathException;
import com.github.dakusui.json.JsonPathNotFoundException;
import com.github.dakusui.json.JsonTypeMismatchException;
import com.github.dakusui.json.JsonUtil;
import com.github.dakusui.symfonion.core.ExceptionThrower;
import com.github.dakusui.symfonion.core.Fraction;
import com.github.dakusui.symfonion.core.FractionFormatException;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.core.SymfonionIllegalFormatException;
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
	private JsonObject json = null;
	

	public Bar(JsonObject jsonObject, Song song) throws SymfonionException, JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
		this.song = song;
		this.json = jsonObject;
		init(jsonObject);
	}

	public void init(JsonObject jsonObject) throws SymfonionException, JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
		try {
			this.beats = Util.parseFraction(JsonUtil.asString(jsonObject, Keyword.$beats));
		} catch (FractionFormatException e) {
			ExceptionThrower.throwIllegalFormatException(
					JsonUtil.asJsonElement(jsonObject, Keyword.$beats),
					SymfonionIllegalFormatException.FRACTION_EXAMPLE);
		}
		this.beats = this.beats == null ? Fraction.one : this.beats;
		this.groove = Groove.DEFAULT_INSTANCE;
		Groove g = Groove.DEFAULT_INSTANCE;
		if (JsonUtil.hasPath(jsonObject, Keyword.$groove)) {
			String grooveName = JsonUtil.asString(jsonObject, Keyword.$groove.name());
			g = song.groove(grooveName);
			if (g == null) {
				ExceptionThrower.throwGrooveNotDefinedException(
						JsonUtil.asJsonElement(jsonObject, Keyword.$groove),
						grooveName
						);			
			}
		}
		this.groove = g;
		JsonObject patternsJsonObject = JsonUtil.asJsonObject(jsonObject, Keyword.$patterns);
		if (patternsJsonObject == null) {
			ExceptionThrower.throwRequiredElementMissingException(jsonObject, Keyword.$patterns);
		}
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
		try {
			return JsonUtil.asJsonElement(this.json, Keyword.$patterns, partName);
		} catch (JsonPathNotFoundException e) {
			return null;
		} catch (JsonInvalidPathException e) {
			return null;
		}
	}
}
