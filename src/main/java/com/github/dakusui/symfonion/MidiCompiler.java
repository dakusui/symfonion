package com.github.dakusui.symfonion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import com.github.dakusui.logias.Logias;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.logias.lisp.s.Literal;
import com.github.dakusui.logias.lisp.s.Sexp;
import com.github.dakusui.symfonion.core.ExceptionThrower;
import com.github.dakusui.symfonion.core.Fraction;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.core.Util;
import com.github.dakusui.symfonion.song.Bar;
import com.github.dakusui.symfonion.song.Groove;
import com.github.dakusui.symfonion.song.Part;
import com.github.dakusui.symfonion.song.Pattern;
import com.github.dakusui.symfonion.song.Pattern.Parameters;
import com.github.dakusui.symfonion.song.Song;
import com.github.dakusui.symfonion.song.Stroke;
import com.google.gson.JsonArray;

public class MidiCompiler {
	public static class CompilerContext {
		private Track track;
		private int channel;
		private Parameters params;
		private long position;
		private int grooveAccent;
		private long strokeLengthInTicks;

		public CompilerContext(Track track, int channel, Parameters params,
				long position, long strokeLengthInTicks, int grooveAccent) {
			this.track = track;
			this.channel = channel;
			this.params = params;
			this.position = position;
			this.grooveAccent = grooveAccent;
			this.strokeLengthInTicks = strokeLengthInTicks;
		}

		public Track getTrack() {
			return track;
		}

		public int getChannel() {
			return channel;
		}

		public Parameters getParams() {
			return params;
		}

		public long getPosition() {
			return position;
		}

		public int getGrooveAccent() {
			return this.grooveAccent;
		}
		
		public long getStrokeLengthInTicks() {
			return strokeLengthInTicks;
		}
		
		
	}
	private Context logiasContext;

	public MidiCompiler(Context logiasContext) {
		this.logiasContext = logiasContext;
	}
	
	public Map<String, Sequence> compile(Song song) throws InvalidMidiDataException, SymfonionException {
		System.out.println("Now compiling...");
		int resolution = 384;
		Map<String, Sequence> ret = new HashMap<String, Sequence>();
		Map<String, Track> tracks = new HashMap<String, Track>(); 
		for (String partName : song.partNames()) {
			Part part = song.part(partName);
			String portName = part.portName();
			Sequence sequence = ret.get(portName); 
			if (sequence == null) {
				sequence = new Sequence(Sequence.PPQ, resolution/4);
				ret.put(portName, sequence);
			}
			tracks.put(partName, sequence.createTrack()); 
		}
		
		////
		// position is the offset of a bar from the beginning of the sequence.
		// Giving the sequencer a grace period to initialize its internal state.
		long positionInTicks = resolution / 4; 
		int barid = 0;
		for (Bar bar : song.bars()) {
			barStarted(barid);
			Groove groove = bar.groove();
			for (String partName : bar.partNames()) {
				partStarted(partName);
				Track track = tracks.get(partName);
				if (track == null) {
					ExceptionThrower.throwPartNotFound(bar.location(partName), partName);
				}
				int channel = song.part(partName).channel(); 
				for (Pattern pattern : bar.part(partName)) {
					patternStarted();
					////
					// relativePosition is a relative position from the beginning 
					// of the bar the pattern belongs to.
					int  grooveAccent = 0;
					Fraction relPos = Fraction.zero;
					Parameters params = pattern.parameters();
					for (Stroke stroke : pattern.strokes()) {
						try {
							Groove.Unit grooveUnit = groove.resolve(relPos);  
							long relativePositionInTicks = grooveUnit. pos();
							grooveAccent = grooveUnit.accent();
							
							Fraction endingPos = Fraction.add(relPos, stroke.length());
							long strokeLengthInTicks = Math.max(0, groove.resolve(endingPos).pos() - relativePositionInTicks);

							stroke.compile(this, new CompilerContext(track, channel, params, positionInTicks + relativePositionInTicks, strokeLengthInTicks, grooveAccent));

							relPos = endingPos; 
							////
							// Breaks if relative position goes over the length of the bar.
							if (Fraction.compare(relPos, bar.beats()) >= 0) {
								break;
							}
						} finally {
							strokeEnded();
						}
					}
					patternEnded();
				}
				partEnded();
			}
			barEnded();
			barid++;
			positionInTicks += bar.beats().doubleValue() * resolution;
		}
		System.out.println("Compilation finished.");
		return ret;
	}
	
	public MidiEvent createNoteOnEvent(int ch, int nKey, int velocity, long lTick) throws InvalidMidiDataException {
		return createNoteEvent(ShortMessage.NOTE_ON,
							   ch,
							   nKey,
							   velocity,
							   lTick);
	}

	public MidiEvent createNoteOffEvent(int ch, int nKey, long lTick) throws InvalidMidiDataException {
		return createNoteEvent(ShortMessage.NOTE_OFF,
							   ch,
							   nKey,
							   0,
							   lTick);
	}

