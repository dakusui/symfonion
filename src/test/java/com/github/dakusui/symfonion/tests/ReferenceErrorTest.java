package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ReferenceErrorTest extends ErrorTest {

	@Test
	public void missinggroove() throws IOException, SymfonionException,
			ParseException {
				String resourceName = "missingreferences/01_groovenotfound.json";
				assertEquals(
						fmt("symfonion: %s: jsonpath: $sequence[1].$groove: error: '17beats' undefined groove symbol\n"),
						invokeWithResource(resourceName)
						);
			}

	@Test
	public void missingnotemap() throws IOException, SymfonionException,
			ParseException {
				String resourceName = "missingreferences/02_notemapnotfound.json";
				assertEquals(
						fmt("symfonion: %s: jsonpath: $patterns.melody1.$notemap: error: '$normal_notfound' undefined notemap symbol\n"),
						invokeWithResource(resourceName)
						);
			}

	@Test
	public void missingnote() throws IOException, SymfonionException,
			ParseException {
				String resourceName = "missingreferences/03_notenotfound.json";
				assertEquals(
						fmt("symfonion: %s: jsonpath: $patterns.melody1.$body[15]: error: 'Z' undefined note in $normal symbol\n"),
						invokeWithResource(resourceName)
						);
			}

	@Test
	public void missingpart() throws IOException, SymfonionException,
			ParseException {
				String resourceName = "missingreferences/04_partnotfound.json";
				assertEquals(
						fmt("symfonion: %s: jsonpath: $sequence[1].$patterns.vocal_notfound: error: 'vocal_notfound' undefined part symbol\n"),
						invokeWithResource(resourceName)
						);
			}

	@Test
	public void missingpattern() throws IOException, SymfonionException,
			ParseException {
				String resourceName = "missingreferences/05_patternnotfound.json";
				assertEquals(
						fmt("symfonion: %s: jsonpath: $sequence[1].$patterns.vocal[0]: error: 'melody1notfound' undefined pattern symbol\n"),
						invokeWithResource(resourceName)
						);
			}

}
