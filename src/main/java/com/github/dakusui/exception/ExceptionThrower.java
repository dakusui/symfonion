package com.github.dakusui.exception;


import static com.github.dakusui.exception.ExceptionContext.*;

public interface ExceptionThrower<K extends Key> {
  ;

  default RuntimeException exception() {
    throw newException(message(currentContext()));
  }

  default RuntimeException exception(String message) {
    throw newException(message + ": " + message(currentContext()));
  }

  default RuntimeException exception(Throwable cause) {
    throw newException(message(currentContext()), cause);
  }

  default RuntimeException exception(String message, Throwable cause) {
    throw newException(message + ": " + message(currentContext()), cause);
  }

  default RuntimeException newException(String message) {
    throw newException(message, null);
  }

  RuntimeException newException(String message, Throwable cause);

  String message(ExceptionContext<K> context);

  private ExceptionContext<K> currentContext() {
    return contextManager().current();
  }

  ExceptionContext.Manager<K> contextManager();
}
