package net.sourceforge.symfonion;

import static java.lang.String.format;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

import net.sourceforge.logias.lisp.Context;
import net.sourceforge.symfonion.core.ExceptionThrower;
import net.sourceforge.symfonion.core.JsonUtil;
import net.sourceforge.symfonion.core.SymfonionException;
import net.sourceforge.symfonion.core.Util;
import net.sourceforge.symfonion.song.Song;

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
	
	public Sequence compile(Song song) throws SymfonionException {
		MidiCompiler compiler = new MidiCompiler(song.getLogiasContext());
		Sequence ret = null;
		try {
			ret = compiler.compile(song);
		} catch (InvalidMidiDataException e) {
			ExceptionThrower.throwCompilationException("Failed to compile a song.", e);
		}
		return ret;
	}
	
	public synchronized void play(MidiDevice dev, Sequence seq) throws SymfonionException {
		Sequencer sequencer;
		try {
			sequencer = MidiSystem.getSequencer(false);
			sequencer.open();
			if (dev != null) {
				dev.open();
				sequencer.getTransmitter().setReceiver(dev.getReceiver());
			}
			try {
				final Symfonion symfonion = this;
				sequencer.setSequence(seq);
				sequencer.start();
				sequencer.addMetaEventListener(new MetaEventListener() {

					public void meta(MetaMessage meta) {
						if (meta.getType() == 47) {
							synchronized (symfonion) {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
								}
								symfonion.notifyAll();
							}
						}
					}
				});
				this.wait();
			} finally {
				if (dev != null) {
					dev.close();
				}
				sequencer.close();
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

	public static void main(String[] args) throws Exception {
		if (args.length > 0 && "--list".equals(args[0])) {
			printMidiOutDevices();
			System.exit(0);
		}
		if (args.length == 2 || args.length == 1) {
			String infile = args[0];
			Symfonion symfonion = new Symfonion(Context.ROOT);
			Song song = symfonion.load(infile);
			Sequence seq = symfonion.compile(song);
			if (args.length > 1) {
				String outfile = args[1];
				File outputFile = new File(outfile);
				MidiSystem.write(seq, 1, outputFile);
			} else {
				Synthesizer synth = MidiSystem.getSynthesizer();
				for (MidiChannel ch : synth.getChannels()) {
					ch.allNotesOff();
					ch.resetAllControllers();
				}
				MidiDevice dev = getMidiOutDevice(System.getProperty("symfonion.midi.out"));
				symfonion.play(dev, seq);
			}
		} else {
			System.err.println("Usage: java -jar symfonion.jar infile [outfile]");
			System.exit(1);
		}
	}

}
