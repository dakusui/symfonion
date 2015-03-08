package com.github.dakusui.symfonion.core;

public class SymfonionRuntimeException extends RuntimeException {
  public SymfonionRuntimeException(String message) {
    super(message);
  }

  public SymfonionRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
