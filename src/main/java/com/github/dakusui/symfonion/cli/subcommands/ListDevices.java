package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.CliRecord;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceManager;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceReportFormatter;

import java.io.InputStream;
import java.io.PrintStream;

import static com.github.dakusui.symfonion.utils.midi.MidiDeviceReportFormatter.createDefaultInstance;
import static java.util.stream.Collectors.joining;

public class ListDevices implements Subcommand {
  @Override
  public void invoke(CliRecord cli, PrintStream ps, InputStream inputStream) {
    MidiDeviceManager reportComposer = MidiDeviceManager.from(MidiDeviceReportFormatter.createDefaultInstance());
    ps.println(reportComposer.composeReport(x -> false, createDefaultInstance(), "Available MIDI devices").stream().collect(joining(String.format("%n"), "", String.format("%n"))));
  }
}
