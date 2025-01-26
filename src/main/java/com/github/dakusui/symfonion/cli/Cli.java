package com.github.dakusui.symfonion.cli;

import com.github.dakusui.symfonion.cli.subcommands.PresetSubcommand;
import com.github.dakusui.symfonion.compat.exceptions.CliException;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.core.Symfonion;
import com.github.dakusui.symfonion.song.Bar;
import com.github.dakusui.symfonion.song.Measure;
import com.github.valid8j.pcond.forms.Predicates;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsgForOption;
import static com.github.dakusui.symfonion.cli.CliUtils.getSingleOptionValueFromCommandLine;
import static java.lang.String.format;

/**
 * A class that models a single CLI invocation.
 *
 * @param subcommand           A sub command to be executed by this object.
 * @param source               A source file to be processed.
 * @param sink                 A sink (output) file to which processed result should be written.
 * @param routeRequest         An object to specify routing of MIDI signals.
 * @param midiInRegexPatterns  An object that maps logical midi input port name to a device name.
 * @param midiOutRegexPatterns An object that maps logical midi output port name to a device name.
 * @param barFilter            A predicate that filters musical bars.
 *                             Used with `-c` and `-p` options.
 * @param measureFilter        A predicate that filters musical measures.
 *                             Used with `-x` and `-q` options.
 * @param partFilter           A predicate that filters parts.
 * @param options              An object that models command line options.
 * @param symfonion            This application's facade object.
 */
