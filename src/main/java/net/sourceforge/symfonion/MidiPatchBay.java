package net.sourceforge.symfonion;

import java.io.IOException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;


public class MidiPatchBay {
	public static void main(String[] args) throws MidiUnavailableException {
		MidiDevice midiout = Symfonion.getMidiOutDevice(System.getProperty("symfonion.midi.out"));
		midiout.open();
		try {
			MidiDevice midiin  = Symfonion.getMidiInDevice(System.getProperty("symfonion.midi.in"));
			midiin.open();
			try {
				Receiver r = midiout.getReceiver();
				try {
					Transmitter t = midiin.getTransmitter();
					try {
						t.setReceiver(r);
						System.in.read();
					} catch (IOException e) {
						System.out.println("quitting due to an error.");
					} finally {
						System.out.println("closing transmitter");
						t.close();
					}
				} finally {
					System.out.println("closing receiver");
					r.close();
				}
			} finally {
				midiin.close();
			}
		} finally {
			midiout.close();
		}
	}


	public static void testMidiInDevice(String[] args) throws MidiUnavailableException {
		MidiDevice midiin  = Symfonion.getMidiInDevice(System.getProperty("symfonion.midi.in"));
		midiin.open();
		try {
			Transmitter t = midiin.getTransmitter();
			try {
				t.setReceiver(new Receiver() {
					@Override
					public void send(MidiMessage message, long timeStamp) {
						System.out.println(String.format("%d:<%s>", timeStamp, message.toString()));
					}
					@Override
					public void close() {
					}
				});
				System.in.read();
			} catch (IOException e) {
				System.out.println("quitting due to an error.");
			} finally {
				System.out.println("closing transmitter");
				t.close();
			}
		} finally {
			midiin.close();
		}

	}
}
