package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import org.apache.commons.cli.HelpFormatter;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * A subcommand that shows a help message of the CLI.
 */
public class Help implements Subcommand {
  /**
   * Creates an object of this class.
   */
  public Help() {
  }

  @Override
  public void invoke(Cli cli, PrintStream ps, InputStream inputStream) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("SYNTAX", cli.options());
  }
}
