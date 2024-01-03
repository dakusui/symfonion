package com.github.dakusui.symfonion.cli;

import com.github.dakusui.symfonion.cli.subcommands.PresetSubcommand;
import com.github.dakusui.symfonion.core.Symfonion;
import com.github.dakusui.symfonion.exceptions.CliException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsg;
import static java.lang.String.format;

public record CliRecord(Subcommand subcommand, File source, File sink, MidiRouteRequest routeRequest,
                        Map<String, Pattern> midiInRegexPatterns, Map<String, Pattern> midiOutRegexPatterns,
                        Options options,
                        Symfonion symfonion) {

  public Cli getMode() {
    return null;
  }

  public static class Builder {
    private final String[] args;
    private File source;
    private File sink = new File("a.midi");
    private MidiRouteRequest routeRequest = null;
    private Map<String, Pattern> midiInRegexPatterns = new HashMap<>();
    private Map<String, Pattern> midiOutRegexPatterns = new HashMap<>();

    public Builder(String... args) {
      this.args = args;
    }

    public CliRecord build() throws ParseException {
      Options options1 = Cli.buildOptions();
      CommandLine cmd = Cli.parseArgs(options1, args);
      if (cmd.hasOption('O')) {
        this.midiOutRegexPatterns = Cli.parseSpecifiedOptionsInCommandLineAsPortNamePatterns(cmd, "O");
      }
      if (cmd.hasOption('I')) {
        this.midiInRegexPatterns = Cli.parseSpecifiedOptionsInCommandLineAsPortNamePatterns(cmd, "I");
      }
      if (cmd.hasOption('o')) {
        String sinkFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "o");
        if (sinkFilename == null) {
          throw new CliException(composeErrMsg("Output filename is required by this option.", "o"));
        }
        this.sink = new File(sinkFilename);
      }
      Subcommand subcommand;
      if (cmd.hasOption("V") || cmd.hasOption("version")) {
        subcommand = PresetSubcommand.VERSION;
      } else if (cmd.hasOption("h") || cmd.hasOption("help")) {
        subcommand = PresetSubcommand.HELP;
      } else if (cmd.hasOption("l") || cmd.hasOption("list")) {
        subcommand = PresetSubcommand.LIST;
      } else if (cmd.hasOption("p") || cmd.hasOption("play")) {
        subcommand = PresetSubcommand.PLAY;
        String sourceFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "p");
        if (sourceFilename == null) {
          throw new CliException(composeErrMsg("Input filename is required by this option.", "p"));
        }
        this.source = new File(sourceFilename);
      } else if (cmd.hasOption("c") || cmd.hasOption("compile")) {
        subcommand = PresetSubcommand.COMPILE;
        String sourceFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "c");
        if (sourceFilename == null) {
          throw new CliException(composeErrMsg("Input filename is required by this option.", "c"));
        }
        this.source = new File(sourceFilename);
      } else if (cmd.hasOption("r") || cmd.hasOption("route")) {
        subcommand = PresetSubcommand.ROUTE;
        Properties props = cmd.getOptionProperties("r");
        if (props.size() != 1) {
          throw new CliException(composeErrMsg("Route information is not given or specified multiple times.", "r", "route"));
        }

        this.routeRequest = new MidiRouteRequest(cmd.getOptionValues('r')[0], cmd.getOptionValues('r')[1]);
      } else {
        @SuppressWarnings("unchecked")
        List<String> leftovers = cmd.getArgList();
        if (leftovers.isEmpty()) {
          subcommand = PresetSubcommand.HELP;
        } else if (leftovers.size() == 1) {
          subcommand = PresetSubcommand.PLAY;
          this.source = new File(leftovers.getFirst());
        } else {
          throw new CliException(composeErrMsg(format("Unrecognized arguments:%s", leftovers.subList(2, leftovers.size())), "-"));
        }
      }
      return new CliRecord(subcommand, source, sink, routeRequest, midiInRegexPatterns, midiOutRegexPatterns, options1, Cli.createSymfonion());
    }
  }
}