	protected MidiEvent createNoteEvent(int nCommand,
										int ch,
										int nKey,
										int nVelocity,
										long lTick) throws InvalidMidiDataException {
		ShortMessage	message = new ShortMessage();
		message.setMessage(nCommand,
						   ch,
						   nKey,
						   nVelocity);
		MidiEvent	event = new MidiEvent(message,
										  lTick);
		return event;
	}
	
	public MidiEvent createProgramChangeEvent(int ch, int pgnum, long lTick) throws InvalidMidiDataException {
		ShortMessage	message = new ShortMessage();
		message.setMessage(ShortMessage.PROGRAM_CHANGE, ch, pgnum, 0);

		MidiEvent	event = new MidiEvent(message, lTick);
		return event;
	}

	public MidiEvent createSysexEvent(int ch, JsonArray arr, long lTick) throws InvalidMidiDataException  {
		SysexMessage	message = new SysexMessage();
		Context lctxt = this.logiasContext.createChild();
		Sexp channel = new Literal(ch);
		lctxt.bind("channel", channel);
		Logias logias = new Logias(lctxt);
		Sexp sysexsexp = logias.buildSexp(arr);
		Sexp sexp = logias.run(sysexsexp);
		if (Sexp.nil.equals(sexp)) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(SysexMessage.SYSTEM_EXCLUSIVE);    // status: SysEx start
		Iterator<Sexp> i = sexp.iterator().assumeList();
		while (i.hasNext()) {
			Sexp cur = i.next();
			baos.write((byte)cur.asAtom().longValue());
		}
		baos.write(SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE);    // End of exclusive 
		try { baos.close(); } catch (IOException e) {}
		byte[] data = baos.toByteArray();
		message.setMessage(data, data.length);
		MidiEvent ret= new MidiEvent(message, lTick);
		return ret;
	}
	public MidiEvent createControlChangeEvent(int ch, int controllernum, int param, long lTick) throws InvalidMidiDataException {
		ShortMessage	message = new ShortMessage();
		message.setMessage(ShortMessage.CONTROL_CHANGE, ch, controllernum, param);
		MidiEvent	event = new MidiEvent(message, lTick);
		return event;
	}

	public MidiEvent createBankSelectMSBEvent(int ch, int bkmsb, long lTick) throws InvalidMidiDataException {
		return createControlChangeEvent(ch, 0, bkmsb, lTick);
	}
	
	public MidiEvent createBankSelectLSBEvent(int ch, int bklsb, long lTick) throws InvalidMidiDataException {
		return createControlChangeEvent(ch, 32, bklsb, lTick);
	}

	public MidiEvent createVolumeChangeEvent(int ch, int volume, long lTick) throws InvalidMidiDataException {
		return createControlChangeEvent(ch, 7, volume, lTick);
	}

	public MidiEvent createPanChangeEvent(int ch, int pan, long lTick) throws InvalidMidiDataException {
		return createControlChangeEvent(ch, 10, pan, lTick);
	}

	public MidiEvent createReverbEvent(int ch, int depth, long lTick) throws InvalidMidiDataException {
		return createControlChangeEvent(ch, 91, depth, lTick);
	}

	public MidiEvent createChorusEvent(int ch, int depth, long lTick) throws InvalidMidiDataException {
		return createControlChangeEvent(ch, 93, depth, lTick);
	}

	public MidiEvent createPitchBendEvent(int ch, int depth, long lTick) throws InvalidMidiDataException {
		ShortMessage	message = new ShortMessage();
		message.setMessage(ShortMessage.PITCH_BEND, ch, 0, depth);
		MidiEvent	event = new MidiEvent(message, lTick);
		return event;
	}
	
	public MidiEvent createModulationEvent(int ch, int depth, long lTick) throws InvalidMidiDataException {
		return createControlChangeEvent(ch, 1, depth, lTick);
	}
	
	public MidiEvent createAfterTouchChangeEvent(int ch, int v, long lTick) throws InvalidMidiDataException {
		ShortMessage	message = new ShortMessage();
		message.setMessage(ShortMessage.CHANNEL_PRESSURE, ch, v, 0);
		MidiEvent	event = new MidiEvent(message, lTick);
		return event;
	}

	public MidiEvent createTempoEvent(int tempo, long lTick) throws InvalidMidiDataException {
		int mpqn = 60000000 / tempo;
		MetaMessage mm = new MetaMessage();
		byte[] data = Util.getIntBytes(mpqn);
		mm.setMessage(0x51, data, data.length);
		return new MidiEvent(mm, lTick);
	}
	
	public void noteProcessed() {
		System.out.print(".");
	}
	
	public void controlEventProcessed() {
		System.out.print("*");
	}
	
	public void sysexEventProcessed() {
		System.out.print("X");
	}
	
	public void barStarted(int barid) {
		System.out.println("bar:<" + barid + "> ");
	}
	
	public void patternStarted() {
		System.out.print("[");
	}
	
	public void patternEnded() {
		System.out.print("]");
	}
	
	public void barEnded() {
	}
	
	public void partStarted(String partName) {
		System.out.print("    part:<" + partName + ">");
	}
	
	public void strokeEnded() {
		System.out.print("|");
	}

	public void partEnded() {
		System.out.println();
	}
}
