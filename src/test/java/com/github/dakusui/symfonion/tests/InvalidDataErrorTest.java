package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.testutils.CliTestBase;
import org.junit.Test;

import java.io.IOException;

public class InvalidDataErrorTest extends CliTestBase {

  @Test
  public void illegalFraction() throws IOException, SymfonionException {
    String resourceName = "illegalValues/01_illegalFraction.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .\"$sequence\"[0].\"$beats\": error: \"16*16\" (primitive) is invalid. (This value must be a fraction. e.g. '1/2', '1/4', and so on.)"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void illegalNoteLength_01() throws IOException, SymfonionException {
    String resourceName = "illegalValues/02_illegalNoteLength_groove.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .\"$grooves\".\"16beats\"[1].\"$length\": error: \"1/6\" (primitive) is invalid. (This value must be a note length. e.g. '4', '8.', '16')"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void illegalNoteLength_02() throws IOException, SymfonionException {
    String resourceName = "illegalValues/03_illegalNoteLength_pattern.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .\"$patterns\".melody1.\"$length\": error: \"1/7\" (primitive) is invalid. (This value must be a note length. e.g. '4', '8.', '16')"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void illegalNoteLength_03() throws IOException, SymfonionException {
    String resourceName = "illegalValues/04_illegalNoteLength_stroke.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .\"$patterns\".melody1.\"$body\"[15].\"$length\": error: \"1/2\" (primitive) is invalid. (This value must be a note length. e.g. '4', '8.', '16')"),
        compileResourceWithCli(resourceName)
    );
  }
}
