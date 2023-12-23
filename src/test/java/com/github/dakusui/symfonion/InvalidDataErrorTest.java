package com.github.dakusui.symfonion;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import com.github.dakusui.symfonion.core.SymfonionException;

public class InvalidDataErrorTest extends ErrorTest {

	@Test
	public void illegalfraction() throws IOException, SymfonionException,
			ParseException {
				String resourceName = "illegalvalues/01_illegalfraction.json";
				assertEquals(
						fmt("symfonion: %s: jsonpath: $sequence[0].$beats: error: 16*16(primitive) is invalid. (This value must be a fraction. e.g. '1/2', '1/4', and so on.)\n"),
						invokeWithResource(resourceName)
						);
			}

	@Test
	public void illegalnotelength_01() throws IOException,
			SymfonionException, ParseException {
				String resourceName = "illegalvalues/02_illegalnotelength_groove.json";
				assertEquals(
						fmt("symfonion: %s: jsonpath: $grooves.16beats[1].$length: error: 1/6(primitive) is invalid. (This value must be a note length. e.g. '4', '8.', '16')\n"),
						invokeWithResource(resourceName)
						);
			}

	@Test
	public void illegalnotelength_02() throws IOException,
			SymfonionException, ParseException {
				String resourceName = "illegalvalues/03_illegalnotelength_pattern.json";
				assertEquals(
						fmt("symfonion: %s: jsonpath: $patterns.melody1.$length: error: 1/7(primitive) is invalid. (This value must be a note length. e.g. '4', '8.', '16')\n"),
						invokeWithResource(resourceName)
						);
			}

	@Test
	public void illegalnotelength_03() throws IOException,
			SymfonionException, ParseException {
				String resourceName = "illegalvalues/04_illegalnotelength_stroke.json";
				assertEquals(
						fmt("symfonion: %s: jsonpath: $patterns.melody1.$body[15].$length: error: 1/2(primitive) is invalid. (This value must be a note length. e.g. '4', '8.', '16')\n"),
						invokeWithResource(resourceName)
						);
			}

}
