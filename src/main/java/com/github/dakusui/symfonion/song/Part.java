package com.github.dakusui.symfonion.song;

import java.util.HashMap;
import java.util.Map;

import com.github.dakusui.json.JsonFormatException;
import com.github.dakusui.json.JsonInvalidPathException;
import com.github.dakusui.json.JsonPathNotFoundException;
import com.github.dakusui.json.JsonTypeMismatchException;
import com.github.dakusui.json.JsonUtil;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.google.gson.JsonObject;


public class Part {
	int channel;
	private static final Map<String, Integer> defaultNoteMap = new HashMap<String, Integer>();
	private Map<String, Integer> notemap = defaultNoteMap;
	private String name;
	private String portName;
	
	static {
		defaultNoteMap.put("C", 60);
		defaultNoteMap.put("D", 62);
		defaultNoteMap.put("E", 64);
		defaultNoteMap.put("F", 65);
		defaultNoteMap.put("G", 67);
		defaultNoteMap.put("A", 69);
		defaultNoteMap.put("B", 71);
	}
	
	public Part(String name, JsonObject json, Song song) throws SymfonionException, JsonPathNotFoundException, JsonTypeMismatchException, JsonFormatException, JsonInvalidPathException {
		this.name = name;
		this.channel = JsonUtil.asInt(json, Keyword.$channel);
		this.portName = JsonUtil.asStringWithDefault(json, null, Keyword.$port);
	}

	public String name() {
		return this.name;
	}
	
	public int channel() {
		return this.channel;
	}
	
	public int note(String noteName) {
		return this.notemap.get(noteName);
	}

	public String portName() {
		return this.portName;
	}
}
