package com.github.dakusui.symfonion.exceptions;

public class SymfonionRuntimeException extends RuntimeException {
  public SymfonionRuntimeException(String message) {
    super(message);
  }

  public SymfonionRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
