package com.github.dakusui.symfonion;

import static java.lang.String.format;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.github.dakusui.symfonion.song.Keyword;
import com.github.dakusui.symfonion.song.Song;

public class Symfonion {
	Context logiasContext;
	
	public Symfonion(Context logiasContext) {
		this.logiasContext = logiasContext;
	}
	
	public Song load(String fileName) throws SymfonionException {
		Song ret = new Song(logiasContext, JsonUtil.toJson(Util.loadFile(fileName)).getAsJsonObject());
		ret.init();
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
			System.out.println("Starting " + portName + "(" + System.currentTimeMillis() + ")");
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
	
	static MidiDevice getMidiInDevice(String deviceNamePattern) {
		MidiDevice ret = null;
		MidiDevice.Info[] infoItems = MidiSystem.getMidiDeviceInfo();
		Pattern p = Pattern.compile(deviceNamePattern == null ? ".*" : deviceNamePattern);
		System.out.println("Device name pattern=<" + deviceNamePattern + ">");
		for (MidiDevice.Info info : infoItems) {
			System.out.println("   <" + info + ">: vendor:(" + info.getVendor() + "), name:(" + info.getName() + ") , desc:(" + info.getDescription() + ")");
			Matcher m = p.matcher(info.getName());
			if (m.find()) {
				Object tmp = null;
				MidiDevice dev = null;
				try {
					dev = MidiSystem.getMidiDevice(info);
					dev.open();
					try {
						tmp = dev.getTransmitter();
					} finally {
						dev.close();
					}
				} catch (Exception e) {
				}
				System.out.println("    ..." + tmp);
				if (tmp != null) {
					ret = dev;
					break;
				}
			}
		}
		System.out.println(format("<%s> is chosen for MIDI-in device.", ret.getDeviceInfo().getName()));		
		return ret;
	}
	
	public static MidiDevice getMidiOutDevice(String deviceNamePattern) {
		MidiDevice ret = null;
		MidiDevice.Info[] infoItems = MidiSystem.getMidiDeviceInfo();
		System.out.println("Device name pattern=<" + deviceNamePattern + ">");
		Pattern p = Pattern.compile(deviceNamePattern == null ? ".*" : deviceNamePattern);
		for (MidiDevice.Info info : infoItems) {
			System.out.println("   <" + info + ">: vendor:(" + info.getVendor() + "), name:(" + info.getName() + ") , desc:(" + info.getDescription() + ")");
			Matcher m = p.matcher(info.getName());
			if (m.find()) {
				Object tmp = null;
				MidiDevice dev = null;
				try {
					dev = MidiSystem.getMidiDevice(info);
					dev.open();
					try {
						tmp = dev.getReceiver();
					} finally {
						dev.close();
					}
				} catch (Exception e) {
				}
				System.out.println("    ..." + tmp);
				if (tmp != null) {
					ret = dev;
					break;
				}
			}
		}
		if (ret != null) {
			String name = ret.getDeviceInfo().getName();
			String desc = ret.getDeviceInfo().getDescription();
			System.out.println(format("<%s, %s> is chosen for MIDI out device.", name, desc));
		} else {
			System.out.println(String.format("No matching device is found for <%s>.", p));
		}
		return ret;
	}

	private static void printMidiOutDevices() {
		MidiDevice.Info[] infoItems = MidiSystem.getMidiDeviceInfo();
		for (MidiDevice.Info info : infoItems) {
			MidiDevice dev = null;
			Object tmp = null;
			try {
				dev = MidiSystem.getMidiDevice(info);
				dev.open();
				try {
					tmp = dev.getReceiver();
				} finally {
					dev.close();
				}
			} catch (Exception e) {
			}
			if (tmp != null) {
				System.out.println(format("name=<%s>, vendor=<%s>, version=<%s>, description=<%s>", info.getName(), info.getVendor(), info.getVersion(), info.getDescription()));
			}
		}
	}

	private static File composeOutputFile(String outFile, String portName) {
		if (portName == null || Keyword.$default.equals(portName)) {
			return new File(outFile); 
		}
		File ret = null;
		int lastIndexOfDot = outFile.lastIndexOf('.'); 
		if (lastIndexOfDot == -1) {
			ret = new File(outFile + "." + portName);
		} else {
			ret = new File(outFile.substring(0, lastIndexOfDot) + "." + portName + outFile.substring(lastIndexOfDot));
		}
		return ret;
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length > 0 && "--list".equals(args[0])) {
			printMidiOutDevices();
			System.exit(0);
		}
		if (args.length == 2 || args.length == 1) {
			String infile = args[0];
			Symfonion symfonion = new Symfonion(Context.ROOT);
			Song song = symfonion.load(infile);
			Map<String, Sequence> sequences = symfonion.compile(song); 
			if (args.length > 1) {
				for (String portName : sequences.keySet()) {
					Sequence seq = sequences.get(portName);
					String outfile = args[1];
					File outputFile = composeOutputFile(outfile, portName);
					MidiSystem.write(seq, 1, outputFile);
				}
			} else {
				Map<String, MidiDevice> devices = new HashMap<String, MidiDevice>();
				for (String portName : sequences.keySet()) {
					MidiDevice dev = getMidiOutDeviceForPortName(portName);
					devices.put(portName, dev);
				}
				symfonion.play(devices, sequences);
			}
		} else {
			System.err.println("Usage: java -jar symfonion.jar infile [outfile]");
			System.exit(1);
		}
	}

	private static MidiDevice getMidiOutDeviceForPortName(String portName) {
		String propertyKey = "symfonion.midi.out";
		if (portName != null && !Keyword.$default.toString().equals(portName))  {
			propertyKey += "." + portName;
		}
		return getMidiOutDevice(System.getProperty(propertyKey));
	}

}
