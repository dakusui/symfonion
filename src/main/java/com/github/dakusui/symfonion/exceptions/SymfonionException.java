package com.github.dakusui.symfonion.exceptions;

import java.io.File;
import java.io.Serial;

public class SymfonionException extends RuntimeException {

  /**
   * A serial version uid.
   */
  @Serial
  private static final long serialVersionUID = -1999577216046615241L;

  public SymfonionException(String message, Throwable cause) {
    super(message, cause);
  }

  public SymfonionException(String message) {
    super(message);
  }

  public SymfonionException(Throwable cause) {
    super(cause);
  }

  private File sourceFile = null;

  public File getSourceFile() {
    return sourceFile;
  }


  public void setSourceFile(File sourceFile) {
    this.sourceFile = sourceFile;
  }

}
