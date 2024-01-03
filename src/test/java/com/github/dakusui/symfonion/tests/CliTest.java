package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.cli.CliRecord;
import com.github.dakusui.symfonion.cli.subcommands.PresetSubcommand;
import com.github.dakusui.symfonion.exceptions.CliException;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.testutils.TestBase;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class CliTest extends TestBase {
	static CliRecord createCli(String... args) throws ParseException {
		return new CliRecord.Builder(args).build();
	}
	@Test
	public void help_01() throws CliException, ParseException {
		CliRecord r = createCli("-h");
		assertEquals(PresetSubcommand.HELP, r.subcommand());
	}

	@Test
	public void help_02() throws ParseException, CliException {
		CliRecord r = createCli("--help");
		assertEquals(PresetSubcommand.HELP, r.subcommand());
	}
	
	@Test
	public void compile_01() throws ParseException, CliException {
		String srcFileName = "test.json";
		CliRecord r = createCli("-c", srcFileName);
		assertEquals(PresetSubcommand.COMPILE, r.subcommand());
		assertEquals(new File(srcFileName), r.source());
	}
	
	@Test
	public void outportOption_01() throws ParseException, CliException {
		CliRecord r = createCli("-O", "out1=test1");
		assertEquals("test1", r.midiOutRegexPatterns().get("out1").toString());
	}
	
	@Test
	public void outportOption_02() throws ParseException, CliException {
		CliRecord r = createCli("-O", "out1=test1", "-O", "out2=test2");
		assertEquals("test1", r.midiOutRegexPatterns().get("out1").toString());
		assertEquals("test2", r.midiOutRegexPatterns().get("out2").toString());
	}

	@Test
	public void inportOption_01() throws ParseException, CliException {
		CliRecord r = createCli("-I", "in1=test1");
		assertEquals("test1", r.midiInRegexPatterns().get("in1").toString());
	}
	
	@Test
	public void inportOption_02() throws ParseException, CliException {
		CliRecord r = createCli("-I", "in1=test1", "-I", "in2=test2");
		assertEquals("test1", r.midiInRegexPatterns().get("in1").toString());
		assertEquals("test2", r.midiInRegexPatterns().get("in2").toString());
	}

	@Test
	public void whenList() throws SymfonionException, ParseException, IOException {
		CliRecord r = new CliRecord.Builder("--list").build();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		r.subcommand().invoke(r, new PrintStream(out), System.in);

		System.err.println(out.toString(StandardCharsets.UTF_8));
	}
}
