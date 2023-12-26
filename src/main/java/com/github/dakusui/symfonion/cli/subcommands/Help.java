package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.CLI;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import org.apache.commons.cli.HelpFormatter;

import java.io.IOException;
import java.io.PrintStream;

public class Help implements Subcommand {
    @Override
    public void invoke(CLI cli, PrintStream ps)  {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("SYNTAX", cli.getOptions());
    }
}
