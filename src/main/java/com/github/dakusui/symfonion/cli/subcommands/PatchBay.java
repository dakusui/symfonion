package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.CliUtils;
import com.github.dakusui.symfonion.cli.Route;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.exceptions.CLIException;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceScanner;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsg;
import static java.lang.String.format;

public class PatchBay implements Subcommand {
  @Override
  public void invoke(Cli cli, PrintStream ps) throws CLIException {
    Route route = cli.getRoute();
    String inPortName = route.in();
    String outPortName = route.out();

    Map<String, Pattern> inDefs = cli.getMidiInDefinitions();

    if (!inDefs.containsKey(inPortName)) {
      throw new CLIException(composeErrMsg(format("MIDI-in port '%s' is specified, but it is not defined by '-I' option.", inPortName), "r", "--route"));
    }
    Pattern inRegex = inDefs.get(inPortName);
    MidiDeviceScanner inScanner = MidiDeviceScanner.chooseInputDevices(System.out, inRegex);
    inScanner.scan();
    MidiDevice.Info[] matchedInDevices = inScanner.getMatchedDevices();
    if (matchedInDevices.length != 1) {
      throw new CLIException(composeErrMsg(format("MIDI-in device for %s(%s) is not found or found more than one.", inPortName, inRegex), "I", null));
    }

    ps.println();

    Map<String, Pattern> outDefs = cli.getMidiOutDefinitions();
    if (!outDefs.containsKey(outPortName)) {
      throw new CLIException(composeErrMsg(format("MIDI-out port '%s' is specified, but it is not defined by '-O' option.", inPortName), "r", "route"));
    }
    Pattern outRegex = outDefs.get(outPortName);
    MidiDeviceScanner outScanner = MidiDeviceScanner.chooseOutputDevices(System.out, outRegex);
    outScanner.scan();
    MidiDevice.Info[] matchedOutDevices = outScanner.getMatchedDevices();
    if (matchedOutDevices.length != 1) {
      throw new CLIException(composeErrMsg(format("MIDI-out device for %s(%s) is not found or found more than one.", outPortName, outRegex), "I", null));
    }
    ps.println();
    patchBay(matchedInDevices[0], matchedOutDevices[0]);
  }

  public static void patchBay(MidiDevice.Info in, MidiDevice.Info out)
      throws CLIException {
    MidiDevice midiOut;
    try {
      midiOut = MidiSystem.getMidiDevice(out);
      midiOut.open();
    } catch (MidiUnavailableException e) {
      throw new CLIException(format("(-) Failed to open MIDI-out device (%s)", out.getName()), e);
    }
    try {
      MidiDevice midiIn;
      try {
        midiIn = MidiSystem.getMidiDevice(in);
        midiIn.open();
      } catch (MidiUnavailableException ee) {
        throw new CLIException(format("(-) Failed to open MIDI-in device (%s)", in.getName()), ee);
      }
      try {
        try (Receiver r = midiOut.getReceiver()) {
          try (Transmitter t = midiIn.getTransmitter()) {
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
          throw new CLIException(format("(-) Failed to get transmitter from MIDI-in device (%s)", in.getName()), e);
        } finally {
          System.err.println("closing receiver");
        }
      } finally {
        midiIn.close();
      }
    } finally {
      midiOut.close();
    }
  }
}
