package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceScanner;

import java.io.PrintStream;

public class ListDevices implements Subcommand {
    @Override
    public void invoke(Cli cli, PrintStream ps)  {
        MidiDeviceScanner.listAllDevices(System.out).scan();
    }
}
