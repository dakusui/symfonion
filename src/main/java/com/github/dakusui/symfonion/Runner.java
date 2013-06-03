package com.github.dakusui.symfonion;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Runner {
	public static void main(String[] args) throws ParseException {
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption("V", "version", false, "print the version information.");
		options.addOption("h", "help",    false, "print the command line usage.");
		options.addOption("l", "list",    false, "list the available midi devices.");
		options.addOption("p", "play",    false, "play the specifiled file.");
		options.addOption("c", "compile", false, "compile the specified file to a standard midi file.");
		options.addOption("r", "route",   true,  "run a midi patch bay.");
		
		options.addOption("O", true,  "specify midi out port.");
		options.addOption("I", true,  "specify midi in port.");
		options.addOption("o", true,  "specify a file to which a compiled standard midi file is output.");
		
		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse( options, args );
		
		try {
			new Runner(cmd).run(options);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private CommandLine cmd;
	
	public Runner(CommandLine cmd) {
		this.cmd = cmd;
	}
	
	public void run(Options options) throws IOException {
		if (cmd.hasOption("V") || cmd.hasOption("version")) {
			this.version(System.out);
		} else if (cmd.hasOption("h") || cmd.hasOption("help")) {
			this.help(System.out, options);
		} else if (cmd.hasOption("l") || cmd.hasOption("list")) {
			this.listDevices(System.out);
		} else if (cmd.hasOption("p") || cmd.hasOption("play")) {
			
		} else if (cmd.hasOption("c") || cmd.hasOption("compile")) {
			
		} else if (cmd.hasOption("r") || cmd.hasOption("route")) {
			
		} else {
		}
	}

	void listDevices(PrintStream ps) {
		Symfonion.printMidiOutDevices(ps);
		ps.println();
		Symfonion.printMidiInDevices(ps);
	}
	
	void compile(PrintStream ps) {
		
	}
	
	void play(PrintStream ps) {
		
	}
	
	void help(PrintStream ps, Options options) {
		ps.println("help");
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("SYNTAX", options);
	}
	void version(PrintStream ps) throws IOException {
		  ps.println("SyMFONION " + version());
		  
		  ps.println(license());
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
	
	private String version() throws IOException {
		String path = "/META-INF/maven/com.github.dakusui/symfonion/pom.properties";
		InputStream stream = getClass().getResourceAsStream(path);
		Properties props = new Properties();
		props.load(stream);
		return props.getProperty("version");
	}
}
