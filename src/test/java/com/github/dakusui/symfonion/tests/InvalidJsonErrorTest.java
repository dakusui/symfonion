package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.testutils.CliTestBase;
import org.junit.Test;

import java.io.IOException;

public class InvalidJsonErrorTest extends CliTestBase {
	@Test
	public void nonwellformed_empty() throws IOException, SymfonionException {
		String resourceName = "invalidjson/01_nonwellformed.json";
		assertActualObjectToStringValueContainsExpectedString(
				String.format("symfonion: %s: Not a JSON Object: null\n", getWorkFile()), 
				compileResourceWithCli(resourceName)
				);
	}

	@Test
	public void invalid_01() throws IOException, SymfonionException {
		String resourceName = "invalidjson/02_invalid_01.json";
		assertActualObjectToStringValueContainsExpectedString(
				fmt("symfonion: %s: jsonpath: /: error: object(0 entries) at this path requires child element $sequence\n"),
				compileResourceWithCli(resourceName)
				);
	}

	@Test
	public void nonwellformed_brokenobject() throws IOException, SymfonionException {
		String resourceName = "invalidjson/03_nonwellformed_02.json";
		assertActualObjectToStringValueContainsExpectedString(
				String.format("symfonion: %s: Unterminated object at line 13 column 8 path $.$patterns.melody1.$body[15].$length\n", getWorkFile()),
				compileResourceWithCli(resourceName)
				);
	}
	
	@Test
	public void invalid_array() throws IOException, SymfonionException {
		String resourceName = "invalidjson/04_invalid_array.json";
		assertActualObjectToStringValueContainsExpectedString(
				String.format("symfonion: %s: Not a JSON Object: []\n", getWorkFile()), 
				compileResourceWithCli(resourceName)
				);
	}
	
	@Test
	public void missingsection_parts() throws IOException, SymfonionException {
		String resourceName = "invalidjson/05_missingsection_parts.json";
		assertActualObjectToStringValueContainsExpectedString(
				fmt("symfonion: %s: jsonpath: $sequence[1].$patterns.vocal: error: 'vocal' undefined part symbol\n"),
				compileResourceWithCli(resourceName)
				);
	}

	@Test
	public void missingsection_pattern() throws IOException, SymfonionException {
		String resourceName = "invalidjson/06_missingsection_patterns.json";
		assertActualObjectToStringValueContainsExpectedString(
				fmt("symfonion: %s: jsonpath: $sequence[1].$patterns.vocal[0]: error: 'melody1' undefined pattern symbol\n"),
				compileResourceWithCli(resourceName)
				);
	}

	@Test
	public void missingsection_groove() throws IOException, SymfonionException {
		String resourceName = "invalidjson/07_missingsection_grooves.json";
		assertActualObjectToStringValueContainsExpectedString(
				fmt("symfonion: %s: jsonpath: $sequence[1].$groove: error: '16beats' undefined groove symbol\n"),
				compileResourceWithCli(resourceName)
				);
	}
	@Test
	public void missingsection_sequence() throws IOException, SymfonionException {
		String resourceName = "invalidjson/08_missingsection_sequences.json";
		assertActualObjectToStringValueContainsExpectedString(
				fmt("symfonion: %s: jsonpath: /: error: object(4 entries) at this path requires child element $sequence\n"),
				compileResourceWithCli(resourceName)
				);
	}
}
