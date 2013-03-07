package net.sourceforge.symfonion.song;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.symfonion.core.JsonUtil;
import net.sourceforge.symfonion.core.SymfonionException;

import com.google.gson.JsonObject;


public class Part {
	int channel;
	private static final Map<String, Integer> defaultNoteMap = new HashMap<String, Integer>();
	private Map<String, Integer> notemap = defaultNoteMap;
	private String name;
	static {
		defaultNoteMap.put("C", 60);
		defaultNoteMap.put("D", 62);
		defaultNoteMap.put("E", 64);
		defaultNoteMap.put("F", 65);
		defaultNoteMap.put("G", 67);
		defaultNoteMap.put("A", 69);
		defaultNoteMap.put("B", 71);
	}
	public Part(String name, JsonObject json, Song song) throws SymfonionException {
		this.name = name;
		this.channel = JsonUtil.asInt(json, Keyword.$channel);
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
}
