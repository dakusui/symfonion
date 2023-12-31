package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.MidiRouteRequest;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.exceptions.CliException;
import com.github.dakusui.symfonion.exceptions.ExceptionThrower;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceManager;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceRecord;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceReportFormatter;
import com.github.dakusui.symfonion.utils.midi.MidiUtils;
import com.github.dakusui.valid8j_pcond.forms.Printables;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsg;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.*;
import static com.github.dakusui.symfonion.utils.Utils.onlyElement;
import static com.github.dakusui.valid8j_pcond.forms.Predicates.and;
import static java.lang.String.format;

public class PatchBay implements Subcommand {
  @Override
  public void invoke(Cli cli, PrintStream ps, InputStream inputStream) throws SymfonionException, IOException {
    MidiRouteRequest route = cli.getRouteRequest();

    String inPortName = route.in();
    Map<String, Pattern> midiInDefinitions = requireMidiInDefinitionsContainsInputPortName(cli.getMidiInDefinitions(), inPortName);
    Pattern regexForMidiIn = midiInDefinitions.get(inPortName);

    String outPortName = route.out();
    Map<String, Pattern> midiOutDefinitions = requireMidiOutDefinitionsContainsOutputPortName(cli.getMidiOutDefinitions(), outPortName);
    Pattern regexForMidiOut = midiOutDefinitions.get(outPortName);

    MidiDeviceManager midiDeviceManager = MidiDeviceManager.from(MidiDeviceReportFormatter.createDefaultInstance());

    MidiDeviceRecord midiInDevice = findMidiDevice(and(isMidiDeviceForInput(), midiDeviceInfoMatches(regexForMidiIn)), midiDeviceManager);
    MidiDeviceRecord midiOutDevice = findMidiDevice(and(isMidiDeviceForOutput(), midiDeviceInfoMatches(regexForMidiOut)), midiDeviceManager);

    route(midiInDevice, midiOutDevice, midiDeviceManager, ps, inputStream);
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


  private static MidiDeviceRecord findMidiDevice(Predicate<MidiDeviceRecord> whereMidiDeviceIsInputAndMatchesRegex, MidiDeviceManager midiDeviceManager) {
    return midiDeviceManager.find(whereMidiDeviceIsInputAndMatchesRegex)
        .collect(onlyElement((e1, e2) -> multipleMidiDevices(e1, e2, whereMidiDeviceIsInputAndMatchesRegex)))
        .orElseThrow(() -> noSuchMidiDeviceWasFound(whereMidiDeviceIsInputAndMatchesRegex));
  }

  private static Predicate<MidiDeviceRecord> isMidiDeviceForInput() {
    return printablePredicate("isMidiDeviceForInput", r -> MidiUtils.isMidiDeviceForInput(r.info()));
  }

  private static Predicate<MidiDeviceRecord> isMidiDeviceForOutput() {
    return printablePredicate("isMidiDeviceForOutput", r -> MidiUtils.isMidiDeviceForOutput(r.info()));
  }

  private static Predicate<MidiDeviceRecord> midiDeviceInfoMatches(Pattern regexForDeviceName) {
    return printablePredicate(".info.name.matches[" + regexForDeviceName + "]", r -> regexForDeviceName.matcher(r.info().getName()).matches());
  }

  private static <T> Predicate<T> printablePredicate(String name, Predicate<T> p) {
    return Printables.predicate(name, p);
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
