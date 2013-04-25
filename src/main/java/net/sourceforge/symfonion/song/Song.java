package net.sourceforge.symfonion.song;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.logias.Logias;
import net.sourceforge.logias.lisp.Context;
import net.sourceforge.symfonion.core.ExceptionThrower;
import net.sourceforge.symfonion.core.JsonUtil;
import net.sourceforge.symfonion.core.SymfonionException;
import net.sourceforge.symfonion.core.Util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class Song {
	private JsonObject json;
	private Map<String, Part> parts = new HashMap<String, Part>();
	private Map<String, Pattern> patterns = new HashMap<String, Pattern>();
	private Map<String, NoteMap> noteMaps = new HashMap<String, NoteMap>();
	private List<Bar> bars  = new LinkedList<Bar>();
	private Context logiasContext;
	
	public Song(Context logiasContext, JsonObject json) {
		this.json = json;
		this.logiasContext = logiasContext;
	}
	
	public void init() throws SymfonionException {
		initSettings();
		initParts();
		initNoteMaps();
		initPatterns();
		initSequence();
	}

	private void initSettings() throws SymfonionException {
		JsonElement tmp = JsonUtil.asJson(this.json, Keyword.$settings);
		if (tmp != null) {
			if (!tmp.isJsonObject()) {
				ExceptionThrower.throwSyntaxException("This element is assumed to be an array not an object.", null);
			}
			String profileName = JsonUtil.asString(tmp.getAsJsonObject(), Keyword.$mididevice);
			Context context = logiasContext;
			Logias logias = new Logias(logiasContext); 
			if (profileName != null) {
				JsonObject devicedef = JsonUtil.toJson(Util.loadResource(profileName + ".js")).getAsJsonObject();
				Iterator<String> i = JsonUtil.keyIterator(devicedef);
				while (i.hasNext()) {
					String k = i.next();
					JsonElement v = devicedef.get(k);
					context.bind(k, logias.run(logias.buildSexp(v)));
				}
			}
		}
	}

	private void initSequence() throws SymfonionException {
		/*
		"$sequence":{
			"$bars":[
			    {
					"$beats":"16/16",
					"$patterns":{ 
						"vocal":["melody1", "effect1"],
						"piano":["melody1"],
						"guitar":["melody1"],
						"base":["melody1"],
						"drums":["melody1"],
					},
					"$groove":null,
				},
			]
		},
		 */
		JsonElement tmp = JsonUtil.asJson(this.json, Keyword.$sequence);
		if (!tmp.isJsonArray()) {
			ExceptionThrower.throwSyntaxException("This element is assumed to be an array not an object.", null);
		}
		JsonArray seqJson = tmp.getAsJsonArray(); 
		int len = seqJson.getAsJsonArray().size();
		for (int i = 0; i < len; i++) {
			JsonElement barJson = seqJson.get(i);
			if (!barJson.isJsonObject()) {
				ExceptionThrower.throwSyntaxException("This element is assumed to be an object", null);
			}
			Bar bar = new Bar(barJson.getAsJsonObject(), this);
			bars.add(bar);
		}
	}

	private void initNoteMaps() throws SymfonionException {
		final JsonObject noteMapsJSON = JsonUtil.asJsonObject(this.json, Keyword.$notemaps);
		Iterator<String> i = JsonUtil.keyIterator(noteMapsJSON);
		noteMaps.put(Keyword.$normal.toString(), NoteMap.defaultNoteMap);
		noteMaps.put(Keyword.$percussion.toString(), NoteMap.defaultPercussionMap);
		while (i.hasNext()) {
			String name = i.next();
			NoteMap cur = new NoteMap(JsonUtil.asJsonObject(noteMapsJSON, name));
			noteMaps.put(name, cur);
		}
	}

	private void initPatterns() throws SymfonionException {
		JsonObject patternsJSON = JsonUtil.asJsonObject(this.json, Keyword.$patterns);
		Iterator<String> i = JsonUtil.keyIterator(patternsJSON);
		while (i.hasNext()) {
			String name = i.next();
			Pattern cur = Pattern.createPattern(name, JsonUtil.asJsonObject(patternsJSON, name), this);
			this.patterns.put(name, cur);
		}
	}

	private void initParts() throws SymfonionException {
		JsonObject instrumentsJSON = JsonUtil.asJsonObject(this.json, Keyword.$parts);
		Iterator<String> i = JsonUtil.keyIterator(instrumentsJSON);
		while (i.hasNext()) {
			String name = i.next();
			Part cur = new Part(name, JsonUtil.asJsonObject(instrumentsJSON, name), this);
			this.parts.put(name, cur);
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
}