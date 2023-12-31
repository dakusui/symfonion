package com.github.dakusui.symfonion.tests.utils.midi;

import com.github.dakusui.symfonion.utils.midi.MidiUtils;
import com.github.dakusui.valid8j_pcond.forms.Predicates;

import javax.sound.midi.MidiSystem;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dakusui.symfonion.utils.midi.MidiUtils.MidiDeviceRecord.Io.IN;
import static com.github.dakusui.symfonion.utils.midi.MidiUtils.MidiDeviceRecord.Io.OUT;

public class MidiDeviceScannerTest {
  public static void main(String... args) {
    Function<MidiUtils.MidiDeviceRecord, String> recordFormatter = (MidiUtils.MidiDeviceRecord r) -> String.format("%1s %1s%1s %s", r.matched() ? "*" : "", r.io() == IN ? "I" : "", r.io() == OUT ? "O" : "", MidiUtils.formatMidiDeviceInfo(r.info()));
    Function<String, String> titleFormatter = (String t) -> "    " + t;
    MidiUtils.MidiDeviceReportComposer reportComposer = MidiUtils.createMidiDeviceReportComposer(titleFormatter, recordFormatter, Predicates.alwaysTrue(), Arrays.stream(MidiSystem.getMidiDeviceInfo()));
    System.out.println(reportComposer.build().stream().collect(Collectors.joining(String.format("%n"), "", String.format("%n"))));
  }
}
