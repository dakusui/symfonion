package com.github.dakusui.symfonion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Transmitter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.song.Keyword;
import com.github.dakusui.symfonion.song.Song;

public class CLI {
    static enum Mode {
        VERSION {
			@Override
			public void invoke(CLI cli, PrintStream ps) {
				ps.println("SyMFONION " + cli.version());
				ps.println(cli.license());
			}
		},
        HELP {
			@Override
			public void invoke(CLI cli, PrintStream ps) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("SYNTAX", cli.getOptions());
			}
		},
        LIST {
			@Override
			public void invoke(CLI cli, PrintStream ps) {
				MidiDeviceScanner.listAllDevices(System.out).scan();
			}
		},
        PLAY {
			@Override
			public void invoke(CLI cli, PrintStream ps) throws SymfonionException, IOException {
				Symfonion symfonion = cli.getSymfonion();

				Song song = symfonion.load(cli.getSourceFile().getAbsolutePath());
				Map<String, Sequence> sequences = symfonion.compile(song); 
				ps.println();
				Map<String, MidiDevice> devices = cli.prepareMidiOutDevices(ps);
				ps.println();
				symfonion.play(devices, sequences);
			}
		},
        COMPILE {
			@Override
			public void invoke(CLI cli, PrintStream ps) throws SymfonionException, IOException {
				Symfonion symfonion = cli.getSymfonion();
				Song song = symfonion.load(cli.getSourceFile().getAbsolutePath());
				Map<String, Sequence> sequences = symfonion.compile(song); 

				for (String portName : sequences.keySet()) {
					Sequence seq = sequences.get(portName);
					String outfile = cli.getSinkFile().getAbsolutePath();
					File outputFile = cli.composeOutputFile(outfile, portName);
					MidiSystem.write(seq, 1, outputFile);
				}
			}
		},
        ROUTE {
			@Override
			public void invoke(CLI cli, PrintStream ps) throws CLIException {
				Route route = cli.getRoute();
				String inPortName = route.in;
				String outPortName = route.out;
				
				Map<String, Pattern> inDefs = cli.getMidiInDefinitions();
				
				if (!inDefs.containsKey(inPortName)) {
					String msg = cli.composeErrMsg(
							String.format(
									"MIDI-in port '%s' is specified, but it is not defined by '-I' option.",
									inPortName
									), 
									"r", 
									"--route"
							);
					throw new CLIException(msg);
				}
				Pattern inRegex = inDefs.get(inPortName);
				MidiDeviceScanner inScanner = MidiDeviceScanner.chooseInputDevices(System.out, inRegex);
				inScanner.scan();
				MidiDevice.Info[] matchedInDevices = inScanner.getMatchedDevices();
				if (matchedInDevices.length == 0 || matchedInDevices.length > 1) {
					String msg = cli.composeErrMsg(
							String.format("MIDI-in device for %s(%s) is not found or found more than one.", inPortName, inRegex), 
							"I", 
							null
							);
					throw new CLIException(msg);
				}

				ps.println();
				
				Map<String, Pattern> outDefs = cli.getMidiOutDefinitions();
				if (!outDefs.containsKey(outPortName)) {
					String msg = cli.composeErrMsg(
							String.format(
									"MIDI-out port '%s' is specified, but it is not defined by '-O' option.",
									inPortName
									), 
									"r", 
									"route"
							);
					throw new CLIException(msg);
				}
				Pattern outRegex = outDefs.get(outPortName);
				MidiDeviceScanner outScanner = MidiDeviceScanner.chooseOutputDevices(System.out, outRegex);
				outScanner.scan();
				MidiDevice.Info[] matchedOutDevices = outScanner.getMatchedDevices();
				if (matchedOutDevices.length == 0 || matchedOutDevices.length > 1) {
					String msg = cli.composeErrMsg(
							String.format("MIDI-out device for %s(%s) is not found or found more than one.", outPortName, outRegex), 
							"I", 
							null
							);
					throw new CLIException(msg);
				}
				ps.println();
				patchbay(matchedInDevices[0], matchedOutDevices[0]);
			}
				
			void patchbay(MidiDevice.Info in, MidiDevice.Info out) throws CLIException {
				MidiDevice midiout;
				try {
					midiout = MidiSystem.getMidiDevice(out);
					midiout.open();
				} catch (MidiUnavailableException e) {
					throw new CLIException(String.format("(-) Failed to open MIDI-out device (%s)", out.getName()), e);
				}
				try {
					MidiDevice midiin;
					try {
						midiin = MidiSystem.getMidiDevice(in);
						midiin.open();
					} catch (MidiUnavailableException ee) {
						throw new CLIException(String.format("(-) Failed to open MIDI-in device (%s)", in.getName()), ee);
					}
					try {
						Receiver r = midiout.getReceiver();
						try {
							Transmitter t = midiin.getTransmitter();
							try {
								t.setReceiver(r);
								System.out.println("Now in MIDI patch-bay mode. Hit enter to quit.");
								System.in.read();
							} catch (IOException e) {
								System.out.println("quitting due to an error.");
							} finally {
								System.out.println("closing transmitter");
								t.close();
							}
						} catch (MidiUnavailableException e) {
							throw new CLIException(String.format("(-) Failed to get transmitter from MIDI-in device (%s)", in.getName()), e);
						} finally {
							System.out.println("closing receiver");
							r.close();
						}
					} catch (MidiUnavailableException e) {
						throw new CLIException(String.format("(-) Failed to get receiver from MIDI-out device (%s)", out.getName()), e);
					} finally {
						midiin.close();
					}
				} finally {
					midiout.close();
				}
			}
		};

