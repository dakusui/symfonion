package com.github.dakusui.symfonion.misc;


import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.github.dakusui.symfonion.core.Fraction;
import com.github.dakusui.symfonion.core.JsonUtil;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;




public class Song {
	static Pattern r = Pattern.compile("([A-Z])([#b]*)([><]*)([\\+\\-]*)");
//	static Pattern r = Pattern.compile("([CDEFGAB])");
	public static void _main(String[] args) throws Exception {
		System.out.println("bbb###".indexOf('#',0));
		System.out.println("bbb###".indexOf('#',1));
		System.out.println("bbb###".indexOf('#',2));
		System.out.println("bbb###".indexOf('#',3));
		System.out.println("bbb###".indexOf('#',4));
		System.out.println("bbb###".indexOf('b',0));
		System.out.println("bbb###".indexOf('b',1));
		System.out.println("bbb###".indexOf('b',2));
		System.out.println("bbb###".indexOf('b',3));
		System.out.println("bbb###".indexOf('+',0));
		System.out.println("----");
		System.out.println(Stroke.count('#', "##bbbb"));
		System.out.println(Stroke.count('b', "##bbbb"));
		System.out.println(Stroke.count('+', "##bbbb"));
	}
	
	public static void __main(String[] args) throws Exception {
		String s = "A#>>B#C+D++EbG>++";
		//s="a";
		boolean parsed = false;
		Matcher m = r.matcher(s);
		int i;
		for (i = 0; m.find(i); i = m.end()) {
			int gc = m.groupCount();
			System.out.println("gc=<" + gc + ">, start=<" + m.start() + ">, end=<" + m.end() + ">");
			if (i != m.start()) {
				throw new SymfonionException("Error:" + s.substring(0, i) + "[" + s.substring(i, m.start()) + "]" + s.substring(m.start()));
			}
			for (int j = 0; j <= gc; j ++) {
				System.out.println("  [" + j + "]=[" + m.group(j) + "]");
			}
			parsed = true;
		}
		if (!parsed) { 
			throw new Exception("Error:[" + s + "]");
		}
		if (i != s.length()) {
			String msg = "Error:" + s.substring(0, i) + "[" + s.substring(i) + "]";
			throw new Exception(msg);
		}
	}
	public static void main(String[] args) throws Exception {
		JsonArray json = JsonUtil.toJson(
				"[\n" + 
		        "\"F#>C#>A#\",\n" +
		        "[\"F#>C#>A#\", \"8\"],\n" +
		        "{\"$notes\":\"F#>C#>A#\"},\n" +
		        "{\"$notes\":\"E>BG#\"},\n" +
		        "{\"$length\":\"8\"},\n" +
		        "{\"$notes\":\"E>BG#\"},\n" +
		        "{\"$notes\":\"E>BG#\"},\n" +
		        "{\"$length\":\"8\"},\n" +
		        "{\"$notes\":\"E>BG#\"},\n" +
		        "{\"$notes\":\"E>BG#\"},\n" +
		        "{\"$notes\":\"G#>D#>B\"},\n" +
		        "{\"$notes\":\"G#>+D#>B\"},\n" +
		        "{\"$notes\":\"G#>+D#>+B\"},\n" +
		        "{\"$notes\":\"G#>+D#>+B+\"},\n" +
		        "{\"$notes\":\"G#>++D#>+B+\"},\n" +
		        "{\"$notes\":\"G#>++D#>++B+\"},\n" +
		        "{\"$notes\":\"G#>++D#>++B++\"},\n" +
		        "]"
		).getAsJsonArray();
		List<Stroke> body = new LinkedList<Stroke>();
		for (int i = 0; i < json.size(); i++) {
			JsonElement cur = json.get(i);
			System.out.println(cur);
			String notes = null;
			String length = "4";
			double gate = 0.8;
			
			if (cur.isJsonPrimitive()) {
				notes = cur.getAsString();
			} else if (cur.isJsonArray()) {
				JsonArray arr = cur.getAsJsonArray(); 
				int elems = arr.size();
				if (elems > 0) {
					notes = arr.get(0).getAsString();
					if (elems > 1) {
						length = arr.get(1).getAsString();
					}
				}
			} else {
				JsonObject obj = cur.getAsJsonObject();
				notes = JsonUtil.asString(obj, "$notes");
				if (obj.has("$length")) {
					length = JsonUtil.asString(obj, "$length");
				}
				if (obj.has("$gate")) {
					gate = JsonUtil.asDouble(obj, "$gate");
				}
			}
			Stroke stroke = new Stroke(
				notes,
				length,
				gate
			);
			body.add(stroke);
		}
		Settings settings = new Settings();
		Sequence seq = sequence(body, settings);
		File outputFile = new File("tmp.mid");
		MidiSystem.write(seq, 1, outputFile);		
	}
	
