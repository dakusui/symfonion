package com.github.dakusui.symfonion;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.core.ExceptionThrower;
import com.github.dakusui.symfonion.core.JsonUtil;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.core.Util;
import com.github.dakusui.symfonion.song.Song;
import com.google.gson.JsonSyntaxException;

public class Symfonion {
	Context logiasContext;
	
	public Symfonion(Context logiasContext) {
		this.logiasContext = logiasContext;
	}
	
	public Song load(String fileName) throws SymfonionException {
		Song ret = null;
		try {
			try {
				ret = new Song(logiasContext, JsonUtil.toJson(Util.loadFile(fileName)).getAsJsonObject());
				ret.init();
			} catch (JsonSyntaxException e) {
				ExceptionThrower.throwLoadFileException(new File(fileName), e.getCause());
			} catch (IllegalStateException e) {
				ExceptionThrower.throwLoadFileException(new File(fileName), e);
			}
		} catch (SymfonionException e) {
			e.setSourceFile(new File(fileName));
			throw e;
		}
		return ret;
	}
	
	public Map<String, Sequence> compile(Song song) throws SymfonionException {
		MidiCompiler compiler = new MidiCompiler(song.getLogiasContext());
		Map<String, Sequence> ret = null;
		try {
			ret = compiler.compile(song);
		} catch (InvalidMidiDataException e) {
			ExceptionThrower.throwCompilationException("Failed to compile a song.", e);
		}
		return ret;
	}
	
	private Map<String, Sequencer> prepareSequencers(List<String> portNames, Map<String, MidiDevice> devices, Map<String, Sequence> sequences) throws MidiUnavailableException, InvalidMidiDataException {
		Map<String, Sequencer> ret = new HashMap<String, Sequencer>();
		final List<Sequencer> playingSequencers = new LinkedList<Sequencer>();
		for (String portName : portNames) {
			final Sequencer sequencer = MidiSystem.getSequencer();
			playingSequencers.add(sequencer);
			sequencer.open();
			ret.put(portName, sequencer);
			MidiDevice dev = devices.get(portName);
			if (dev != null) {
				dev.open();
				for (Transmitter tr : sequencer.getTransmitters()) {
					tr.setReceiver(null);
				}
				sequencer.getTransmitter().setReceiver(dev.getReceiver());
			}
			sequencer.setSequence(sequences.get(portName)); 
			sequencer.addMetaEventListener(new MetaEventListener() {
				Sequencer seq = sequencer;
				@Override
				public void meta(MetaMessage meta) {
					if (meta.getType() == 0x2f) {
						synchronized (Symfonion.this) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
							playingSequencers.remove(this.seq);
							if (playingSequencers.isEmpty()) {
								Symfonion.this.notifyAll();
							}
						}
					}
				}
			});
		}
		return ret;
	}
	
	private void startSequencers(List<String> portNames, Map<String, MidiDevice> devices, Map<String, Sequencer> sequencers) {
		for (String portName : portNames) {
			System.out.println("Start playing on " + portName + "(" + System.currentTimeMillis() + ")");
			sequencers.get(portName).start();
		}
	}
	
	private void cleanUpSequencers(List<String> portNames, Map<String, MidiDevice> devices, Map<String, Sequencer> sequencers) {
		List<String> tmp = new LinkedList<String>();
		tmp.addAll(portNames);
		Collections.reverse(portNames);
		for (String portName : tmp) {
			MidiDevice dev = devices.get(portName);
			if (dev != null) {
				dev.close();
			}
			Sequencer sequencer = sequencers.get(portName);
			if (sequencer != null) {
				sequencer.close();
			}
		}
	}
	
	public synchronized void play(Map<String, MidiDevice> devices, Map<String, Sequence> sequences) throws SymfonionException {
		List<String> portNames = new LinkedList<String>();
		portNames.addAll(sequences.keySet());
		Map<String, Sequencer> sequencers;
		try {
			sequencers = prepareSequencers(portNames, devices, sequences);
			try {
				startSequencers(portNames, devices, sequencers);
				this.wait();
			} finally {
				System.out.println("Finished playing.");
				cleanUpSequencers(portNames, devices, sequencers);
			}
		} catch (MidiUnavailableException e) {
			ExceptionThrower.throwDeviceException("Midi device was not available.", e);
		} catch (InvalidMidiDataException e) {
			ExceptionThrower.throwDeviceException("Data was invalid.", e);
		} catch (InterruptedException e) {
			ExceptionThrower.throwDeviceException("Operation was interrupted.", e);
		}
	}
}
