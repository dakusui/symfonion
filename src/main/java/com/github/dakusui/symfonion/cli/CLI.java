package com.github.dakusui.symfonion.cli;

import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.cli.subcommands.PresetSubcommand;
import com.github.dakusui.symfonion.core.exceptions.CLIException;
import com.github.dakusui.symfonion.scenarios.MidiDeviceScanner;
import com.github.dakusui.symfonion.scenarios.Symfonion;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.song.Keyword;
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

public class CLI {

  private Subcommand subcommand = PresetSubcommand.VERSION;
  private File source;
  private File                 sink     = new File("a.midi");
  private Route                route    = null;
  private Map<String, Pattern> midiins  = new HashMap<>();
  private Map<String, Pattern> midiouts = new HashMap<>();
  private final Symfonion symfonion;
  private Options   options;

  public CLI(String... args) throws ParseException, CLIException {
    this.init(args);
    this.symfonion = createSymfonion();
  }

  public Map<String, MidiDevice> prepareMidiOutDevices(PrintStream ps)
      throws CLIException {
    Map<String, Pattern> portDefinitions = this.getMidiOutDefinitions();
    String deviceType = "out";
    return prepareMidiDevices(ps, deviceType,
        portDefinitions);
  }

  @SuppressWarnings("UnusedDeclaration")
  protected Map<String, MidiDevice> prepareMidiInDevices(PrintStream ps)
      throws CLIException {
    Map<String, Pattern> portDefinitions = this.getMidiOutDefinitions();
    String deviceType = "in";
    return prepareMidiDevices(ps, deviceType, portDefinitions);
  }

