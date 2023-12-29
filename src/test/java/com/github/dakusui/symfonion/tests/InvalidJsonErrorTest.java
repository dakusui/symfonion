package com.github.dakusui.symfonion.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import com.github.dakusui.symfonion.exceptions.SymfonionException;

public class InvalidJsonErrorTest extends ErrorTest {
	@Test
	public void nonwellformed_empty() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/01_nonwellformed.json";
		assertEquals(
				String.format("symfonion: %s: Not a JSON Object: null\n", getWorkFile()), 
				invokeWithResource(resourceName)
				);
	}

	@Test
	public void invalid_01() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/02_invalid_01.json";
		assertEquals(
				fmt("symfonion: %s: jsonpath: /: error: object(0 entries) at this path requires child element $sequence\n"),
				invokeWithResource(resourceName)
				);
	}

	@Test
	public void nonwellformed_brokenobject() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/03_nonwellformed_02.json";
		assertEquals(
				String.format("symfonion: %s: Unterminated object at line 13 column 8 path $.$patterns.melody1.$body[15].$length\n", getWorkFile()),
				invokeWithResource(resourceName)
				);
	}
	
	@Test
	public void invalid_array() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/04_invalid_array.json";
		assertEquals(
				String.format("symfonion: %s: Not a JSON Object: []\n", getWorkFile()), 
				invokeWithResource(resourceName)
				);
	}
	
	@Test
	public void missingsection_parts() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/05_missingsection_parts.json";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $sequence[1].$patterns.vocal: error: 'vocal' undefined part symbol\n"),
				invokeWithResource(resourceName)
				);
	}

	@Test
	public void missingsection_pattern() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/06_missingsection_patterns.json";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $sequence[1].$patterns.vocal[0]: error: 'melody1' undefined pattern symbol\n"),
				invokeWithResource(resourceName)
				);
	}

	@Test
	public void missingsection_groove() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/07_missingsection_grooves.json";
		assertEquals(
				fmt("symfonion: %s: jsonpath: $sequence[1].$groove: error: '16beats' undefined groove symbol\n"),
				invokeWithResource(resourceName)
				);
	}
	@Test
	public void missingsection_sequence() throws IOException, SymfonionException, ParseException {
		String resourceName = "invalidjson/08_missingsection_sequences.json";
		assertEquals(
				fmt("symfonion: %s: jsonpath: /: error: object(4 entries) at this path requires child element $sequence\n"),
				invokeWithResource(resourceName)
				);
	}
}
