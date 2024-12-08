package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.MidiRouteRequest;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.compat.exceptions.CliException;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceManager;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceRecord;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceReportFormatter;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsg;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.failedToRetrieveTransmitterFromMidiIn;
import static com.github.dakusui.symfonion.utils.midi.MidiDeviceManager.isMidiDeviceForInput;
import static com.github.dakusui.symfonion.utils.midi.MidiDeviceManager.matchesPortNameInDefinitions;
import static com.github.valid8j.pcond.forms.Predicates.and;
import static java.lang.String.format;

public class PatchBay implements Subcommand {
  @Override
  public void invoke(Cli cli, PrintStream ps, InputStream inputStream) throws SymfonionException, IOException {
    MidiRouteRequest route = cli.routeRequest();

    String inPortName = route.in();
    Map<String, Pattern> midiInDefinitions = requireMidiInDefinitionsContainsInputPortName(cli.midiInRegexPatterns(), inPortName);

    String outPortName = route.out();
    Map<String, Pattern> midiOutDefinitions = requireMidiOutDefinitionsContainsOutputPortName(cli.midiOutRegexPatterns(), outPortName);

    MidiDeviceManager midiDeviceManager = MidiDeviceManager.from(MidiDeviceReportFormatter.createDefaultInstance());

    MidiDeviceRecord midiInDevice = MidiDeviceManager.lookUpMidiDevice(isInputPortAndMatchesPortName(inPortName, midiInDefinitions), midiDeviceManager);
    MidiDeviceRecord midiOutDevice = MidiDeviceManager.lookUpMidiDevice(isOutputPortAndMatchesPortName(outPortName, midiOutDefinitions), midiDeviceManager);

    route(midiInDevice, midiOutDevice, midiDeviceManager, ps, inputStream);
  }

  private static Predicate<MidiDeviceRecord> isOutputPortAndMatchesPortName(String outPortName, Map<String, Pattern> midiOutDefinitions) {
    return and(MidiDeviceManager.isMidiDeviceForOutput(), matchesPortNameInDefinitions(outPortName, midiOutDefinitions));
  }

  private static Predicate<MidiDeviceRecord> isInputPortAndMatchesPortName(String inPortName, Map<String, Pattern> midiInDefinitions) {
    return and(isMidiDeviceForInput(), matchesPortNameInDefinitions(inPortName, midiInDefinitions));
  }

  public static void route(MidiDeviceRecord input, MidiDeviceRecord output, MidiDeviceManager deviceManager, PrintStream ps, InputStream inputStream) {
    try (MidiDevice outMidiDevice = deviceManager.openMidiDevice(output)) {
      try (MidiDevice inMidiDevice = deviceManager.openMidiDevice(input)) {
        try (Receiver r = outMidiDevice.getReceiver()) {
          try (Transmitter t = inMidiDevice.getTransmitter()) {
            t.setReceiver(r);
            ps.println("Now in MIDI patch-bay mode. Hit enter to quit.");
            //noinspection ResultOfMethodCallIgnored
            inputStream.read();
          } catch (IOException e) {
            ps.println("quitting due to an error.");
          } finally {
            ps.println("closing transmitter");
          }
        } catch (MidiUnavailableException e) {
          throw failedToRetrieveTransmitterFromMidiIn(e, inMidiDevice.getDeviceInfo());
        } finally {
          ps.println("closing receiver");
        }
      }
    }
  }


  private static Map<String, Pattern> requireMidiInDefinitionsContainsInputPortName(Map<String, Pattern> midiOutDefinitions, String inPortName) {
    if (!midiOutDefinitions.containsKey(inPortName)) {
      throw new CliException(composeErrMsg(format("MIDI-in port '%s' is specified, but it is not defined by '-I' option.", inPortName), "r", "--route"));
    }
    return midiOutDefinitions;
  }

  private static Map<String, Pattern> requireMidiOutDefinitionsContainsOutputPortName(Map<String, Pattern> midiOutDefinitions, String outPortName) {
    if (!midiOutDefinitions.containsKey(outPortName)) {
      throw new CliException(composeErrMsg(format("MIDI-out port '%s' is specified, but it is not defined by '-O' option.", outPortName), "r", "route"));
    }
    return midiOutDefinitions;
  }
}
