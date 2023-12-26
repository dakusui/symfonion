package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.CLI;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.scenarios.MidiDeviceScanner;

import java.io.IOException;
import java.io.PrintStream;

public class ListDevices implements Subcommand {
    @Override
    public void invoke(CLI cli, PrintStream ps)  {
        MidiDeviceScanner.listAllDevices(System.out).scan();
    }
}
