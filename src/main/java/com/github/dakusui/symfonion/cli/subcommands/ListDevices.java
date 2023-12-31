package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceManager;

import javax.sound.midi.MidiSystem;
import java.io.PrintStream;
import java.util.Arrays;

import static com.github.dakusui.symfonion.utils.midi.MidiDeviceReportFormatter.createDefaultReportFormatter;
import static com.github.dakusui.symfonion.utils.midi.MidiUtils.createMidiDeviceManager;
import static java.util.stream.Collectors.joining;

public class ListDevices implements Subcommand {
  @Override
  public void invoke(Cli cli, PrintStream ps) {
    MidiDeviceManager reportComposer = createMidiDeviceManager();
    ps.println(reportComposer.composeReport(x -> false, createDefaultReportFormatter(), "Available MIDI devices").stream().collect(joining(String.format("%n"), "", String.format("%n"))));
  }
}
