package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.testutils.CliTestBase;
import org.junit.Test;

import java.io.IOException;

public class InvalidJsonErrorTest extends CliTestBase {
  @Test
  public void invalid_01() throws IOException, SymfonionException {
    String resourceName = "invalidJson/02_invalid_01.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .: error: {} (object: 0 entries) at this path requires child element $sequence\n"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void invalid_array() throws IOException, SymfonionException {
    String resourceName = "invalidJson/04_invalid_array.json";
    assertActualObjectToStringValueContainsExpectedString(
        String.format("symfonion: %s: Not a JSON Object: []\n", getWorkFile()),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingSection_parts() throws IOException, SymfonionException {
    String resourceName = "invalidJson/05_missingSection_parts.json";
    assertActualObjectToStringValueContainsExpectedString(
        /* Actually, this error message is not good. This should be complained by symfonion, not by JsonUtils.*/
        fmt("This element ({\"$notemaps\":{},\"$patterns\":{\"...\":\"...\"},\"$grooves\":{\"...\":\"...\"},\"$sequence\":[\"...\"]}) doesn't have path: [$parts]\n"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingSection_pattern() throws IOException, SymfonionException {
    String resourceName = "invalidJson/06_missingSection_patterns.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .: error: {\"$parts\":{\"...\":\"...\"},\"$notemaps\":{},\"$grooves\":{\"...\":\"...\"},\"$sequence\":[\"...\"]} (object: 4 entries) at this path requires child element $patterns\n"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingSection_groove() throws IOException, SymfonionException {
    String resourceName = "invalidJson/07_missingSection_grooves.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .: error: {\"$parts\":{\"...\":\"...\"},\"$notemaps\":{},\"$patterns\":{\"...\":\"...\"},\"$sequence\":[\"...\"]} (object: 4 entries) at this path requires child element $grooves"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingSection_sequence() throws IOException, SymfonionException {
    String resourceName = "invalidJson/08_missingSection_sequences.json";

    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .: error: " +
            "{\"$parts\":{\"...\":\"...\"},\"$notemaps\":{},\"$patterns\":{\"...\":\"...\"},\"$grooves\":{\"...\":\"...\"}}" +
            " (object: 4 entries)" +
            " at this path requires child element $sequence\n"),
        compileResourceWithCli(resourceName)
    );
  }
}