	static Sequence sequence(List<Stroke> bar, Settings settings) throws InvalidMidiDataException {
		int resolution = 384;
		Sequence ret = new Sequence(Sequence.PPQ, 96);
		Track track = ret.createTrack();
		long position = 0;
		track.add(createProgramChangeEvent(1, 9, 0));
		for (Stroke stroke : bar) {
			System.out.println("***" + position + ":gate:" + stroke.gate());
			if (stroke.notes() != null) {
				for (Note note : stroke.notes()) {
					int key = note.key();
					int velocity = settings.velocitybase() + note.accent() * settings.velocitydelta();
					track.add(createNoteOnEvent(1, key, velocity, position));
					track.add(createNoteOffEvent(1, key, (int)(position + stroke.length().doubleValue() * stroke.gate() * resolution))); 
				}
			}
			position += stroke.length().doubleValue() * resolution;
		}
		return ret;
	}
	
	private static MidiEvent createNoteOnEvent(int ch, int nKey, int velocity, long lTick) {
		return createNoteEvent(ShortMessage.NOTE_ON,
							   ch,
							   nKey,
							   velocity,
							   lTick);
	}



	private static MidiEvent createNoteOffEvent(int ch, int nKey, long lTick) {
		return createNoteEvent(ShortMessage.NOTE_OFF,
							   ch,
							   nKey,
							   0,
							   lTick);
	}



	private static MidiEvent createNoteEvent(int nCommand,
											int ch,
											 int nKey,
											 int nVelocity,
											 long lTick) {
		ShortMessage	message = new ShortMessage();
		try
		{
			message.setMessage(nCommand,
							   ch,
							   nKey,
							   nVelocity);
		}
		catch (InvalidMidiDataException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		MidiEvent	event = new MidiEvent(message,
										  lTick);
		return event;
	}
	private static MidiEvent createProgramChangeEvent(int ch, int pgnum, long lTick) {
		ShortMessage	message = new ShortMessage();
		try	{
			message.setMessage(ShortMessage.PROGRAM_CHANGE,
			ch,
			pgnum,
			0);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			System.exit(1);
		}
		MidiEvent	event = new MidiEvent(message,
				  lTick);
		return event;
	}

}
enum ControllerEvent {
	$modulationwheel {
		@Override public int code() { return 1;}
	},
	$dataentrymsb {
		@Override public int code() { return 6;}
	},
	$volume {
		@Override public int code() { return 7;}
	},
	$pan {
		@Override public int code() { return 10;}
	},
	$expression {
		@Override public int code() { return 11;}
	},
	$dataentrylsb {
		@Override public int code() { return 38;}
	},
	$sustainpedal {
		@Override public int code() { return 64;}
	},
	$revervlevel {
		@Override public int code() { return 91;}
	},
	$tremololevel {
		@Override public int code() { return 92;}
	},
	$choruslevel {
		@Override public int code() { return 93;}
	},
	$celestelevel {
		@Override public int code() { return 94;}
	},
	$phaserlevel {
		@Override public int code() { return 95;}
	},
	$nonregisteredparameterlsb {
		@Override public int code() { return 98;}
	},
	$nonregisteredparametermsb {
		@Override public int code() { return 99;}
	},
	$registeredparameternumberlsb {
		@Override public int code() { return 100;}
	},
	$registeredparameternumbermsb {
		@Override public int code() { return 101;}
	},
	$allcontrollersoff {
		@Override public int code() { return 121;}
	},
	$alllnotesoff {
		@Override public int code() { return 123;}
	},
	;
	public abstract int code();
}
enum NoteName {
	C {
		@Override int number() {return 60;}
	},
	D  {
		@Override int number() {return 62;}
	},
	E {
		@Override int number() {return 64;}
	},
	F {
		@Override int number() {return 65;}
	},
	G {
		@Override int number() {return 67;}
	},
	A {
		@Override int number() {return 69;}
	},
	B {
		@Override int number() {return 71;}
	};	
	abstract int number();
}
class Note {
	int key;
	int accent;
	public Note(int key, int accent) {
		this.key = key;
		this.accent = accent;
	}
	public int key() {
		return this.key;
	}
	public int accent() {
		return this.accent;
	}
}
class Stroke {
	static Pattern notesPattern = Pattern.compile("([CDEFGAB])([#b]*)([><]*)([\\+\\-]*)");
	static Pattern lengthPattern = Pattern.compile("([1-9][0-9]*)(\\.*)");
	
