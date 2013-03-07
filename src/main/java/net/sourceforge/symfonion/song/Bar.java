package net.sourceforge.symfonion.song;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.sourceforge.symfonion.core.ExceptionThrower;
import net.sourceforge.symfonion.core.Fraction;
import net.sourceforge.symfonion.core.JsonUtil;
import net.sourceforge.symfonion.core.SymfonionException;
import net.sourceforge.symfonion.core.Util;


public class Bar {
	Fraction beats;
	Map<String, List<Pattern>> patternLists = new HashMap<String, List<Pattern>>();
	Groove groove;
	private Song song;
	
	public Bar(JsonObject jsonObject, Song song) throws SymfonionException {
		this.song = song;
		init(jsonObject);
	}

	public void init(JsonObject jsonObject) throws SymfonionException {
		this.beats = Util.parseFraction(JsonUtil.asString(jsonObject, Keyword.$beats));
		JsonObject patternsJsonObject = JsonUtil.asJsonObject(jsonObject, Keyword.$patterns);
		Iterator<Entry<String, JsonElement>> i = patternsJsonObject.entrySet().iterator();
		while (i.hasNext()) {
			String partName = i.next().getKey();
			List<Pattern> patterns = new LinkedList<Pattern>();
			JsonArray partPatternsJsonArray = JsonUtil.asJsonArray(patternsJsonObject, partName);
			if (! partPatternsJsonArray.isJsonArray()) {
				ExceptionThrower.throwSyntaxException("Array is expected here.", null);
			}
			int len = partPatternsJsonArray.size();
			for (int j = 0; j < len; j++) {
				String patternName = partPatternsJsonArray.get(j).getAsString();
				Pattern cur = song.pattern(patternName);
				if (cur == null) {
					ExceptionThrower.throwSyntaxException("Pattern:<" + patternName + "> is not defined.", null);
				}
				patterns.add(cur);
			}
			patternLists.put(partName, patterns);
		}
		this.groove = null;
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
}
