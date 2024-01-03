package com.github.dakusui.symfonion.tests;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.nio.charset.StandardCharsets;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.CliRecord;
import com.github.dakusui.symfonion.cli.subcommands.PresetSubcommand;
import com.github.dakusui.symfonion.exceptions.CliException;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.testutils.TestBase;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class CliTest extends TestBase {
	@Test
	public void help_01() throws CliException, ParseException {
		Cli r = new Cli("-h");
		assertEquals(PresetSubcommand.HELP, r.getMode());
	}

	@Test
	public void help_02() throws ParseException, CliException {
		Cli r = new Cli("--help");
		assertEquals(PresetSubcommand.HELP, r.getMode());
	}
	
	@Test
	public void compile_01() throws ParseException, CliException {
		String srcFileName = "test.json";
		Cli r = new Cli("-c", srcFileName);
		assertEquals(PresetSubcommand.COMPILE, r.getMode());
		assertEquals(new File(srcFileName), r.getSourceFile());
	}
	
	@Test
	public void outportOption_01() throws ParseException, CliException {
		Cli r = new Cli("-O", "out1=test1");
		assertEquals("test1", r.getMidiOutDefinitions().get("out1").toString());
	}
	
	@Test
	public void outportOption_02() throws ParseException, CliException {
		Cli r = new Cli("-O", "out1=test1", "-O", "out2=test2");
		assertEquals("test1", r.getMidiOutDefinitions().get("out1").toString());
		assertEquals("test2", r.getMidiOutDefinitions().get("out2").toString());
	}

	@Test
	public void inportOption_01() throws ParseException, CliException {
		Cli r = new Cli("-I", "in1=test1");
		assertEquals("test1", r.getMidiInDefinitions().get("in1").toString());
	}
	
	@Test
	public void inportOption_02() throws ParseException, CliException {
		Cli r = new Cli("-I", "in1=test1", "-I", "in2=test2");
		assertEquals("test1", r.getMidiInDefinitions().get("in1").toString());
		assertEquals("test2", r.getMidiInDefinitions().get("in2").toString());
	}

	@Test
	public void whenList() throws SymfonionException, ParseException, IOException {
		CliRecord r = new CliRecord.Builder("--list").build();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		r.subcommand().invoke(r, new PrintStream(out), System.in);

		System.err.println(out.toString(StandardCharsets.UTF_8));
	}
}
