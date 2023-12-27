package com.github.dakusui.symfonion.cli;

import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.cli.subcommands.PresetSubcommand;
import com.github.dakusui.symfonion.core.exceptions.CLIException;
import com.github.dakusui.symfonion.scenarios.MidiDeviceScanner;
import com.github.dakusui.symfonion.scenarios.Symfonion;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import org.apache.commons.cli.*;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsg;
import static java.lang.String.format;

public class Cli {
  
  private Subcommand subcommand = PresetSubcommand.VERSION;
  private File source;
  private File sink = new File("a.midi");
  private Route route = null;
  private Map<String, Pattern> midiins = new HashMap<>();
  private Map<String, Pattern> midiouts = new HashMap<>();
  private final Symfonion symfonion;
  private Options options;
  
  public Cli(String... args) throws ParseException, CLIException {
    this.init(args);
    this.symfonion = createSymfonion();
  }
  
  public Map<String, MidiDevice> prepareMidiOutDevices(PrintStream ps)
      throws CLIException {
    return prepareMidiDevices(ps, "out", this.getMidiOutDefinitions());
  }
  
  @SuppressWarnings("UnusedDeclaration")
  protected Map<String, MidiDevice> prepareMidiInDevices(PrintStream ps)
      throws CLIException {
    return prepareMidiDevices(ps, "in", this.getMidiOutDefinitions());
  }
  
  private static Map<String, MidiDevice> prepareMidiDevices(PrintStream ps,
                                                     String deviceType, Map<String, Pattern> portDefinitions)
      throws CLIException {
    Map<String, MidiDevice> devices = new HashMap<>();
    for (String portname : portDefinitions.keySet()) {
      Pattern regex = portDefinitions.get(portname);
      MidiDeviceScanner scanner = MidiDeviceScanner.chooseOutputDevices(ps,
          regex);
      scanner.scan();
      MidiDevice.Info[] matchedInfos = getInfos(portname, scanner, regex);
      try {
        devices.put(portname, MidiSystem.getMidiDevice(matchedInfos[0]));
      } catch (MidiUnavailableException e) {
        String msg = composeErrMsg(format("Failed to access MIDI-%s device:'%s'.", deviceType, matchedInfos[0].getName()), "O");
        throw new CLIException(msg, e);
      }
    }
    return devices;
  }
  
  private static MidiDevice.Info[] getInfos(String portname, MidiDeviceScanner scanner, Pattern regex) throws CLIException {
    MidiDevice.Info[] matchedInfos = scanner.getMatchedDevices();
    if (matchedInfos.length > 1) {
      String msg = format(
          "Device for port '%s' (regex:%s) wasn't identical (%d)", portname,
          regex, matchedInfos.length);
      throw new CLIException(msg);
    } else if (matchedInfos.length == 0) {
      String msg = format(
          "No matching device was found for port '%s' (regex:%s)", portname,
          regex);
      throw new CLIException(msg);
    }
    return matchedInfos;
  }
  
  public Options getOptions() {
    return this.options;
  }
  
  public Symfonion getSymfonion() {
    return this.symfonion;
  }
  
  public void init(String... args) throws ParseException, CLIException {
    this.options = buildOptions();
    this.analyzeCommandLine(parseArgs(this.options, args));
  }
  
  protected Symfonion createSymfonion() {
    return new Symfonion(Context.ROOT.createChild());
  }
  
  static CommandLine parseArgs(Options options, String[] args) throws ParseException {
    CommandLineParser parser = new GnuParser();
    
    return parser.parse(options, args);
  }
  
