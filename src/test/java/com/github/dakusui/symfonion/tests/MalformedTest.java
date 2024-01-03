package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.testutils.CliTestBase;
import org.junit.Test;

import java.io.IOException;

public class MalformedTest extends CliTestBase {
  @Test
  public void givenMalformed_emptyFile() throws IOException, SymfonionException {
    String resourceName = "invalidJson/01_nonwellformed.json";
    assertActualObjectToStringValueContainsExpectedString(
        String.format("symfonion: %s: Not a JSON Object: null\n", getWorkFile()),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void givenMalformed_brokenObject() throws IOException, SymfonionException {
    String resourceName = "invalidJson/03_nonwellformed_02.json";
    assertActualObjectToStringValueContainsExpectedString(
        String.format("symfonion: %s: Unterminated object at line 13 column 8 path $.$patterns.melody1.$body[15].$length\n", getWorkFile()),
        compileResourceWithCli(resourceName)
    );
  }

}
