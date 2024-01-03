package com.github.dakusui.symfonion.utils.midi;

import javax.sound.midi.MidiDevice;
import java.util.List;

import static java.util.Collections.emptyList;

public interface MidiDeviceReportFormatter {
  static MidiDeviceReportFormatter createDefaultInstance() {
    return new MidiDeviceReportFormatter() {
      @Override
      public List<String> header(MidiDevice.Info info, String title) {
        return List.of(
            "   " + title,
            String.format("   %s", MidiUtils.formatMidiDeviceInfo(info)),
            "-------------------------------------------------------------------------"
        );
      }


      @Override
      public String formatRecord(MidiDeviceRecord record) {
        return String.format("%1s%1s %s", record.in() ? "I" : "", record.out() ? "O" : "", MidiUtils.formatMidiDeviceInfo(record.info()));
      }

      @Override
      public List<String> footer() {
        return emptyList();
      }

      @Override
      public String formatResult(boolean matched, String formattedRecord) {
        return String.format("%1s%s", matched ? "*" : "", formattedRecord);
      }
    };
  }

  List<String> header(MidiDevice.Info info, String title);

  String formatRecord(MidiDeviceRecord record);

  List<String> footer();

  String formatResult(boolean matched, String formattedRecord);
}
