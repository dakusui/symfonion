package com.github.dakusui.exception;

import com.github.dakusui.symfonion.testutils.TestBase;
import org.junit.Test;

import static com.github.dakusui.symfonion.exception.SymfonionExceptionThrower.*;
import static com.github.dakusui.symfonion.exception.SymfonionExceptionThrower.Key.FILENAME;

public class ExceptionThrowerTest extends TestBase {
  @Test(expected = RuntimeException.class)
  public void tryExceptionThrower() {
    try (var ignored = context(entry(FILENAME, "aFilename"))) {
      exampleMethod();
    }
  }

  @Test(expected = RuntimeException.class)
  public void tryExceptionThrowerNestedContext() {
    try (var ignored = context(entry(FILENAME, "aFilename"))) {
      try (var ignored2 = context(entry(FILENAME, "bFilename"))) {
        exampleMethod();
      }
    }
  }


  @Test(expected = RuntimeException.class)
  public void tryExceptionThrowerNestedContextWithShorthand() {
    try (var ignored = context($(FILENAME, "aFilename"))) {
      try (var ignored2 = context($(FILENAME, "bFilename"))) {
        exampleMethod();
      }
    }
  }

  private void exampleMethod() {
    throw FILE_BROKEN.exception("Ooops");
  }
}
