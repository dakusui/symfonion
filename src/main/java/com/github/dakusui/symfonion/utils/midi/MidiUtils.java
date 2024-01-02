package com.github.dakusui.symfonion.utils.midi;

import com.github.dakusui.symfonion.exceptions.CLIException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.io.PrintStream;
import java.util.regex.Pattern;

import static java.lang.String.format;

public enum MidiUtils {
  ;

  public static String formatMidiDeviceInfo(MidiDevice.Info info) {
    return String.format(
        "%-25s %-15s %-35s",
        info == null ? "name" : info.getName(),
        info == null ? "version" : info.getVersion(),
        info == null ? "vendor" : info.getVendor()
    );
  }

  public static boolean isMidiDeviceForInput(MidiDevice.Info device) {
    Object tmp = null;
    try {
      MidiDevice dev = MidiSystem.getMidiDevice(device);
try (dev) {
dev.open();
tmp = dev.getTransmitter();
}
    } catch (Exception ignored) {
    }
    return tmp != null;
  }

  public static boolean isMidiDeviceForOutput(MidiDevice.Info info) {
    Object tmp = null;
    try {
      MidiDevice dev = MidiSystem.getMidiDevice(info);
try (dev) {
dev.open();
tmp = dev.getReceiver();
}
    } catch (Exception ignored) {
    }
    return tmp != null;
  }

  public static MidiDeviceScanner listAllDevices(PrintStream ps) {
    return new MidiDeviceScanner(ps) {
      @Override
      protected void start(MidiDevice.Info[] allDevices) {
        PrintStream ps = getPrintStream();
        ps.println("     Available MIDI devices");
        ps.printf("  io %s%n", formatMidiDeviceInfo(null));
        ps.println("---------------------------------------------------------------------------");
      }

      @Override
      protected boolean matches(MidiDevice.Info device) {
        return true;
      }

      @Override
      protected void matched(MidiDevice.Info device) {
      }

      @Override
      protected void end(MidiDevice.Info[] matchedDevices) {
      }

      @Override
      protected void scanned(MidiDevice.Info device) {
        String i = isMidiDeviceForInput(device) ? "I" : " ";
        String o = isMidiDeviceForOutput(device) ? "O" : " ";
        getPrintStream().printf("  %1s%1s %s%n", i, o, formatMidiDeviceInfo(device));
      }

      @Override
      protected void didntMatch(MidiDevice.Info info) {
      }
    };
  }

  public static MidiDeviceScanner chooseInputDevices(PrintStream ps, Pattern regex) {
    return new MidiDeviceScanner.RegexMidiDeviceScanner(ps, regex) {
      @Override
      protected String getHeader() {
        return "MIDI-in devices";
      }
      @Override
      protected boolean matches(MidiDevice.Info device) {
        return super.matches(device) && isMidiDeviceForInput(device);
      }
    };
  }

  public static MidiDeviceScanner chooseOutputDevices(PrintStream ps, Pattern regex) {
    return new MidiDeviceScanner.RegexMidiDeviceScanner(ps, regex) {
      @Override
      protected String getHeader() {
        return "MIDI-out devices";
      }
      @Override
      protected boolean matches(MidiDevice.Info device) {
        return super.matches(device) && isMidiDeviceForOutput(device);
      }
    };
  }

  public static MidiDevice.Info[] getInfos(String portName, MidiDeviceScanner scanner, Pattern regex) throws CLIException {
    MidiDevice.Info[] matchedInfos = scanner.getMatchedDevices();
    if (matchedInfos.length > 1) {
      String msg = format("Device for port '%s' (regex:%s) wasn't unique (%d)", portName, regex, matchedInfos.length);
      throw new CLIException(msg);
    } else if (matchedInfos.length == 0) {
      String msg = format("No matching device was found for port '%s' (regex:%s)", portName, regex);
      throw new CLIException(msg);
    }
    return matchedInfos;
  }
}
