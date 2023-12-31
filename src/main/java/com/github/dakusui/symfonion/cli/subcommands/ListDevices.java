package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.utils.midi.MidiUtils;
import com.github.dakusui.valid8j_pcond.forms.Predicates;

import javax.sound.midi.MidiSystem;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.Function;

import static com.github.dakusui.symfonion.utils.midi.MidiUtils.MidiDeviceRecord.Io.IN;
import static com.github.dakusui.symfonion.utils.midi.MidiUtils.MidiDeviceRecord.Io.OUT;
import static com.github.dakusui.symfonion.utils.midi.MidiUtils.createMidiDeviceReportComposer;
import static com.github.dakusui.symfonion.utils.midi.MidiUtils.formatMidiDeviceInfo;
import static java.util.stream.Collectors.joining;

public class ListDevices implements Subcommand {
    @Override
    public void invoke(Cli cli, PrintStream ps)  {
        Function<MidiUtils.MidiDeviceRecord, String> recordFormatter = (MidiUtils.MidiDeviceRecord r) -> String.format("%1s %1s%1s %s", r.matched() ? "*" : "", r.io() == IN ? "I" : "", r.io() == OUT ? "O" : "", formatMidiDeviceInfo(r.info()));
        Function<String, String> titleFormatter = (String t) -> "    " + t;
        MidiUtils.MidiDeviceReportComposer reportComposer = createMidiDeviceReportComposer(titleFormatter, recordFormatter, Predicates.alwaysTrue(), Arrays.stream(MidiSystem.getMidiDeviceInfo()));
        ps.println(reportComposer.build().stream().collect(joining(String.format("%n"), "", String.format("%n"))));
    }
}
