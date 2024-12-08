package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.testutils.CliTestBase;
import com.github.valid8j.pcond.forms.Predicates;
import org.junit.Test;

import java.io.IOException;

public class ReferenceErrorTest extends CliTestBase {
  @Test
  public void missingGroove() throws IOException, SymfonionException {
    String resourceName = "missingReferences/01_grooveNotFound.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .\"$sequence\"[1].\"$groove\": error: '17beats' undefined groove symbol\n"),
        compileResourceWithCli(resourceName)
    );
    System.out.println(Predicates.isInstanceOf());
  }

  @Test
  public void missingNoteMap() throws IOException, SymfonionException {
    String resourceName = "missingReferences/02_notemapNotFound.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .\"$patterns\".melody1.\"$notemap\": error: '$normal_notfound' undefined notemap symbol\n"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingNote() throws IOException, SymfonionException {
    String resourceName = "missingReferences/03_noteNotFound.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .\"$patterns\".melody1.\"$body\"[15]: error: 'Z' undefined note in $normal symbol\n"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingPart() throws IOException, SymfonionException {
    String resourceName = "missingReferences/04_partNotFound.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .\"$sequence\"[1].\"$parts\".vocal_notfound: error: 'vocal_notfound' undefined part symbol\n"),
        compileResourceWithCli(resourceName)
    );
  }

  @Test
  public void missingPattern() throws IOException, SymfonionException {
    String resourceName = "missingReferences/05_patternNotFound.json";
    assertActualObjectToStringValueContainsExpectedString(
        fmt("symfonion: %s: jsonpath: .\"$sequence\"[1].\"$parts\".vocal[0]: error: 'melody1notfound' undefined pattern symbol\n"),
        compileResourceWithCli(resourceName)
    );
  }

}
