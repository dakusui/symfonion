package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.RouteRequest;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.exceptions.CliException;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceScanner;
import com.github.dakusui.symfonion.utils.midi.MidiUtils;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsg;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.*;
import static java.lang.String.format;

public class PatchBay implements Subcommand {
  @Override
  public void invoke(Cli cli, PrintStream ps) throws CliException {
    RouteRequest route = cli.getRouteRequest();
    String inPortName = route.in();
    String outPortName = route.out();

    Map<String, Pattern> inDefs = cli.getMidiInDefinitions();

    if (!inDefs.containsKey(inPortName)) {
      throw new CliException(composeErrMsg(format("MIDI-in port '%s' is specified, but it is not defined by '-I' option.", inPortName), "r", "--route"));
    }
    Pattern inRegex = inDefs.get(inPortName);
    MidiDeviceScanner inScanner = MidiUtils.chooseInputDevices(System.out, inRegex);
    inScanner.scan();
    MidiDevice.Info[] matchedInDevices = inScanner.getMatchedDevices();
    if (matchedInDevices.length != 1) {
      throw new CliException(composeErrMsg(format("MIDI-in device for %s(%s) is not found or found more than one.", inPortName, inRegex), "I", null));
    }

    ps.println();

    Map<String, Pattern> outDefs = cli.getMidiOutDefinitions();
    if (!outDefs.containsKey(outPortName)) {
      throw new CliException(composeErrMsg(format("MIDI-out port '%s' is specified, but it is not defined by '-O' option.", inPortName), "r", "route"));
    }
    Pattern outRegex = outDefs.get(outPortName);
    MidiDeviceScanner outScanner = MidiUtils.chooseOutputDevices(System.out, outRegex);
    outScanner.scan();
    MidiDevice.Info[] matchedOutDevices = outScanner.getMatchedDevices();
    if (matchedOutDevices.length != 1) {
      throw new CliException(composeErrMsg(format("MIDI-out device for %s(%s) is not found or found more than one.", outPortName, outRegex), "I", null));
    }
    ps.println();
    MidiDevice inMidiDevice;
    try {
      inMidiDevice = MidiSystem.getMidiDevice(matchedInDevices[0]);
    } catch (MidiUnavailableException e) {
      throw failedToOpenMidiIn(e, matchedInDevices[0]);
    }
    MidiDevice outMidiDevice;
    try {
      outMidiDevice= MidiSystem.getMidiDevice(matchedOutDevices[0]);
    } catch (MidiUnavailableException e) {
      throw failedToOpenMidiOut(e, matchedInDevices[0]);
    }
    patchBay(inMidiDevice, outMidiDevice);
  }

  public static void patchBay(MidiDevice inMidiDevice, MidiDevice outMidiDevice)
      throws CliException {
    try {
      outMidiDevice.open();
    } catch (MidiUnavailableException e) {
      throw failedToOpenMidiOut(e, outMidiDevice.getDeviceInfo());
    }
    try (outMidiDevice) {
      try {
        inMidiDevice.open();
      } catch (MidiUnavailableException ee) {
        throw failedToOpenMidiIn(ee, inMidiDevice.getDeviceInfo());
      }
      try (inMidiDevice) {
        try (Receiver r = outMidiDevice.getReceiver()) {
          try (Transmitter t = inMidiDevice.getTransmitter()) {
            t.setReceiver(r);
            System.err.println("Now in MIDI patch-bay mode. Hit enter to quit.");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
          } catch (IOException e) {
            System.err.println("quitting due to an error.");
          } finally {
            System.err.println("closing transmitter");
          }
        } catch (MidiUnavailableException e) {
          throw failedToRetrieveTransmitterFromMidiIn(e, inMidiDevice.getDeviceInfo());
        } finally {
          System.err.println("closing receiver");
        }
      }
    }
  }
}
