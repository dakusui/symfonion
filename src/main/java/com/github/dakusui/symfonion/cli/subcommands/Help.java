package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import org.apache.commons.cli.HelpFormatter;

import java.io.PrintStream;

public class Help implements Subcommand {
    @Override
    public void invoke(Cli cli, PrintStream ps)  {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("SYNTAX", cli.getOptions());
    }
}
