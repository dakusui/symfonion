package com.github.dakusui.exception;

import com.github.dakusui.symfonion.testutils.TestBase;
import org.junit.jupiter.api.Test;

import static com.github.dakusui.symfonion.exception.SymfonionExceptionThrower.*;
import static com.github.dakusui.symfonion.exception.SymfonionExceptionThrower.Key.FILENAME;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionThrowerTest extends TestBase {
  @Test
  public void tryExceptionThrower() {
    assertThrows(RuntimeException.class, () -> {
      try (var ignored = context(entry(FILENAME, "aFilename"))) {
        exampleMethod();
      }
    });
  }

  @Test
  public void tryExceptionThrowerNestedContext() {
    assertThrows(RuntimeException.class, () -> {
      try (var ignored = context(entry(FILENAME, "aFilename"))) {
        try (var ignored2 = context(entry(FILENAME, "bFilename"))) {
          exampleMethod();
        }
      }
    });
  }


  @Test
  public void tryExceptionThrowerNestedContextWithShorthand() {
    assertThrows(RuntimeException.class, () -> {
      try (var ignored = context($(FILENAME, "aFilename"))) {
        try (var ignored2 = context($(FILENAME, "bFilename"))) {
          exampleMethod();
        }
      }
    });
  }

  private void exampleMethod() {
    throw FILE_BROKEN.exception("Ooops");
  }
}
