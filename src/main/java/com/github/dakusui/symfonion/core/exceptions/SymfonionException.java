package com.github.dakusui.symfonion.core.exceptions;

import java.io.File;

public class SymfonionException extends Exception {

  /**
   * A serial version uid.
   */
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