	List<Note> notes = new LinkedList<Note>();
	private Fraction length;
	private double gate;
	public Stroke(
			String notes,
			String length,
			double gate
			) throws SymfonionException {
		this.length = parseLength(length);
		this.gate = gate;
		if (notes != null) {
			this.notes = parseNotes(notes);
		}
	}
	
	public double gate() {
		return this.gate;
	}

	public Fraction length() {
		return length;
	}

	public List<Note> notes() {
		return this.notes;
	}

	private Fraction parseLength(String length) {
		Matcher m = lengthPattern.matcher(length);
		Fraction ret = null;
		if (m.matches()) {
			int l = Integer.parseInt(m.group(1));
			ret = new Fraction(1, l);
			int dots = count('.', m.group(2));
			for (int i = 0; i < dots; i++) {
				l *= 2;
				ret = Fraction.add(ret, new Fraction(1, l));
			}
		}
		return ret;
	}

	private List<Note> parseNotes(String s) throws SymfonionException {
		List<Note> ret = new LinkedList<Note>();
		boolean parsed = false;
		Matcher m = notesPattern.matcher(s);
		int i;
		for (i = 0; m.find(i); i = m.end()) {
			int gc = m.groupCount();
			System.out.println("gc=<" + gc + ">, start=<" + m.start() + ">, end=<" + m.end() + ">");
			if (i != m.start()) {
				throw new SymfonionException("Error:" + s.substring(0, i) + "[" + s.substring(i, m.start()) + "]" + s.substring(m.start()));
			}
			for (int j = 0; j <= gc; j ++) {
				System.out.println("  [" + j + "]=[" + m.group(j) + "]");
			}
			int n = 
				NoteName.valueOf(m.group(1)).number() + 
				count('#', m.group(2)) - count('b', m.group(2)) +
				count('>', m.group(3)) * 12 - count('<', m.group(3)) *12;
			int a = count('+', m.group(4)) - count('-', m.group(4));
			Note nn = new Note(n, a);
			ret.add(nn);
			parsed = true;
		}
		if (!parsed) { 
			throw new SymfonionException("Error:[" + s + "]");
		}
		if (i != s.length()) {
			String msg = "Error:" + s.substring(0, i) + "[" + s.substring(i) + "]";
			throw new SymfonionException(msg);
		}
		return ret;
	}
	static int count(char ch, String s) {
		int ret = 0;
		for (int i = s.indexOf(ch); i >= 0; i = s.indexOf(ch, i + 1)) {
			ret++;
		}
		return ret;
	}
}
class Settings {
	int velocitybase  = 64;
	int velocitydelta = 10;
	int transpose = 0;
	int volume = 64;
	public int velocitybase() {
		return this.velocitybase;
	}
	public int velocitydelta() {
		return this.velocitydelta;
	}
	public int transpose() {
		return this.transpose;
	}
	public int volume() {
		return this.volume;
	}
}