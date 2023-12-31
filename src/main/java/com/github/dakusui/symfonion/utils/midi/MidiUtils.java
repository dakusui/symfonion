package com.github.dakusui.symfonion.utils.midi;

import com.github.dakusui.symfonion.exceptions.CliException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;

public enum MidiUtils {
  ;

  public static Stream<MidiDevice.Info> streamMidiDeviceInfo() {
    return Arrays.stream(MidiSystem.getMidiDeviceInfo());
  }

  public static String formatMidiDeviceInfo(MidiDevice.Info info) {
    return String.format("%-25s %-15s %-35s", info.getName(), info.getVersion(), info.getVendor());
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

  public static MidiDeviceScanner chooseOutputDevices(
      PrintStream ps, Pattern regex) {
    return new MidiDeviceScanner.RegexMidiDeviceScanner(ps, regex) {
      @Override
      protected String getTitle() {
        return "MIDI-out devices";
      }

      @Override
      protected boolean matches(MidiDevice.Info device) {
        return super.matches(device) && isMidiDeviceForOutput(device);
      }
    };
  }

  public static MidiDevice.Info[] getInfos(String portName, MidiDeviceScanner scanner, Pattern regex) throws CliException {
    MidiDevice.Info[] matchedInfos = scanner.getMatchedDevices();
    if (matchedInfos.length > 1) {
      String msg = format("Device for port '%s' (regex:%s) wasn't unique (%d)", portName, regex, matchedInfos.length);
      throw new CliException(msg);
    } else if (matchedInfos.length == 0) {
      String msg = format("No matching device was found for port '%s' (regex:%s)", portName, regex);
      throw new CliException(msg);
    }
    return matchedInfos;
  }
}
