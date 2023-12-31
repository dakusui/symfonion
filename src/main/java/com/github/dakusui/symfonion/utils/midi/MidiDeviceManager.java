package com.github.dakusui.symfonion.utils.midi;

import com.github.dakusui.symfonion.utils.Utils;

import javax.sound.midi.MidiDevice;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MidiDeviceManager {


  final List<MidiDeviceRecord> records;
  final MidiDeviceReportFormatter reportFormatter;


  public MidiDeviceManager(MidiDeviceReportFormatter formatter) {
    this.reportFormatter = formatter;
    this.records = new LinkedList<>();
  }

  public MidiDeviceManager add(MidiDevice.Info info) {
    return this.add(MidiDeviceRecord.fromMidiDeviceInfo(info));
  }

  public MidiDeviceManager add(MidiDeviceRecord record) {
    this.records.add(record);
    return this;
  }

  public Optional<MidiDeviceRecord> lookUp(Pattern regex) {
    return streamRecords().filter(r -> regex.matcher(r.info().getName()).matches()).collect(Utils.onlyElement());
  }

  public Stream<MidiDeviceRecord> find(Predicate<MidiDeviceRecord> cond) {
    return streamRecords().filter(cond);
  }

  private Stream<MidiDeviceRecord> streamRecords() {
    return this.records.stream();
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
