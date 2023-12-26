package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.logias.Logias;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.core.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

import static com.github.dakusui.symfonion.core.exceptions.ExceptionThrower.requiredElementMissingException;
import static com.github.dakusui.symfonion.core.exceptions.ExceptionThrower.typeMismatchException;
import static com.github.dakusui.symfonion.core.exceptions.SymfonionTypeMismatchException.ARRAY;
import static com.github.dakusui.symfonion.core.exceptions.SymfonionTypeMismatchException.OBJECT;

public class Song {
	private JsonObject json;
	private Map<String, Part> parts = new HashMap<String, Part>();
	private Map<String, Pattern> patterns = new HashMap<String, Pattern>();
	private Map<String, NoteMap> noteMaps = new HashMap<String, NoteMap>();
	private Map<String, Groove> grooves = new HashMap<String, Groove>();
	private List<Bar> bars  = new LinkedList<Bar>();
	private Context logiasContext;
	
	public Song(Context logiasContext, JsonObject json) {
		this.json = json;
		this.logiasContext = logiasContext;
	}
	
	public void init() throws SymfonionException, JsonException {
		initSettings();
		initParts();
		initNoteMaps();
		initPatterns();
		initGrooves();
		initSequence();
	}

	private void initSettings() throws SymfonionException, JsonException {
		JsonElement tmp = JsonUtils.asJsonObjectWithDefault(this.json, new JsonObject(), Keyword.$settings);
		if (tmp != null) {
			if (!tmp.isJsonObject()) {
				throw typeMismatchException(tmp, OBJECT);
			}
			String profileName = JsonUtils.asStringWithDefault(tmp.getAsJsonObject(), "", Keyword.$mididevice);
			Context context = logiasContext;
			Logias logias = new Logias(logiasContext); 
			if (!"".equals(profileName)) {
				JsonObject devicedef = JsonUtils.toJson(Utils.loadResource(profileName + ".js")).getAsJsonObject();
				Iterator<String> i = JsonUtils.keyIterator(devicedef);
				while (i.hasNext()) {
					String k = i.next();
					JsonElement v = devicedef.get(k);
					context.bind(k, logias.run(logias.buildSexp(v)));
				}
			}
		}
	}

	private void initSequence() throws SymfonionException, JsonException {
		JsonElement tmp = JsonUtils.asJsonElement(this.json, Keyword.$sequence);
		if (tmp == null) {
			throw requiredElementMissingException(this.json, Keyword.$sequence);
		}
		if (!tmp.isJsonArray()) {
			throw typeMismatchException(tmp, ARRAY);
		}
		JsonArray seqJson = tmp.getAsJsonArray(); 
		int len = seqJson.getAsJsonArray().size();
		for (int i = 0; i < len; i++) {
			JsonElement barJson = seqJson.get(i);
			if (!barJson.isJsonObject()) {
				throw typeMismatchException(seqJson, OBJECT);
			}
			Bar bar = new Bar(barJson.getAsJsonObject(), this);
			bars.add(bar);
		}
	}

	private void initNoteMaps() throws SymfonionException, JsonException {
		final JsonObject noteMapsJSON = JsonUtils.asJsonObjectWithDefault(this.json, new JsonObject(), Keyword.$notemaps);
		
		Iterator<String> i = JsonUtils.keyIterator(noteMapsJSON);
		noteMaps.put(Keyword.$normal.toString(), NoteMap.defaultNoteMap);
		noteMaps.put(Keyword.$percussion.toString(), NoteMap.defaultPercussionMap);
		while (i.hasNext()) {
			String name = i.next();
			NoteMap cur = new NoteMap(JsonUtils.asJsonObject(noteMapsJSON, name));
			noteMaps.put(name, cur);
		}
	}

	private void initPatterns() throws SymfonionException, JsonException {
		JsonObject patternsJSON = JsonUtils.asJsonObjectWithDefault(this.json, new JsonObject(), Keyword.$patterns);
		
		Iterator<String> i = JsonUtils.keyIterator(patternsJSON);
		while (i.hasNext()) {
			String name = i.next();
			Pattern cur = Pattern.createPattern(JsonUtils.asJsonObject(patternsJSON, name), this);
			this.patterns.put(name, cur);
		}
	}

	private void initParts() throws SymfonionException, JsonException {
		if (JsonUtils.hasPath(this.json, Keyword.$parts)) {
			JsonObject instrumentsJSON = JsonUtils.asJsonObject(this.json, Keyword.$parts);
			Iterator<String> i = JsonUtils.keyIterator(instrumentsJSON);
			while (i.hasNext()) {
				String name = i.next();
				Part cur = new Part(name, JsonUtils.asJsonObject(instrumentsJSON, name));
				this.parts.put(name, cur);
			}
		}
	}

	private void initGrooves() throws SymfonionException, JsonException {
		if (JsonUtils.hasPath(this.json, Keyword.$grooves)) {
			JsonObject groovesJSON = JsonUtils.asJsonObject(this.json, Keyword.$grooves);
			
			Iterator <String> i = JsonUtils.keyIterator(groovesJSON);
			while (i.hasNext()) {
				String name = i.next();
				Groove cur = Groove.createGroove(JsonUtils.asJsonArray(groovesJSON, name));
				this.grooves.put(name, cur);
			}
		}
	}
	
	public Pattern pattern(String patternName) {
		return this.patterns.get(patternName);
	}
	
	public NoteMap noteMap(String noteMapName) {
		return this.noteMaps.get(noteMapName);
	}
	
	public List<Bar> bars() {
		return Collections.unmodifiableList(this.bars);
	}

	public Set<String> partNames() {
		return Collections.unmodifiableSet(this.parts.keySet());
	}
	
	public Part part(String name) {
		return this.parts.get(name);
	}

	public Context getLogiasContext() {
		return this.logiasContext;
	}

	public Groove groove(String grooveName) {
		return this.grooves.get(grooveName);
	}
}
