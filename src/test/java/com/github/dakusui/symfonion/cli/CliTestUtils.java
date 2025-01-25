package com.github.dakusui.symfonion.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static com.github.dakusui.symfonion.cli.Cli.parseArgs;

/**
 * //@formatter:off
 * //@formatter:on
 */
public class CliTestUtils {
  static CommandLine createCommandLine(String... args) {
    try {
      return parseArgs(createOptions(), args);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  static Options createOptions() {
    return Cli.buildOptions();
  }
}
