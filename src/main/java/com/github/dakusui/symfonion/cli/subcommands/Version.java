package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.CLI;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;

import java.io.IOException;
import java.io.PrintStream;

public class Version implements Subcommand {
    @Override
    public void invoke(CLI cli, PrintStream ps) throws SymfonionException, IOException {
        ps.println("SyMFONION " + cli.version());
        ps.println(cli.license());
    }
}
