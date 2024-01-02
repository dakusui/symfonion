package com.github.dakusui.symfonion.utils.midi;

import com.github.dakusui.symfonion.exceptions.ExceptionThrower;
import com.github.dakusui.valid8j_pcond.forms.Printables;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.*;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.ContextKey.MIDI_DEVICE_INFO;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.ContextKey.MIDI_DEVICE_INFO_IO;
import static com.github.dakusui.symfonion.utils.Utils.onlyElement;

public class MidiDeviceManager {


  final List<MidiDeviceRecord> records;
  final MidiDeviceReportFormatter reportFormatter;


  public MidiDeviceManager(MidiDeviceReportFormatter formatter) {
    this.reportFormatter = formatter;
    this.records = new LinkedList<>();
  }

  public MidiDeviceRecord lookUp(Predicate<MidiDeviceRecord> whereClause) {
    return this.find(whereClause)
        .collect(onlyElement((e1, e2) -> multipleMidiDevices(e1, e2, whereClause)))
        .orElseThrow(() -> noSuchMidiDeviceWasFound(whereClause));
  }


  public static MidiDeviceManager from(MidiDeviceReportFormatter reportFormatter) {
    return from(reportFormatter, MidiUtils.streamMidiDeviceInfo());
  }

  public static MidiDeviceManager from(MidiDeviceReportFormatter reportFormatter, Stream<MidiDevice.Info> midiDeviceInfoStream) {
    MidiDeviceManager reportComposer = new MidiDeviceManager(reportFormatter);
    midiDeviceInfoStream.forEach(reportComposer::add);
    return reportComposer;
  }

  public static Predicate<MidiDeviceRecord> matchesPortNameInDefinitions(String inPortName, Map<String, Pattern> midiInDefinitions) {
    return midiDeviceInfoMatches(midiInDefinitions.get(inPortName));
  }

  private static Predicate<MidiDeviceRecord> midiDeviceInfoMatches(Pattern regexForDeviceName) {
    return printablePredicate(".info.name.matches[" + regexForDeviceName + "]", r -> regexForDeviceName.matcher(r.info().getName()).matches());
  }

  public static Predicate<MidiDeviceRecord> isMidiDeviceForInput() {
    return printablePredicate("isMidiDeviceForInput", r -> MidiUtils.isMidiDeviceForInput(r.info()));
  }

  public static Predicate<MidiDeviceRecord> isMidiDeviceForOutput() {
    return printablePredicate("isMidiDeviceForOutput", r -> MidiUtils.isMidiDeviceForOutput(r.info()));
  }

  private static <T> Predicate<T> printablePredicate(String name, Predicate<T> p) {
    return Printables.predicate(name, p);
  }

  public static MidiDeviceRecord lookUpMidiDevice(Predicate<MidiDeviceRecord> whereClause, MidiDeviceManager midiDeviceManager) {
    return midiDeviceManager.lookUp(whereClause);
  }

  public MidiDeviceManager add(MidiDevice.Info info) {
    return this.add(MidiDeviceRecord.fromMidiDeviceInfo(info));
  }

  public MidiDeviceManager add(MidiDeviceRecord record) {
    this.records.add(record);
    return this;
  }


  public Stream<MidiDeviceRecord> find(Predicate<MidiDeviceRecord> cond) {
    return streamRecords().filter(cond);
  }

  private Stream<MidiDeviceRecord> streamRecords() {
    return this.records.stream();
  }

  public MidiDevice openMidiDevice(MidiDeviceRecord deviceRecord) {
    try (ExceptionThrower.Context ignored = context($(MIDI_DEVICE_INFO, deviceRecord.info()), $(MIDI_DEVICE_INFO_IO, deviceRecord.io()))) {
      return openMidiDevice(deviceRecord.info());
    }
  }

  public MidiDevice openMidiDevice(MidiDevice.Info info) {
    try {
      MidiDevice ret = MidiSystem.getMidiDevice(info);
      ret.open();
      return ret;
    } catch (MidiUnavailableException e) {
      throw ExceptionThrower.failedToOpenMidiDevice(e);
    }
  }

  public List<String> composeReport(Predicate<MidiDeviceRecord> cond, final MidiDeviceReportFormatter reportFormatter, final String title) {
    MidiDevice.Info dummyInfoForHeader = new MidiDevice.Info("name", "vendor", "description", "version") {
    };
    return new ArrayList<>() {
      {
        this.addAll(reportFormatter.header(dummyInfoForHeader, title)
            .stream()
            .map(s -> reportFormatter.formatResult(false, s))
            .toList());
        this.addAll(MidiDeviceManager.this.records
            .stream()
            .map(r -> reportFormatter.formatResult(cond.test(r), reportFormatter.formatRecord(r)))
            .toList());
        this.addAll(reportFormatter.footer().stream().map(s -> reportFormatter.formatResult(false, s)).toList());
      }
    };
  }
}
