package net.sourceforge.symfonion.misc;

import java.io.File;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class Seq {
	/*	This velocity is used for all notes.
	 */
	private static final int	VELOCITY = 64;


	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			printUsageAndExit();
		}
		File outputFile = new File(args[0]);
		Sequence	sequence = null;
		sequence = new Sequence(Sequence.PPQ, 96);
			
		/* Track objects cannot be created by invoking their constructor
		   directly. Instead, the Sequence object does the job. So we
		   obtain the Track there. This links the Track to the Sequence
		   automatically.
		*/
		Track	track = sequence.createTrack();
		Track	track2 = sequence.createTrack();
		track.add(createPatchChangeEvent(1, 12));
		track.add(createPatchChangeEvent(2, 1));
		// first chord: C major
		for (int i = 0; i < 8; i ++) {
			track.add(createNoteOnEvent(1, 60, i * 96 + 0));
			track.add(createNoteOffEvent(1, 60, i * 96 + 2));
			track2.add(createNoteOnEvent(2, 60, i * 96 + 0));
			track2.add(createNoteOffEvent(2, 60, i * 96 + 2));
		}
		
		
		/*		
		track.add(createNoteOnEvent(64, 0));
		track.add(createNoteOnEvent(67, 0));
		track.add(createNoteOnEvent(72, 0));
		track.add(createNoteOffEvent(64, 96));
		track.add(createNoteOffEvent(67, 96));
		track.add(createNoteOffEvent(72, 96));
		// second chord: f minor N
		track.add(createNoteOnEvent(53, 1));
		track.add(createNoteOnEvent(65, 1));
		track.add(createNoteOnEvent(68, 1));
		track.add(createNoteOnEvent(73, 1));
		track.add(createNoteOffEvent(63, 2));
		track.add(createNoteOffEvent(65, 2));
		track.add(createNoteOffEvent(68, 2));
		track.add(createNoteOffEvent(73, 2));

		// third chord: C major 6-4
		track.add(createNoteOnEvent(55, 2));
		track.add(createNoteOnEvent(64, 2));
		track.add(createNoteOnEvent(67, 2));
		track.add(createNoteOnEvent(72, 2));
		track.add(createNoteOffEvent(64, 3));
		track.add(createNoteOffEvent(72, 3));

		// forth chord: G major 7
		track.add(createNoteOnEvent(65, 3));
		track.add(createNoteOnEvent(71, 3));
		track.add(createNoteOffEvent(55, 4));
		track.add(createNoteOffEvent(65, 4));
		track.add(createNoteOffEvent(67, 4));
		track.add(createNoteOffEvent(71, 4));

		// fifth chord: C major
		track.add(createNoteOnEvent(48, 4));
		track.add(createNoteOnEvent(64, 4));
		track.add(createNoteOnEvent(67, 4));
		track.add(createNoteOnEvent(72, 4));
		track.add(createNoteOffEvent(48, 8));
		track.add(createNoteOffEvent(64, 8));
		track.add(createNoteOffEvent(67, 8));
		track.add(createNoteOffEvent(72, 8));
*/
		/* Now we just save the Sequence to the file we specified.
		   The '0' (second parameter) means saving as SMF type 0.
		   Since we have only one Track, this is actually the only option
		   (type 1 is for multiple tracks).
		*/
		MidiSystem.write(sequence, 1, outputFile);
	}



	private static MidiEvent createPatchChangeEvent(int ch, int i) throws InvalidMidiDataException {
		ShortMessage message =  new ShortMessage();
		message.setMessage(ShortMessage.PROGRAM_CHANGE, ch, i, 0);
		return new MidiEvent(
				message,
				0
				);
	}



	private static MidiEvent createNoteOnEvent(int ch, int nKey, long lTick)
	{
		return createNoteEvent(ShortMessage.NOTE_ON,
							   ch,
							   nKey,
							   VELOCITY,
							   lTick);
	}



	private static MidiEvent createNoteOffEvent(int ch, int nKey, long lTick)
	{
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
											 long lTick)
	{
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



	private static void printUsageAndExit()
	{
			out("usage:");
			out("java CreateSequence <midifile>");
			System.exit(1);
	}


	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}
