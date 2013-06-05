package com.github.dakusui.symfonion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.github.dakusui.logias.lisp.Context;

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
				ps.println("help");
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("SYNTAX", cli.getOptions());
			}
		},
        LIST {
			@Override
			public void invoke(CLI cli, PrintStream ps) {
				Symfonion.printMidiOutDevices(ps);
				ps.println();
				Symfonion.printMidiInDevices(ps);
			}
		},
        PLAY {
			@Override
			public void invoke(CLI cli, PrintStream ps) {
				// TODO Auto-generated method stub
				
			}
		},
        COMPILE {
			@Override
			public void invoke(CLI cli, PrintStream ps) {
				// TODO Auto-generated method stub
				
			}
		},
        ROUTE {
			@Override
			public void invoke(CLI cli, PrintStream ps) {
				// TODO Auto-generated method stub
				
			}
		};

		public abstract void invoke(CLI cli, PrintStream ps);
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
    private List<Route> routes = new LinkedList<Route>();
    private Map<String, Pattern> midiins = new HashMap<String, Pattern>();
    private Map<String, Pattern> midiouts = new HashMap<String, Pattern>();
	private Symfonion symfonion;
	private Options options;
    
    public CLI(String... args) throws ParseException, CLIException {
        this.init(args);
        this.symfonion = createSymfonion();
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
        	String sinkFilename = cmd.getOptionValue('o');
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
            String sourceFilename = cmd.getOptionValue('p');
            if (sourceFilename == null) {
        		String msg = composeErrMsg("Input filename is required by this option.", "p", null);
        		throw new CLIException(msg);
            }
            this.source = new File(sourceFilename);
        } else if (cmd.hasOption("c") || cmd.hasOption("compile")) {
            this.mode = Mode.COMPILE;
            String sourceFilename = cmd.getOptionValue("c");
            if (sourceFilename == null) {
        		String msg = composeErrMsg("Input filename is required by this option.", "c", null);
        		throw new CLIException(msg);
            }
            this.source = new File(sourceFilename);
        } else if (cmd.hasOption("r") || cmd.hasOption("route")) {
            this.mode = Mode.ROUTE;
            Properties props = cmd.getOptionProperties("r");
            routes.clear();
            for (Object key: props.keySet()) {
            	String outport = key.toString();
            	Route cur = new Route(outport.toString(), props.getProperty(outport));
            	routes.add(cur);
            }
        } else {
            @SuppressWarnings("unchecked")
			List<String> leftovers = cmd.getArgList();
            if (leftovers.size() == 0) {
                this.mode = Mode.HELP;
            } else if (leftovers.size() == 1) {
                this.mode = Mode.PLAY;
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
				ret.put(p, portpattern);
			} catch (PatternSyntaxException e) {
				String msg = String.format("Regular expression '%' for '%s' isn't a valid regular expression.", portname, p);
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

	public Mode getMode() {
        return this.mode;
    }
	
	private String license() {
		return "Copyright 2013 Hiroshi Ukai.\n\n" + 
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
		InputStream stream = getClass().getResourceAsStream(path);
		Properties props = new Properties();
		String version = "(N/A)";
		try {
			props.load(stream);
			version = props.getProperty("version");
		} catch (IOException e) {
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
    
    public static void main(String[] args) throws ParseException, CLIException {
    	CLI cli = new CLI(args);
    	int exitCode = 255;
    	try {
    		cli.mode.invoke(cli, System.out);
    		exitCode = 0; 
    	} catch (Exception e) {
    		
    	} finally {
    		System.exit(exitCode);
    	}
    }
}