package com.github.dakusui.symfonion;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.github.dakusui.symfonion.core.SymfonionException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class CLITest {
	@Test
	public void help_01() throws CLIException, ParseException {
		CLI r = new CLI("-h");
		assertEquals(CLI.Mode.HELP, r.getMode());
	}

	@Test
	public void help_02() throws ParseException, IOException, CLIException {
		CLI r = new CLI("--help");
		assertEquals(CLI.Mode.HELP, r.getMode());
	}
	
	@Test
	public void compile_01() throws ParseException, CLIException {
		String srcFileName = "test.json";
		CLI r = new CLI("-c", srcFileName);
		assertEquals(CLI.Mode.COMPILE, r.getMode());
		assertEquals(new File(srcFileName), r.getSourceFile());
	}
	
	@Test
	public void outportOption_01() throws ParseException, CLIException {
		CLI r = new CLI("-O", "out1=test1");
		assertEquals("test1", r.getMidiOutDefinitions().get("out1").toString());
	}
	
	@Test
	public void outportOption_02() throws ParseException, CLIException {
		CLI r = new CLI("-O", "out1=test1", "-O", "out2=test2");
		assertEquals("test1", r.getMidiOutDefinitions().get("out1").toString());
		assertEquals("test2", r.getMidiOutDefinitions().get("out2").toString());
	}

	@Test
	public void inportOption_01() throws ParseException, CLIException {
		CLI r = new CLI("-I", "in1=test1");
		assertEquals("test1", r.getMidiInDefinitions().get("in1").toString());
	}
	
	@Test
	public void inportOption_02() throws ParseException, CLIException {
		CLI r = new CLI("-I", "in1=test1", "-I", "in2=test2");
		assertEquals("test1", r.getMidiInDefinitions().get("in1").toString());
		assertEquals("test2", r.getMidiInDefinitions().get("in2").toString());
	}

	@Test
	public void whenList() throws SymfonionException, ParseException, IOException {
		CLI r = new CLI("--list");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		r.getMode().invoke(r, new PrintStream(out));

		System.err.println(out.toString(StandardCharsets.UTF_8));
	}
}
