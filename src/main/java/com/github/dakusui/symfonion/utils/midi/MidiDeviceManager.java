package com.github.dakusui.symfonion.utils.midi;

import com.github.dakusui.symfonion.exceptions.ExceptionThrower;
import com.github.dakusui.symfonion.utils.Utils;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MidiDeviceManager {


  final List<MidiDeviceRecord> records;
  final MidiDeviceReportFormatter reportFormatter;


  public MidiDeviceManager(MidiDeviceReportFormatter formatter) {
    this.reportFormatter = formatter;
    this.records = new LinkedList<>();
  }


  public static MidiDeviceManager from(MidiDeviceReportFormatter reportFormatter) {
    return from(reportFormatter, MidiUtils.streamMidiDeviceInfo());
  }

  public static MidiDeviceManager from(MidiDeviceReportFormatter reportFormatter, Stream<MidiDevice.Info> midiDeviceInfoStream) {
    MidiDeviceManager reportComposer = new MidiDeviceManager(reportFormatter);
    midiDeviceInfoStream.forEach(reportComposer::add);
    return reportComposer;
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
    return openMidiDevice(deviceRecord.info(), deviceRecord.io());
  }

  public MidiDevice openMidiDevice(MidiDevice.Info info, MidiDeviceRecord.Io io) {
    try {
      MidiDevice ret = MidiSystem.getMidiDevice(info);
      ret.open();
      return ret;
    } catch (MidiUnavailableException e) {
      throw ExceptionThrower.failedToOpenMidiDevice(e, info, io);
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
