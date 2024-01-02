package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.noteNotDefinedException;


public class NoteMap {
	public static final NoteMap defaultNoteMap = new NoteMap(Keyword.$normal.name()); 
	public static final NoteMap defaultPercussionMap = new NoteMap(Keyword.$percussion.name()); 
	protected Map<String, Integer> map = new HashMap<>();
	private String name;
	static {
		defaultNoteMap.map.put("C", 60);
		defaultNoteMap.map.put("D", 62);
		defaultNoteMap.map.put("E", 64);
		defaultNoteMap.map.put("F", 65);
		defaultNoteMap.map.put("G", 67);
		defaultNoteMap.map.put("A", 69);
		defaultNoteMap.map.put("B", 71);
		defaultPercussionMap.map.put("B", 36);
		defaultPercussionMap.map.put("S", 38);
		defaultPercussionMap.map.put("C", 49);
		defaultPercussionMap.map.put("O", 46);
		defaultPercussionMap.map.put("H", 42);
		defaultPercussionMap.map.put("T", 47);
	}
	public NoteMap(String name) {
		this.name = name;
	}
	public NoteMap(final JsonObject json) {
		Iterator<String> i = JsonUtils.keyIterator(json);
		while (i.hasNext()) {
			String cur = i.next();
			int v = json.get(cur).getAsInt();
			this.map.put(cur, v);
		}
	}
	
	public int note(String notename, JsonElement problemCausingJsonNode) throws SymfonionException {
		if ("r".equals(notename)) {
			return -1;
		}
		if (!this.map.containsKey(notename)) {
			throw noteNotDefinedException(problemCausingJsonNode, notename, this.name);
		}
		return this.map.get(notename);
	}
	
	public String name() {
		return this.name;
	}
}