public record Cli(
    Subcommand subcommand,
    File source,
    File sink,
    MidiRouteRequest routeRequest,
    /*
     * Returns a map that defines MIDI-in port names.
     * A key in the returned map is a port name used in a symfonion song file.
     * The value associated with it is a regular expression that should specify a MIDI device.
     * The regular expression should be defined so that it matches one and only one MIDI-in device available in the system.
     */
    Map<String, Pattern> midiInRegexPatterns,
    /*
     * Returns a map that defines MIDI-out port names.
     * A key in the returned map is a port name used in a symfonion song file.
     * The value associated with it is a regular expression that should specify a MIDI device.
     * The regular expression should be defined so that it matches one and only one MIDI-out device available in the system.
     */
    Map<String, Pattern> midiOutRegexPatterns,
    Predicate<Bar> barFilter,
    Predicate<Measure> measureFilter,
    Predicate<String> partFilter,
    Options options,
    Symfonion symfonion) {

  /**
   * Invokes this object.
   *
   * @param stdout A print stream to which the output of the execution goes.
   * @param in An input stream from which the executed subcommand reads data.
   * @throws IOException A failure detected during execution.
   */
  public void invoke(PrintStream stdout, InputStream in) throws IOException {
    this.subcommand().invoke(this, stdout, in);
  }

  /**
   * Invokes the **SyMFONION** application with given command line arguments.
   *
   * @param stdout A print stream to which stdout data are printed.
   * @param stderr A print stream to which stderr data are printed.
   * @param args Command line arguments.
   * @return An exit code.
   */
  public static int invoke(PrintStream stdout, PrintStream stderr, String... args) {
    int ret;
    try {
      Cli.cli(args).$().invoke(stdout, System.in);
      ret = 0;
    } catch (ParseException e) {
      printError(stderr, e);
      ret = 1;
    } catch (CliException e) {
      printError(stderr, e);
      ret = 2;
    } catch (SymfonionException e) {
      printError(stderr, e);
      ret = 3;
    } catch (IOException e) {
      e.printStackTrace(stderr);
      ret = 4;
    } catch (Exception e) {
      e.printStackTrace(stderr);
      ret = 5;
    }
    return ret;
  }

  /**
   * A synonym for `new Builder(String... args)`.
   * Prefer this over directly calling `new Builder(String... args)` for readability's sake.
   *
   * @param args Commandline arguments
   * @return A new `Cli.Builder` object
   */
  public static Builder cli(String... args) {
    return new Builder(args);
  }

  /**
   * The application's entry point.
   *
   * @param args Given command line arguments.
   */
  public static void main(String... args) {
    System.exit(invoke(System.out, System.err, args));
  }

  /**
   * Returns an {@code Options} object which represents the specification of this CLI command.
   *
   * @return an {@code Options} object for this {@code CLI} class.
   */
  static Options buildOptions() {
    // create `Options` object
    Options options = new Options();

    // //
    // Behavior options
    options.addOption("V", "version", false, "print the version information.");
    options.addOption("h", "help", false, "print the command line usage.");
    options.addOption("l", "list", false, "list the available midi devices.");
    options.addOption("p", "play", true, "play the specified file using old syntax. deprecated.");
    options.addOption("q", "play-song", true, "play the specified file.");
    options.addOption("c", "compile", true,
                      "compile the specified file to a standard midi file using old syntax. deprecated.");
    options.addOption("x", "compile-song", true,
                      "compile the specified file to a standard midi file.");
    {
      Option option = OptionBuilder.create("r");
      option.setLongOpt("route");
      option.setValueSeparator('=');
      option.setArgs(2);
      option.setDescription("run a midi patch bay.");
      options.addOption(option);
    }

    // //
    // I/O options
    {
      Option option = OptionBuilder.create("O");
      option.setValueSeparator('=');
      option.setArgs(2);
      option.setDescription("specify midi out port.");
      options.addOption(option);
    }
    {
      Option option = OptionBuilder.create("I");
      option.setValueSeparator('=');
      option.setArgs(2);
      option.setDescription("specify midi in port.");
      options.addOption(option);
    }
    {
      Option option = OptionBuilder.create("o");
      option.setArgs(1);
      option.setDescription("specify a file to which a compiled standard midi file is output.");
      options.addOption(option);
    }
    // bar filter
    {
      OptionBuilder.withLongOpt("bars");
      Option option = OptionBuilder.create();
      option.setArgs(1);
      option.setDescription("specify a filter for bars. bars any of whose labels matches with a given regex will be processed.");
      options.addOption(option);
    }
    // part filter
    {
      OptionBuilder.withLongOpt("parts");
      Option option = OptionBuilder.create();
      option.setArgs(1);
      option.setDescription("specify a filter for parts. parts whose name matches with a given regex will be processed.");
      options.addOption(option);
    }
    return options;
  }

  static Symfonion createSymfonion() {
    return new Symfonion();
  }

  static CommandLine parseArgs(Options options, String[] args) throws ParseException {
    CommandLineParser parser = new GnuParser();

    return parser.parse(options, args);
  }

  static Map<String, Pattern> parseSpecifiedOptionAsPortNamePatterns(CommandLine cmd, String optionName) throws CliException {
    Properties           props = cmd.getOptionProperties(optionName);
    Map<String, Pattern> ret   = new HashMap<>();
    for (Object key : props.keySet()) {
      String portName = key.toString();
      String p        = props.getProperty(portName);
      try {
        ret.put(portName, Pattern.compile(p));
      } catch (PatternSyntaxException e) {
        throw new CliException(composeErrMsgForOption(format("Regular expression '%s' for '%s' isn't valid.", portName, p), optionName, null), e);
      }
    }
    return ret;
  }

  private static void printError(PrintStream ps, Throwable t) {
    ps.printf("symfonion: %s%n", t.getMessage());
  }

  /*
   * A builder of {@code Cli} class.
   *
   * It is encouraged to use {@link Cli#cli(String...)} and {@link Builder#$()} method to create an instance of this `Builder`
   * class and the product class `Cli`.
   *
   * That is,
   *
   * //@formatter:off
   * [source, java]
   * ----
   * class Example {
   *   void example() {
   *     Cli cli = cli("-x", "song.json").chain()
   *                                     .some()
   *                                     .methods()
   *                                     .$()
   *     cli.invoke();
   *   }
   * }
   * ----
   * //@formatter:on
   */
  public static class Builder {
    private final String[]             args;
    private       File                 source;
    private       File                 sink                 = new File("target/a.midi");
    private       MidiRouteRequest     routeRequest         = null;
    private       Map<String, Pattern> midiInRegexPatterns  = new HashMap<>();
    private       Map<String, Pattern> midiOutRegexPatterns = new HashMap<>();

    /**
     * Creates an object of this class.
     * @param args Command line arguments.
     */
    public Builder(String... args) {
      this.args = args;
    }

    /*
     * A synonym for {@link Builder#build()}.
     *
     * Prefer this method over `build` for readability's sake.
     *
     * @return A new `Cli` object.
     * @throws ParseException Failed to parse commandline arguments based on the specification of this application.
     * @see Builder#build()
     * @see Builder#Builder(String... args)
     */
    public Cli $() throws ParseException {
      return build();
    }

    /**
     * Builds a `Cli` object.
     *
     * @return A new `Cli` object built from values given to this builder.
     * @throws ParseException Failed to parse the given arguments.
     *
     * @see Cli
     */
    public Cli build() throws ParseException {
      Options     options = buildOptions();
      CommandLine cmd     = parseArgs(options, args);
      if (cmd.hasOption('O')) {
        this.midiOutRegexPatterns = parseSpecifiedOptionAsPortNamePatterns(cmd, "O");
      }
      if (cmd.hasOption('I')) {
        this.midiInRegexPatterns = parseSpecifiedOptionAsPortNamePatterns(cmd, "I");
      }
      if (cmd.hasOption('o')) {
        this.sink = new File(getSingleOptionValueFromCommandLine(cmd, "o"));
      }
      Subcommand subcommand;
      if (cmd.hasOption("V") || cmd.hasOption("version")) {
        subcommand = PresetSubcommand.VERSION;
      } else if (cmd.hasOption("h") || cmd.hasOption("help")) {
        subcommand = PresetSubcommand.HELP;
      } else if (cmd.hasOption("l") || cmd.hasOption("list")) {
        subcommand = PresetSubcommand.LIST;
      } else if (cmd.hasOption("p") || cmd.hasOption("play")) {
        subcommand  = PresetSubcommand.PLAY;
        this.source = new File(getSingleOptionValueFromCommandLine(cmd, "p"));
      } else if (cmd.hasOption("q") || cmd.hasOption("play-song")) {
        subcommand  = PresetSubcommand.PLAY_SONG;
        this.source = new File(getSingleOptionValueFromCommandLine(cmd, "q"));
      } else if (cmd.hasOption("c") || cmd.hasOption("compile")) {
        subcommand  = PresetSubcommand.COMPILE;
        this.source = new File(getSingleOptionValueFromCommandLine(cmd, "c"));
      } else if (cmd.hasOption("x") || cmd.hasOption("compile-song")) {
        subcommand  = PresetSubcommand.COMPILE_SONG;
        this.source = new File(getSingleOptionValueFromCommandLine(cmd, "x"));
      } else if (cmd.hasOption("r") || cmd.hasOption("route")) {
        subcommand = PresetSubcommand.ROUTE;
        Properties props = cmd.getOptionProperties("r");
        if (props.size() != 1) {
          throw new CliException(composeErrMsgForOption("Route information is not given or specified multiple times.", "r", "route"));
        }
        this.routeRequest = new MidiRouteRequest(cmd.getOptionValues('r')[0], cmd.getOptionValues('r')[1]);
      } else {
        @SuppressWarnings("unchecked")
        List<String> leftovers = cmd.getArgList();
        if (leftovers.isEmpty()) {
          subcommand = PresetSubcommand.HELP;
        } else if (leftovers.size() == 1) {
          subcommand  = PresetSubcommand.PLAY;
          this.source = new File(leftovers.getFirst());
        } else {
          throw new CliException(CliUtils.composeErrMsgForShortOption(format("Unrecognized arguments:%s", leftovers.subList(2, leftovers.size())), "-"));
        }
      }
      Predicate<Bar> barFilter = Predicates.alwaysTrue();
      if (cmd.hasOption("bars") && !Objects.equals("*", cmd.getOptionValue("bars")))
        barFilter = bar -> bar.labels().stream().anyMatch(l -> l.matches(cmd.getOptionValue("bars")));
      Predicate<Measure> measureFilter = Predicates.alwaysTrue();
      if (cmd.hasOption("ms") && !Objects.equals("*", cmd.getOptionValue("ms")))
        measureFilter = measure -> measure.labels().stream().anyMatch(l -> l.matches(cmd.getOptionValue("ms")));
      Predicate<String> partFilter = Predicates.alwaysTrue();
      if (cmd.hasOption("parts") && !Objects.equals("*", cmd.getOptionValue("parts")))
        partFilter = partName -> partName != null && partName.matches(cmd.getOptionValue("parts"));
      return new Cli(subcommand, source, sink, routeRequest, midiInRegexPatterns, midiOutRegexPatterns, barFilter, measureFilter, partFilter, options, createSymfonion());
    }
  }
}