  /**
   * Returns an {@code Options} object which represents the specification of this CLI command.
   *
   * @return an {@code Options} object for this {@code CLI} class.
   */
  private static Options buildOptions() {
    // create Options object
    Options options = new Options();
    
    // //
    // Behavior options
    options.addOption("V", "version", false, "print the version information.");
    options.addOption("h", "help", false, "print the command line usage.");
    options.addOption("l", "list", false, "list the available midi devices.");
    options.addOption("p", "play", true, "play the specifiled file.");
    options.addOption("c", "compile", true,
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
    return options;
  }
  
  public void analyzeCommandLine(CommandLine cmd) throws CLIException {
    if (cmd.hasOption('O')) {
      this.midiouts = parseSpecifiedOptionsInCommandLineAsPortNamePatterns(cmd, "O");
    }
    if (cmd.hasOption('I')) {
      this.midiins = parseSpecifiedOptionsInCommandLineAsPortNamePatterns(cmd, "I");
    }
    if (cmd.hasOption('o')) {
      String sinkFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "o");
      if (sinkFilename == null) {
        throw new CLIException(composeErrMsg("Output filename is required by this option.", "o"));
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
        throw new CLIException(composeErrMsg("Input filename is required by this option.", "p"));
      }
      this.source = new File(sourceFilename);
    } else if (cmd.hasOption("c") || cmd.hasOption("compile")) {
      this.subcommand = PresetSubcommand.COMPILE;
      String sourceFilename = CliUtils.getSingleOptionValueFromCommandLine(cmd, "c");
      if (sourceFilename == null) {
        throw new CLIException(composeErrMsg("Input filename is required by this option.", "c"));
      }
      this.source = new File(sourceFilename);
    } else if (cmd.hasOption("r") || cmd.hasOption("route")) {
      this.subcommand = PresetSubcommand.ROUTE;
      Properties props = cmd.getOptionProperties("r");
      if (props.size() != 1) {
        throw new CLIException(composeErrMsg("Route information is not given or specified multiple times.", "r", "route"));
      }
      
      this.route = new Route(cmd.getOptionValues('r')[0], cmd.getOptionValues('r')[1]);
    } else {
      @SuppressWarnings("unchecked")
      List<String> leftovers = cmd.getArgList();
      if (leftovers.isEmpty()) {
        this.subcommand = PresetSubcommand.HELP;
      } else if (leftovers.size() == 1) {
        this.subcommand = PresetSubcommand.PLAY;
        this.source = new File(leftovers.getFirst());
      } else {
        throw new CLIException(composeErrMsg(format("Unrecognized arguments:%s", leftovers.subList(2, leftovers.size())), "-"));
      }
    }
  }
  
  private static Map<String, Pattern> parseSpecifiedOptionsInCommandLineAsPortNamePatterns(CommandLine cmd, String optionName) throws CLIException {
    Properties props = cmd.getOptionProperties(optionName);
    Map<String, Pattern> ret = new HashMap<>();
    for (Object key : props.keySet()) {
      String portname = key.toString();
      String p = props.getProperty(portname);
      try {
        Pattern portpattern = Pattern.compile(p);
        ret.put(portname, portpattern);
      } catch (PatternSyntaxException e) {
        throw new CLIException(composeErrMsg(
            format("Regular expression '%s' for '%s' isn't valid.", portname, p),
            optionName,
            null), e);
      }
    }
    return ret;
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
  
  public Map<String, Pattern> getMidiInDefinitions() {
    return this.midiins;
  }
  
  public Map<String, Pattern> getMidiOutDefinitions() {
    return this.midiouts;
  }
  
  public Route getRoute() {
    return this.route;
  }
  
  public static void main(String... args) {
    if (args.length == 0 && !GraphicsEnvironment.isHeadless()) {
      fallbackToSimpleGUI();
    } else {
      int exitCode = invoke(System.out, System.err, args);
      System.exit(exitCode);
    }
  }
  
  public static int invoke(PrintStream stdout, PrintStream stderr, String... args) {
    int ret;
    try {
      Cli cli = new Cli(args);
      cli.subcommand.invoke(cli, stdout);
      ret = 0;
    } catch (ParseException e) {
      printError(stderr, e);
      ret = 1;
    } catch (CLIException e) {
      printError(stderr, e);
      ret = 2;
    } catch (SymfonionException e) {
      printError(stderr, e);
      ret = 3;
    } catch (IOException e) {
      e.printStackTrace(stderr);
      ret = 4;
    }
    return ret;
  }
  
  private static void printError(PrintStream ps, Throwable t) {
    ps.printf("symfonion: %s%n", t.getMessage());
  }
  
  private static String filenameFromFileChooser() {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
    int result = chooser.showOpenDialog(new JFrame());
    if (result == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile().getAbsolutePath();
    }
    return null;
  }
  
  private static void fallbackToSimpleGUI() {
    String selectedFile = filenameFromFileChooser();
    if (selectedFile == null) {
      System.exit(0);
    } else {
      String[] args = new String[]{selectedFile};
      final JTextArea textArea = new JTextArea();
      JFrame frame = new JFrame("symfonion output");
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
}