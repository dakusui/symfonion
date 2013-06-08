package com.github.dakusui.symfonion;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.core.Util;

public class ErrorTest {
	private File workFile;

	File load(String resourceName) throws IOException, SymfonionException {
		String content = Util.loadResource(resourceName);
		File ret = this.getWorkFile();
		PrintStream ps = new PrintStream(ret);
		try {
			ps.print(content);
		} finally {
			ps.close();
		}
		
		return ret;
	}
	
	@Before
	public void createWorkFile() throws IOException {
		this.workFile = File.createTempFile("symfonion-test", "js");
	}

	@Test
	public void nonwellformed_empty() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/01_nonwellformed.js";
		assertEquals(
				String.format("symfonion: %s: Not a JSON Object: null\n", getWorkFile()), 
				invokeWithResource(resourceName)
				);
	}

	@Test
	public void invalid_01() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/02_invalid_01.js";
		assertEquals(
				fmt("symfonion: %s: jsonpath: /: error: object(0 entries) at this path requires child element $sequence\n"), 
				invokeWithResource(resourceName)
				);
	}

	@Test
	public void nonwellformed_brokenobject() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/03_nonwellformed_02.js";
		assertEquals(
				String.format("symfonion: %s: Unterminated object at line 13 column 8\n", getWorkFile()), 
				invokeWithResource(resourceName)
				);
	}
	
	@Test
	public void invalid_array() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/04_invalid_array.js";
		assertEquals(
				String.format("symfonion: %s: Not a JSON Object: []\n", getWorkFile()), 
				invokeWithResource(resourceName)
				);
	}

	private String fmt(String fmt) {
		return String.format(fmt, getWorkFile());
	}

	@Test
	public void illegalfraction() throws IOException, SymfonionException, ParseException {
		String resourceName = "illegalvalues/01_illegalfraction.js";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $sequence[0].$beats: error: 16*16(primitive) is invalid. (This value must be a fraction. e.g. '1/2', '1/4', and so on.)\n"),
				invokeWithResource(resourceName)
				);
	}

	@Test
	public void illegalnotelength_01() throws IOException, SymfonionException, ParseException {
		String resourceName = "illegalvalues/02_illegalnotelength_groove.js";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $grooves.16beats[1].$length: error: 1/6(primitive) is invalid. (This value must be a note length. e.g. '4', '8.', '16')\n"),
				invokeWithResource(resourceName)
				);
	}
	@Test
	public void illegalnotelength_02() throws IOException, SymfonionException, ParseException {
		String resourceName = "illegalvalues/03_illegalnotelength_pattern.js";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $patterns.melody1.$parameters.$length: error: 1/7(primitive) is invalid. (This value must be a note length. e.g. '4', '8.', '16')\n"),
				invokeWithResource(resourceName)
				);
	}

	@Test
	public void illegalnotelength_03() throws IOException, SymfonionException, ParseException {
		String resourceName = "illegalvalues/04_illegalnotelength_stroke.js";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $patterns.melody1.$body[15].$length: error: 1/2(primitive) is invalid. (This value must be a note length. e.g. '4', '8.', '16')\n"),
				invokeWithResource(resourceName)
				);
	}
	
	@Test
	public void missinggroove() throws IOException, SymfonionException, ParseException {
		String resourceName = "missingreferences/01_groovenotfound.js";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $sequence[1].$groove: error: '17beats' undefined groove symbol\n"),
				invokeWithResource(resourceName)
				);
	}
	
	@Test
	public void missingnotemap() throws IOException, SymfonionException, ParseException {
		String resourceName = "missingreferences/02_notemapnotfound.js";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $patterns.melody1.$notemap: error: '$normal_notfound' undefined notemap symbol\n"),
				invokeWithResource(resourceName)
				);
	}
	
	@Test
	public void missingnote() throws IOException, SymfonionException, ParseException {
		String resourceName = "missingreferences/03_notenotfound.js";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $patterns.melody1.$body[15]: error: 'Z' undefined note in $normal symbol\n"),
				invokeWithResource(resourceName)
				);
	}
	
	@Test
	public void missingpart() throws IOException, SymfonionException, ParseException {
		String resourceName = "missingreferences/04_partnotfound.js";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $sequence[1].$patterns.vocal[0]: error: 'melody1_notfound' undefined pattern symbol\n"),
				invokeWithResource(resourceName)
				);
	}
	
	@Test
	public void missingpattern() throws IOException, SymfonionException, ParseException {
		String resourceName = "missingreferences/05_patternnotfound.js";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $sequence[1].$patterns.vocal[0]: error: 'melody1notfound' undefined pattern symbol\n"),
				invokeWithResource(resourceName)
				);
	}
	
	private String invokeWithResource(String resourceName) throws IOException,
			SymfonionException, ParseException, CLIException {
		ByteArrayOutputStream baos;
		PrintStream ps = new PrintStream(baos = new ByteArrayOutputStream());
		this.workFile = load(resourceName);
		CLI.invoke(ps, ps, "-c", workFile.getAbsolutePath());
		return baos.toString();
	}
	
	public File getWorkFile() {
		return this.workFile;
	}
}
