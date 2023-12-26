package com.github.dakusui.symfonion.cli;

import com.github.dakusui.symfonion.core.exceptions.SymfonionException;

import java.io.IOException;
import java.io.PrintStream;

public interface Subcommand {
    void invoke(Cli cli, PrintStream ps) throws SymfonionException, IOException;
}
