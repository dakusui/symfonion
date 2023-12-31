package com.github.dakusui.symfonion.utils.midi;

import com.github.dakusui.symfonion.cli.MidiRouteRequest;
import com.github.dakusui.symfonion.exceptions.CliException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.dakusui.valid8j.ValidationFluents.all;
import static com.github.dakusui.valid8j_pcond.fluent.Statement.objectValue;
import static java.lang.String.format;

public enum MidiUtils {
  ;

  public static MidiDeviceManager createMidiDeviceManager() {
    return createMidiDeviceManager(MidiDeviceReportFormatter.createDefaultReportFormatter(), Arrays.stream(MidiSystem.getMidiDeviceInfo())
    );
  }

  public static MidiDeviceManager createMidiDeviceManager(Stream<MidiDevice.Info> midiDeviceInfoStream) {
    return createMidiDeviceManager(MidiDeviceReportFormatter.createDefaultReportFormatter(), midiDeviceInfoStream
    );
  }

  private static MidiDeviceManager createMidiDeviceManager(MidiDeviceReportFormatter reportFormatter, Stream<MidiDevice.Info> midiDeviceInfoStream) {
    MidiDeviceManager reportComposer = new MidiDeviceManager(reportFormatter);
    midiDeviceInfoStream.forEach(reportComposer::add);
    return reportComposer;
  }

  public record MidiRoute(MidiDevice.Info in, List<MidiDevice> outDevices) {
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

  public static Optional<MidiRoute> findRouteFor(MidiRouteRequest routeRequest, Map<String, Pattern> inDefs, Map<String, Pattern> outDefs, List<MidiDevice.Info> deviceInfoList) {
    assert all(
        objectValue(routeRequest).then().isNotNull().$(),
        objectValue(routeRequest).invoke("in").then().isNotNull().$(),
        objectValue(routeRequest).invoke("out").then().isNotNull().$(),
        objectValue(inDefs).then().isNotNull().$(),
        objectValue(inDefs).invoke("containsKey", routeRequest.in()).asBoolean().then().isTrue().$(),
        objectValue(outDefs).then().isNotNull().$(),
        objectValue(outDefs).invoke("containsKey", routeRequest.out()).asBoolean().then().isTrue().$(),
        objectValue(deviceInfoList).then().isNotNull().$());
    return findRouteFor(inDefs.get(routeRequest.in()), outDefs.get(routeRequest.out()), deviceInfoList);
  }

  public static Optional<MidiRoute> findRouteFor(Pattern regexForMidiInDevice, Pattern regexForMidiOutDevice, List<MidiDevice.Info> deviceInfoList) {
    return Optional.empty();
  }


  public static MidiDeviceScanner chooseInputDevices(PrintStream ps, Pattern regex) {
    return new MidiDeviceScanner.RegexMidiDeviceScanner(ps, regex) {
      @Override
      protected String getTitle() {
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
