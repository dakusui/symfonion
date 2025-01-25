package com.github.dakusui.symfonion.cli;

import com.github.dakusui.symfonion.cli.subcommands.PresetSubcommand;
import com.github.dakusui.symfonion.compat.exceptions.CliException;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.core.Symfonion;
import com.github.dakusui.symfonion.song.Bar;
import com.github.dakusui.symfonion.song.Measure;
import com.github.valid8j.pcond.forms.Predicates;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.lang.String.format;

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

  static Map<String, Pattern> parseSpecifiedOptionsInCommandLineAsPortNamePatterns(CommandLine cmd, String optionName) throws CliException {
    Properties           props = cmd.getOptionProperties(optionName);
    Map<String, Pattern> ret   = new HashMap<>();
    for (Object key : props.keySet()) {
      String portName = key.toString();
      String p        = props.getProperty(portName);
      try {
        Pattern portpattern = Pattern.compile(p);
        ret.put(portName, portpattern);
      } catch (PatternSyntaxException e) {
        throw new CliException(CliUtils.composeErrMsgForOption(format("Regular expression '%s' for '%s' isn't valid.", portName, p),
                                                               optionName, null), e);
      }
    }
    return ret;
  }

  public static int invoke(PrintStream stdout, PrintStream stderr, String... args) {
    int ret;
    try {
      Cli cli = new Builder(args).build();
      cli.subcommand().invoke(cli, stdout, System.in);
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

  private static void printError(PrintStream ps, Throwable t) {
    ps.printf("symfonion: %s%n", t.getMessage());
  }

  public static void main(String... args) {
    if (args.length == 0 && !GraphicsEnvironment.isHeadless()) {
      fallbackToSimpleGUI();
    } else {
      int exitCode = invoke(System.out, System.err, args);
      System.exit(exitCode);
    }
  }

  static void fallbackToSimpleGUI() {
    String selectedFile = filenameFromFileChooser();
    if (selectedFile != null) {
      String[]        args     = new String[]{selectedFile};
      final JTextArea textArea = new JTextArea();
      JFrame          frame    = new JFrame("symfonion output");
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
      frame.add(textArea);
      frame.pack();
      frame.setSize(800, 600);
      frame.setVisible(true);
      PrintStream ps = new PrintStream(new OutputStream() {
        private final StringBuilder sb = new StringBuilder();

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        @Override
        public void write(int b) {
          if (b == '\r')
            return;

          if (b == '\n') {
            final String text = sb + "\n";
            SwingUtilities.invokeLater(() -> textArea.append(text));
            sb.setLength(0);
            return;
          }

          sb.append((char) b);
        }

      });
      System.setOut(ps);
      System.setErr(ps);
      invoke(System.out, System.err, args);
    }
  }

  static String filenameFromFileChooser() {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
    int result = chooser.showOpenDialog(new JFrame());
    if (result == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile().getAbsolutePath();
    }
    return null;
  }

  public static class Builder {
    private final String[]             args;
    private       File                 source;
    private       File                 sink                 = new File("target/a.midi");
    private       MidiRouteRequest     routeRequest         = null;
    private       Map<String, Pattern> midiInRegexPatterns  = new HashMap<>();
    private       Map<String, Pattern> midiOutRegexPatterns = new HashMap<>();

    public Builder(String... args) {
      this.args = args;
    }

    public Cli build() throws ParseException {
      Options     options1 = buildOptions();
      CommandLine cmd      = parseArgs(options1, args);
      if (cmd.hasOption('O')) {
        this.midiOutRegexPatterns = parseSpecifiedOptionsInCommandLineAsPortNamePatterns(cmd, "O");
      }
      if (cmd.hasOption('I')) {
        this.midiInRegexPatterns = parseSpecifiedOptionsInCommandLineAsPortNamePatterns(cmd, "I");
      }
      if (cmd.hasOption('o')) {
        String sinkFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "o");
        if (sinkFilename == null) {
          throw new CliException(CliUtils.composeErrMsgForShortOption("Output filename is required by this option.", "o"));
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
          throw new CliException(CliUtils.composeErrMsgForShortOption("Input filename is required by this option.", "p"));
        }
        this.source = new File(sourceFilename);
      } else if (cmd.hasOption("q") || cmd.hasOption("play-song")) {
        subcommand = PresetSubcommand.PLAY_SONG;
        String sourceFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "q");
        if (sourceFilename == null) {
          throw new CliException(CliUtils.composeErrMsgForShortOption("Input filename is required by this option.", "q"));
        }
        this.source = new File(sourceFilename);
      } else if (cmd.hasOption("c") || cmd.hasOption("compile")) {
        subcommand = PresetSubcommand.COMPILE;
        String sourceFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "c");
        if (sourceFilename == null) {
          throw new CliException(CliUtils.composeErrMsgForShortOption("Input filename is required by this option.", "c"));
        }
        this.source = new File(sourceFilename);
      } else if (cmd.hasOption("x") || cmd.hasOption("compile-song")) {
        subcommand = PresetSubcommand.COMPILE_SONG;
        String sourceFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "x");
        if (sourceFilename == null) {
          throw new CliException(CliUtils.composeErrMsgForShortOption("Input filename is required by this option.", "c"));
        }
        this.source = new File(sourceFilename);
      } else if (cmd.hasOption("r") || cmd.hasOption("route")) {
        subcommand = PresetSubcommand.ROUTE;
        Properties props = cmd.getOptionProperties("r");
        if (props.size() != 1) {
          throw new CliException(CliUtils.composeErrMsgForOption("Route information is not given or specified multiple times.", "r", "route"));
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
      return new Cli(subcommand, source, sink, routeRequest, midiInRegexPatterns, midiOutRegexPatterns, barFilter, measureFilter, partFilter, options1, createSymfonion());
    }
  }
}
