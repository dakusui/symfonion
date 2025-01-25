package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import org.apache.commons.cli.HelpFormatter;

import java.io.InputStream;
import java.io.PrintStream;

/**
<<<<<<< Updated upstream
 * A subcommand that shows a help message of the CLI.
=======
 * A "help" subcommand
>>>>>>> Stashed changes
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