		public abstract void invoke(CLI cli, PrintStream ps) throws SymfonionException, IOException;
    }
    
    static class Route {
    	String in;
    	String out;
    	
    	Route(String in, String out) {
    		this.in = in;
    		this.out = out;
    	}
    }
    
    private Mode mode = Mode.VERSION;
    private File source;
    private File sink = new File("a.midi");
    private Route route = null;
    private Map<String, Pattern> midiins = new HashMap<String, Pattern>();
    private Map<String, Pattern> midiouts = new HashMap<String, Pattern>();
	private Symfonion symfonion;
	private Options options;
    
    public CLI(String... args) throws ParseException, CLIException {
        this.init(args);
        this.symfonion = createSymfonion();
    }
    
    protected Map<String, MidiDevice> prepareMidiOutDevices(PrintStream ps) throws CLIException {
		Map<String, Pattern> portDefinitions = this.getMidiOutDefinitions();
    	String deviceType = "out";
		Map<String, MidiDevice> devices = prepareMidiDevices(ps, deviceType,
				portDefinitions);
		return devices;
	}

    protected Map<String, MidiDevice> prepareMidiInDevices(PrintStream ps) throws CLIException {
		Map<String, Pattern> portDefinitions = this.getMidiOutDefinitions();
    	String deviceType = "in";
		Map<String, MidiDevice> devices = prepareMidiDevices(ps, deviceType,
				portDefinitions);
		return devices;
	}

	private Map<String, MidiDevice> prepareMidiDevices(PrintStream ps,
			String deviceType, Map<String, Pattern> portDefinitions)
			throws CLIException {
		Map<String, MidiDevice> devices = new HashMap<String, MidiDevice>();
		for (String portname : portDefinitions.keySet()) {
			Pattern regex = portDefinitions.get(portname);
			MidiDeviceScanner scanner = MidiDeviceScanner.chooseOutputDevices(ps, regex);
			scanner.scan();
			MidiDevice.Info[] matchedInfos = scanner.getMatchedDevices();
			if (matchedInfos.length > 1) {
				String msg = String.format("Device for port '%s' (regex:%s) wasn't identical (%d)", portname, regex, matchedInfos.length);
				throw new CLIException(msg);
			} else if (matchedInfos.length == 0) {
				String msg = String.format("No matching device was found for port '%s' (regex:%s)", portname, regex);
				throw new CLIException(msg);
			}
			try {
				devices.put(portname, MidiSystem.getMidiDevice(matchedInfos[0]));
			} catch (MidiUnavailableException e) {
				String msg = this.composeErrMsg(String.format("Failed to access MIDI-%s device:'%s'.", deviceType, matchedInfos[0].getName()), "O", null);
				throw new CLIException(msg, e);
			}
		}
		return devices;
	}

    protected File composeOutputFile(String outfile, String portName) {
		if (portName == null || Keyword.$default.equals(portName)) {
			return new File(outfile); 
		}
		File ret = null;
		int lastIndexOfDot = outfile.lastIndexOf('.'); 
		if (lastIndexOfDot == -1) {
			ret = new File(outfile + "." + portName);
		} else {
			ret = new File(outfile.substring(0, lastIndexOfDot) + "." + portName + outfile.substring(lastIndexOfDot));
		}
		return ret;
	}

	protected Options getOptions() {
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
        CommandLine ret = parser.parse( options, args );
        
        return ret;
    }

	private Options buildOptions() {
		// create Options object
        Options options = new Options();

        ////
        // Behavior options
        options.addOption("V", "version", false, "print the version information.");
        options.addOption("h", "help",    false, "print the command line usage.");
        options.addOption("l", "list",    false, "list the available midi devices.");
        options.addOption("p", "play",    true,  "play the specifiled file.");
        options.addOption("c", "compile", true, "compile the specified file to a standard midi file.");
        {
            Option option = OptionBuilder.create("r");
            option.setLongOpt("route");
            option.setValueSeparator('=');
            option.setArgs(2);
            option.setDescription("run a midi patch bay.");
            options.addOption(option);
        }

        ////
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
        		String msg = composeErrMsg("Output filename is required by this option.", "o", null);
        		throw new CLIException(msg);
        	}
            this.sink = new File(sinkFilename);
        }
        if (cmd.hasOption("V") || cmd.hasOption("version")) {
            this.mode = Mode.VERSION;
        } else if (cmd.hasOption("h") || cmd.hasOption("help")) {
            this.mode = Mode.HELP;
        } else if (cmd.hasOption("l") || cmd.hasOption("list")) {
            this.mode = Mode.LIST;
        } else if (cmd.hasOption("p") || cmd.hasOption("play")) {
            this.mode = Mode.PLAY;
            String sourceFilename = getSingleOptionValue(cmd, "p");
            if (sourceFilename == null) {
        		String msg = composeErrMsg("Input filename is required by this option.", "p", null);
        		throw new CLIException(msg);
            }
            this.source = new File(sourceFilename);
        } else if (cmd.hasOption("c") || cmd.hasOption("compile")) {
            this.mode = Mode.COMPILE;
            String sourceFilename = getSingleOptionValue(cmd, "c");
            if (sourceFilename == null) {
        		String msg = composeErrMsg("Input filename is required by this option.", "c", null);
        		throw new CLIException(msg);
            }
            this.source = new File(sourceFilename);
        } else if (cmd.hasOption("r") || cmd.hasOption("route")) {
            this.mode = Mode.ROUTE;
            Properties props = cmd.getOptionProperties("r");
            if (props.size() != 1) {
            	String msg = composeErrMsg("Route information is not given or specified multiple times.", "r", "route");
            	throw new CLIException(msg);
            }
            
            this.route = new Route(
            		cmd.getOptionValues('r')[0],
            		cmd.getOptionValues('r')[1]
            );
        } else {
            @SuppressWarnings("unchecked")
			List<String> leftovers = cmd.getArgList();
            if (leftovers.size() == 0) {
                this.mode = Mode.HELP;
            } else if (leftovers.size() == 1) {
                this.mode = Mode.PLAY;
                this.source = new File(leftovers.get(0));
            } else {
            	String msg = composeErrMsg(String.format("Unrecognized arguments:%s", leftovers.subList(2, leftovers.size())), "-", null);
            	throw new CLIException(msg);
            }
        }
    }

	private Map<String, Pattern> initializeMidiPorts(CommandLine cmd, String optionName	) throws CLIException {
		Properties props = cmd.getOptionProperties(optionName);
		Map<String, Pattern> ret = new HashMap<String, Pattern>();
		for (Object key : props.keySet()) {
			String portname = key.toString();
			String p = props.getProperty(portname);
			try {
				Pattern portpattern = Pattern.compile(p);
				ret.put(portname, portpattern);
			} catch (PatternSyntaxException e) {
				String msg = String.format("Regular expression '%' for '%s' isn't valid.", portname, p);
				throw new CLIException(composeErrMsg(msg, optionName, null), e);
			}
		}
		return ret;
	}

    private String composeErrMsg(String msg, String optionName, String longOptionName) {
    	if (longOptionName != null) {
    		return String.format("(-%s/--%s) %s", optionName, longOptionName, msg);
    	} else {
    		return String.format("(-%s) %s", optionName, msg);
    	}
	}
    
    private String getSingleOptionValue(CommandLine cmd, String optionName) throws CLIException {
    	String ret = cmd.getOptionValue(optionName);
    	int sz = cmd.getOptionProperties(optionName).size();
    	if (sz != 1) {
    		String msg = composeErrMsg(
    				String.format("This option requires one and only one value. (found %d times)", sz),
    				optionName, 
    				null
    				);
    		throw new CLIException(msg);
    	}
    	return ret;
    }

	public Mode getMode() {
        return this.mode;
    }
	
	private String license() {
		return  "Copyright 2013 Hiroshi Ukai.\n\n" + 
				"Licensed under the Apache License, Version 2.0 (the \"License\");" +
				"you may not use this work except in compliance with the License. " +
				"You may obtain a copy of the License in the LICENSE file, or at:\n\n" + 
				"http://www.apache.org/licenses/LICENSE-2.0\n\n" +
				"Unless required by applicable law or agreed to in writing, " + 
				"software distributed under the License is distributed on an \"AS IS\" " + 
				"BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either " + 
				"express or implied. See the License for the specific language " + 
				"governing permissions and limitations under the License.";
	}

	private String version() {
		String path = "/META-INF/maven/com.github.dakusui/symfonion/pom.properties";
		Properties props = new Properties();
		String version = "(N/A)";
		InputStream stream = getClass().getResourceAsStream(path);
		if (stream != null) {
			try {
				props.load(stream);
				version = props.getProperty("version");
			} catch (IOException e) {
			}
		}
		return version;
	}

    public File getSourceFile() {
        return this.source;
    }
    
    public File getSinkFile() {
        return this.sink ;
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
    	int exitCode = invoke(System.out, System.err, args);
    	System.exit(exitCode);
    }
    
    public static int invoke(PrintStream stdout, PrintStream stderr, String... args) {
    	int ret = 255;
    	try {
        	CLI cli = new CLI(args);
    		cli.mode.invoke(cli, stdout);
    		ret = 0;
    	} catch (ParseException e) {
    		printError(stderr, e);
    		ret = 1;
    	} catch (CLIException e) {
    		printError(stderr, e);
    		e.printStackTrace();
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
    	ps.println(String.format("symfonion: %s", t.getMessage()));
    }
}