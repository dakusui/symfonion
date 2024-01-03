package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.testutils.CliTestBase;
import org.junit.Test;

import java.io.IOException;

public class ReferenceErrorTest extends CliTestBase {
  @Test
  public void missingGroove() throws IOException, SymfonionException {
    String resourceName = "missingreferences/01_groovenotfound.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: $sequence[1].$groove: error: '17beats' undefined groove symbol\n"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingNoteMap() throws IOException, SymfonionException {
    String resourceName = "missingreferences/02_notemapnotfound.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: $patterns.melody1.$notemap: error: '$normal_notfound' undefined notemap symbol\n"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingNote() throws IOException, SymfonionException {
    String resourceName = "missingreferences/03_notenotfound.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: $patterns.melody1.$body[15]: error: 'Z' undefined note in $normal symbol\n"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingPart() throws IOException, SymfonionException {
    String resourceName = "missingreferences/04_partnotfound.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: $sequence[1].$patterns.vocal_notfound: error: 'vocal_notfound' undefined part symbol\n"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingPattern() throws IOException, SymfonionException {
    String resourceName = "missingreferences/05_patternnotfound.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: $sequence[1].$patterns.vocal[0]: error: 'melody1notfound' undefined pattern symbol\n"),
        compileResourceWithCli(resourceName)
    );
  }

}
