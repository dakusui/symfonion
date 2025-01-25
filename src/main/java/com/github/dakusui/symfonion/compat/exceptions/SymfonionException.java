package com.github.dakusui.symfonion.compat.exceptions;

import java.io.File;
import java.io.Serial;

public class SymfonionException extends RuntimeException {
  private final File sourceFile;

  /**
   * A serial version uid.
   */
  @Serial
  private static final long serialVersionUID = -1999577216046615241L;

  public SymfonionException(String message, Throwable cause, File sourceFile) {
    super(message, cause);
    this.sourceFile = sourceFile;
  }

  public SymfonionException(String message, File sourceFile) {
    super(message);
    this.sourceFile = sourceFile;
  }

  public SymfonionException(Throwable cause, File sourceFile) {
    super(cause);
    this.sourceFile = sourceFile;
  }


  public File getSourceFile() {
    return sourceFile;
  }
}
