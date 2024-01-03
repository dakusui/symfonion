package com.github.dakusui.symfonion.cli;

import com.github.dakusui.symfonion.cli.subcommands.PresetSubcommand;
import com.github.dakusui.symfonion.exceptions.CliException;
import com.github.dakusui.symfonion.core.Symfonion;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsg;
import static java.lang.String.format;

public class Cli {

  private Subcommand subcommand = PresetSubcommand.VERSION;
  private File source;
  private File sink = new File("a.midi");
  private MidiRouteRequest routeRequest = null;
  private Map<String, Pattern> midiInRegexPatterns = new HashMap<>();
  private Map<String, Pattern> midiOutRegexPatterns = new HashMap<>();
  private final Symfonion symfonion;
  private Options options;

  public Cli(String... args) throws ParseException, CliException {
    this.init(args);
    this.symfonion = CliRecord.createSymfonion();
  }

  public Options getOptions() {
    return this.options;
  }

  public Symfonion getSymfonion() {
    return this.symfonion;
  }

  public void init(String... args) throws ParseException, CliException {
    this.options = CliRecord.buildOptions();
    this.analyzeCommandLine(CliRecord.parseArgs(this.options, args));
  }

  public void analyzeCommandLine(CommandLine cmd) throws CliException {
    if (cmd.hasOption('O')) {
      this.midiOutRegexPatterns = CliRecord.parseSpecifiedOptionsInCommandLineAsPortNamePatterns(cmd, "O");
    }
    if (cmd.hasOption('I')) {
      this.midiInRegexPatterns = CliRecord.parseSpecifiedOptionsInCommandLineAsPortNamePatterns(cmd, "I");
    }
    if (cmd.hasOption('o')) {
      String sinkFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "o");
      if (sinkFilename == null) {
        throw new CliException(composeErrMsg("Output filename is required by this option.", "o"));
      }
      this.sink = new File(sinkFilename);
    }
    if (cmd.hasOption("V") || cmd.hasOption("version")) {
      this.subcommand = PresetSubcommand.VERSION;
    } else if (cmd.hasOption("h") || cmd.hasOption("help")) {
      this.subcommand = PresetSubcommand.HELP;
    } else if (cmd.hasOption("l") || cmd.hasOption("list")) {
      this.subcommand = PresetSubcommand.LIST;
    } else if (cmd.hasOption("p") || cmd.hasOption("play")) {
      this.subcommand = PresetSubcommand.PLAY;
      String sourceFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "p");
      if (sourceFilename == null) {
        throw new CliException(composeErrMsg("Input filename is required by this option.", "p"));
      }
      this.source = new File(sourceFilename);
    } else if (cmd.hasOption("c") || cmd.hasOption("compile")) {
      this.subcommand = PresetSubcommand.COMPILE;
      String sourceFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "c");
      if (sourceFilename == null) {
        throw new CliException(composeErrMsg("Input filename is required by this option.", "c"));
      }
      this.source = new File(sourceFilename);
    } else if (cmd.hasOption("r") || cmd.hasOption("route")) {
      this.subcommand = PresetSubcommand.ROUTE;
      Properties props = cmd.getOptionProperties("r");
      if (props.size() != 1) {
        throw new CliException(composeErrMsg("Route information is not given or specified multiple times.", "r", "route"));
      }

      this.routeRequest = new MidiRouteRequest(cmd.getOptionValues('r')[0], cmd.getOptionValues('r')[1]);
    } else {
      @SuppressWarnings("unchecked")
      List<String> leftovers = cmd.getArgList();
      if (leftovers.isEmpty()) {
        this.subcommand = PresetSubcommand.HELP;
      } else if (leftovers.size() == 1) {
        this.subcommand = PresetSubcommand.PLAY;
        this.source = new File(leftovers.getFirst());
      } else {
        throw new CliException(composeErrMsg(format("Unrecognized arguments:%s", leftovers.subList(2, leftovers.size())), "-"));
      }
    }
  }

  public Subcommand getMode() {
    return this.subcommand;
  }

  public File getSourceFile() {
    return this.source;
  }

  public File getSinkFile() {
    return this.sink;
  }

  /**
   * Returns a map that defines MIDI-in port names.
   * A key in the returned map is a port name used in a symfonion song file.
   * The value associated with it is a regular expression that should specify a MIDI device.
   * The regular expression should be defined so that it matches one and only one MIDI-in device available in the system.
   *
   * @return A map that defines MIDI-in port names.
   */
  public Map<String, Pattern> getMidiInDefinitions() {
    return this.midiInRegexPatterns;
  }

  /**
   * Returns a map that defines MIDI-out port names.
   * A key in the returned map is a port name used in a symfonion song file.
   * The value associated with it is a regular expression that should specify a MIDI device.
   * The regular expression should be defined so that it matches one and only one MIDI-out device available in the system.
   *
   * @return A map that defines MIDI-out port names.
   */
  public Map<String, Pattern> getMidiOutDefinitions() {
    return this.midiOutRegexPatterns;
  }

  public MidiRouteRequest getRouteRequest() {
    return this.routeRequest;
  }

}