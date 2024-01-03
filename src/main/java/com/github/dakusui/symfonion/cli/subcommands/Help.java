package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.CliRecord;
import com.github.dakusui.symfonion.cli.Subcommand;
import org.apache.commons.cli.HelpFormatter;

import java.io.InputStream;
import java.io.PrintStream;

public class Help implements Subcommand {
    @Override
    public void invoke(CliRecord cli, PrintStream ps, InputStream inputStream)  {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("SYNTAX", cli.options());
    }
}
