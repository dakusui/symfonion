package com.github.dakusui.symfonion;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class ArgTest {
	public static void _main(String... args) throws ParseException {
		Option opt = new Option("T", true, "example");
		opt.setValueSeparator('=');
		opt.setArgs(2);
		
		Options options = new Options();
		options.addOption(opt);
		CommandLineParser parser = new PosixParser(); 
		CommandLine cmd = parser.parse(options, new String[]{"-Thello=world", "-THELLO=WORLD", "-THi", "-T"});
		
		////
		//
		System.out.println("cmd.getOptions():" +  Arrays.toString(cmd.getOptions()));
		System.out.println("cmd.getArgs():" +  Arrays.toString(cmd.getArgs()));
		System.out.println("cmd.getOptionValues('T')" + Arrays.toString(cmd.getOptionValues('T')));
		System.out.println("opt.getValues():" + Arrays.toString(opt.getValues()));
		System.out.println("cmd.getOptionObject('T')" + cmd.getOptionObject('T'));
		System.out.println(cmd.getOptionProperties("T"));
		System.out.println(File.pathSeparator);
	}

	public static void main(String... args) throws ParseException {
		Option opt = new Option("T", true, "example");
		opt.setOptionalArg(true);
		opt.setValueSeparator('=');
		opt.setArgs(2);
		
		Options options = new Options();
		options.addOption(opt);
		CommandLineParser parser = new PosixParser(); 
		CommandLine cmd = parser.parse(options, new String[]{"-Tk", "-Tt"});
		
		////
		//
		System.out.println("cmd.getOptions():" +  Arrays.toString(cmd.getOptions()));
		System.out.println("cmd.getArgs():" +  Arrays.toString(cmd.getArgs()));
		System.out.println("cmd.getOptionValues('T')" + Arrays.toString(cmd.getOptionValues('T')));
		System.out.println("opt.getValues():" + Arrays.toString(opt.getValues()));
		System.out.println("cmd.getOptionObject('T')" + cmd.getOptionObject('T'));
		System.out.println(cmd.getOptionProperties("T"));
		System.out.println(File.pathSeparator);
	}
}
