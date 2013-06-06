package com.github.dakusui.symfonion;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice.Info;

public abstract class MidiDeviceScanner {
	static abstract class RegexMidiDeviceScanner extends MidiDeviceScanner {

		private Pattern regex;

		public RegexMidiDeviceScanner(PrintStream ps, Pattern regex) {
			super(ps);
			this.regex = regex;
		}

		@Override
		protected void start(Info[] allDevices) {
			PrintStream ps = getPrintStream();
			ps.println("     " + getHeader());
			ps.println(String.format("  io %s", format(null)));
			ps.println("---------------------------------------------------------------------------");
		}

		protected abstract String getHeader();

		@Override
		protected boolean matches(Info device) {
			Matcher m = regex.matcher(device.getName());
			return m.find();
		}

		@Override
		protected void matched(Info device) {
			String i = isInputDevice(device) ? "I" : " ";
			String o = isOutputDevice(device) ? "O" : " ";
			getPrintStream().println(String.format("* %1s%1s %s", i, o, format(device)));
		}

		@Override
		protected void scanned(Info device) {
		}

		@Override
		protected void end(Info[] matchedDevices) {
		}

		@Override
		protected void didntMatch(Info device) {
			String i = isInputDevice(device) ? "I" : " ";
			String o = isOutputDevice(device) ? "O" : " ";
			getPrintStream().println(String.format("  %1s%1s %s", i, o, format(device)));
		}
	}
		
	private PrintStream ps;
	private MidiDevice.Info[] matchedDevices = null;
	
	protected abstract void start(MidiDevice.Info[] allDevices);
	protected abstract boolean matches(MidiDevice.Info device);
	protected abstract void matched(MidiDevice.Info device);
	protected abstract void scanned(Info device);
	protected abstract void end(MidiDevice.Info[] matchedDevices);
	
	public MidiDeviceScanner(PrintStream ps) {
		this.ps = ps;
	}
	
	public void scan() {
		List<MidiDevice.Info> matched = new LinkedList<MidiDevice.Info>();
		MidiDevice.Info[] allDevices = MidiSystem.getMidiDeviceInfo();
		
		start(allDevices);
		for (MidiDevice.Info cur : allDevices) {
			if (matches(cur)) {
				matched(cur);
				matched.add(cur);
			} else {
				didntMatch(cur);
			}
			scanned(cur);
		}
		end(matchedDevices = matched.toArray(new MidiDevice.Info[0]));
	}
	

	protected abstract void didntMatch(MidiDevice.Info info);

	public boolean isOutputDevice(MidiDevice.Info info) {
		Object tmp = null;
		try {
			MidiDevice dev = MidiSystem.getMidiDevice(info);
			dev.open();
			try {
				tmp = dev.getReceiver();
			} finally {
				dev.close();
			}
		} catch (Exception e) {
		}
		return tmp != null;
	}

	public boolean isInputDevice(MidiDevice.Info device) {
		Object tmp = null;
		try {
			MidiDevice dev = MidiSystem.getMidiDevice(device);
			dev.open();
			try {
				tmp = dev.getTransmitter();
			} finally {
				dev.close();
			}
		} catch (Exception e) {
		}
		return tmp != null;
	}
	
	public PrintStream getPrintStream() {
		return this.ps;
	}
	
	public void setPrintStream(PrintStream ps) {
		this.ps = ps;
	}
	
	protected String format(MidiDevice.Info info) {
		return String.format(
				"%-20s %-15s %-35s",
				info == null ? "name" : info.getName(), 
				info == null ? "version" : info.getVersion(), 
				info == null ? "vendor" : info.getVendor() 
		);
	}
	
	public MidiDevice.Info[] getMatchedDevices() {
		return this.matchedDevices;
	}
	
	public static MidiDeviceScanner listAllDevices(PrintStream ps) {
		return new MidiDeviceScanner(ps) {
			@Override
			protected void start(Info[] allDevices) {
				PrintStream ps = getPrintStream();
				ps.println("     Available MIDI devices");
				ps.println(String.format("  io %s", format(null)));
				ps.println("---------------------------------------------------------------------------");
			}
			
			@Override
			protected boolean matches(Info device) {
				return true;
			}
			
			@Override
			protected void matched(Info device) {
			}
			
			@Override
			protected void end(Info[] matchedDevices) {
			}

			@Override
			protected void scanned(Info device) {
				String i = isInputDevice(device) ? "I" : " ";
				String o = isOutputDevice(device) ? "O" : " ";
				getPrintStream().println(String.format("  %1s%1s %s", i, o, format(device)));
			}

			@Override
			protected void didntMatch(Info info) {
			}
		};
	}
	
	public static MidiDeviceScanner chooseInputDevices(PrintStream ps, Pattern regex) {
		return new RegexMidiDeviceScanner(ps, regex) {
			@Override
			protected String getHeader() {
				return "MIDI-in devices";
			}
			@Override
			protected boolean matches(Info device) {
				return super.matches(device) && isInputDevice(device);
			}
		};
	}

	public static MidiDeviceScanner chooseOutputDevices(PrintStream ps, Pattern regex) {
		return new RegexMidiDeviceScanner(ps, regex) {
			@Override
			protected String getHeader() {
				return "MIDI-out devices";
			}
			@Override
			protected boolean matches(Info device) {
				return super.matches(device) && isOutputDevice(device);
			}
		};
	}

	public static void main(String[] args) {
		chooseInputDevices(System.out, Pattern.compile("Real")).scan();
		System.out.println();
		chooseOutputDevices(System.out, Pattern.compile("Ger")).scan();
		System.out.println();
		listAllDevices(System.out).scan();
	}
}