  private Map<String, MidiDevice> prepareMidiDevices(PrintStream ps,
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
        String msg = this.composeErrMsg(String.format(
            "Failed to access MIDI-%s device:'%s'.", deviceType,
            matchedInfos[0].getName()), "O", null);
        throw new CLIException(msg, e);
      }
    }
    return devices;
  }

  private static MidiDevice.Info[] getInfos(String portname, MidiDeviceScanner scanner, Pattern regex) throws CLIException {
    MidiDevice.Info[] matchedInfos = scanner.getMatchedDevices();
    if (matchedInfos.length > 1) {
      String msg = String.format(
          "Device for port '%s' (regex:%s) wasn't identical (%d)", portname,
              regex, matchedInfos.length);
      throw new CLIException(msg);
    } else if (matchedInfos.length == 0) {
      String msg = String.format(
          "No matching device was found for port '%s' (regex:%s)", portname,
              regex);
      throw new CLIException(msg);
    }
    return matchedInfos;
  }

  public File composeOutputFile(String outfile, String portName) {
    if (portName == null || Keyword.$default.name().equals(portName)) {
      return new File(outfile);
    }
    File ret;
    int lastIndexOfDot = outfile.lastIndexOf('.');
    if (lastIndexOfDot == -1) {
      ret = new File(outfile + "." + portName);
    } else {
      ret = new File(outfile.substring(0, lastIndexOfDot) + "." + portName
          + outfile.substring(lastIndexOfDot));
    }
    return ret;
  }

  public Options getOptions() {
    return this.options;
  }

  public Symfonion getSymfonion() {
    return this.symfonion;
  }

  public void init(String... args) throws ParseException, CLIException {
    this.options = buildOptions();
    this.analyze(parseArgs(this.options, args));
  }

  protected Symfonion createSymfonion() {
    return new Symfonion(Context.ROOT);
  }

  CommandLine parseArgs(Options options, String[] args) throws ParseException {
    CommandLineParser parser = new GnuParser();

    return parser.parse(options, args);
  }

  private Options buildOptions() {
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
      option
          .setDescription("specify a file to which a compiled standard midi file is output.");
      options.addOption(option);
    }
    return options;
  }

  public void analyze(CommandLine cmd) throws CLIException {
    if (cmd.hasOption('O')) {
      String optionName = "O";
      this.midiouts = initializeMidiPorts(cmd, optionName);
    }
    if (cmd.hasOption('I')) {
      String optionName = "I";
      this.midiins = initializeMidiPorts(cmd, optionName);
    }
    if (cmd.hasOption('o')) {
      String sinkFilename = getSingleOptionValue(cmd, "o");
      if (sinkFilename == null) {
        String msg = composeErrMsg(
            "Output filename is required by this option.", "o", null);
        throw new CLIException(msg);
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
      String sourceFilename = getSingleOptionValue(cmd, "p");
      if (sourceFilename == null) {
        String msg = composeErrMsg(
            "Input filename is required by this option.", "p", null);
        throw new CLIException(msg);
      }
      this.source = new File(sourceFilename);
    } else if (cmd.hasOption("c") || cmd.hasOption("compile")) {
      this.subcommand = PresetSubcommand.COMPILE;
      String sourceFilename = getSingleOptionValue(cmd, "c");
      if (sourceFilename == null) {
        String msg = composeErrMsg(
            "Input filename is required by this option.", "c", null);
        throw new CLIException(msg);
      }
      this.source = new File(sourceFilename);
    } else if (cmd.hasOption("r") || cmd.hasOption("route")) {
      this.subcommand = PresetSubcommand.ROUTE;
      Properties props = cmd.getOptionProperties("r");
      if (props.size() != 1) {
        String msg = composeErrMsg(
            "Route information is not given or specified multiple times.", "r",
            "route");
        throw new CLIException(msg);
      }

      this.route = new Route(cmd.getOptionValues('r')[0],
          cmd.getOptionValues('r')[1]);
    } else {
      @SuppressWarnings("unchecked")
      List<String> leftovers = cmd.getArgList();
      if (leftovers.isEmpty()) {
        this.subcommand = PresetSubcommand.HELP;
      } else if (leftovers.size() == 1) {
        this.subcommand = PresetSubcommand.PLAY;
        this.source = new File(leftovers.getFirst());
      } else {
        String msg = composeErrMsg(
            String.format("Unrecognized arguments:%s",
                leftovers.subList(2, leftovers.size())), "-", null);
        throw new CLIException(msg);
      }
    }
  }

  private Map<String, Pattern> initializeMidiPorts(CommandLine cmd,
      String optionName) throws CLIException {
    Properties props = cmd.getOptionProperties(optionName);
    Map<String, Pattern> ret = new HashMap<>();
    for (Object key : props.keySet()) {
      String portname = key.toString();
      String p = props.getProperty(portname);
      try {
        Pattern portpattern = Pattern.compile(p);
        ret.put(portname, portpattern);
      } catch (PatternSyntaxException e) {
        String msg = String.format(
            "Regular expression '%s' for '%s' isn't valid.", portname, p);
        throw new CLIException(composeErrMsg(msg, optionName, null), e);
      }
    }
    return ret;
  }

  public String composeErrMsg(String msg, String optionName,
                              String longOptionName) {
    if (longOptionName != null) {
      return String.format("(-%s/--%s) %s", optionName, longOptionName, msg);
    } else {
      return String.format("(-%s) %s", optionName, msg);
    }
  }

  private String getSingleOptionValue(CommandLine cmd, String optionName)
      throws CLIException {
    String ret = cmd.getOptionValue(optionName);
    int sz = cmd.getOptionProperties(optionName).size();
    if (sz != 1) {
      String msg = composeErrMsg(String.format(
              "This option requires one and only one value. (found %d times)", sz),
          optionName, null);
      throw new CLIException(msg);
    }
    return ret;
  }

  public Subcommand getMode() {
    return this.subcommand;
  }

  public String license() {
    return """
            Copyright 2013 Hiroshi Ukai.

            Licensed under the Apache License, Version 2.0 (the "License");you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

            https://www.apache.org/licenses/LICENSE-2.0

            Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.""";
  }

  public String version() {
    String path = "/META-INF/maven/com.github.dakusui/symfonion/pom.properties";
    Properties props = new Properties();
    String version = "(N/A)";
    InputStream stream = getClass().getResourceAsStream(path);
    if (stream != null) {
      try {
        props.load(stream);
        version = props.getProperty("version");
      } catch (IOException ignored) {
      }
    }
    return version;
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

  public static int invoke(PrintStream stdout, PrintStream stderr,
      String... args) {
    int ret;
    try {
      CLI cli = new CLI(args);
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
      String[] args = new String[] { selectedFile };
      final JTextArea textArea = new JTextArea();
      JFrame frame = new JFrame("symfonion output");
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
      frame.add(textArea);
      frame.pack();
      frame.setSize(800,600);
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