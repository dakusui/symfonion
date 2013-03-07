package net.sourceforge.symfonion;

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

import net.sourceforge.logias.Logias;
import net.sourceforge.logias.lisp.Context;
import net.sourceforge.logias.lisp.s.Literal;
import net.sourceforge.logias.lisp.s.Sexp;
import net.sourceforge.symfonion.core.ExceptionThrower;
import net.sourceforge.symfonion.core.SymfonionException;
import net.sourceforge.symfonion.core.Util;
import net.sourceforge.symfonion.song.Bar;
import net.sourceforge.symfonion.song.Part;
import net.sourceforge.symfonion.song.Pattern;
import net.sourceforge.symfonion.song.Pattern.Parameters;
import net.sourceforge.symfonion.song.Song;
import net.sourceforge.symfonion.song.Stroke;

import com.google.gson.JsonArray;

public class MidiCompiler {
	public static class CompilerContext {
		private Track track;
		private int channel;
		private Parameters params;
		private long position;
		private int resolution;

		public CompilerContext(Track track, int channel, Parameters params,
				long position, int resolution) {
			this.track = track;
			this.channel = channel;
			this.params = params;
			this.position = position;
			this.resolution = resolution;
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

		public int getResolution() {
			return resolution;
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
		
		// Giving the sequencer a grace period to initialize its internal state.
		long position = resolution / 4; 
		int barid = 0;
		for (Bar bar : song.bars()) {
			barStarted(barid);
			for (String partName : bar.partNames()) {
				partStarted(partName);
				Track track = tracks.get(partName);
				if (track == null) {
					ExceptionThrower.throwInstrumentNotFound("Instrument:<" + partName + "> is not found.", null);
				}
				int channel = song.part(partName).channel(); 
				for (Pattern pattern : bar.part(partName)) {
					patternStarted();
					long positionDelta = 0;
					Parameters params = pattern.parameters();
					for (Stroke stroke : pattern.strokes()) {
						try {
							stroke.compile(this, new CompilerContext(track, channel, params, position + positionDelta, resolution));
							positionDelta += stroke.length().doubleValue() * resolution;
							if (positionDelta >= bar.beats().doubleValue() * resolution) {
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
			position += bar.beats().doubleValue() * resolution;
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
