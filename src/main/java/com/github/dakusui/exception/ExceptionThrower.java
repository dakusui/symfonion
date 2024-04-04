package com.github.dakusui.exception;


import java.util.function.Function;
import java.util.function.Supplier;

public enum ExceptionThrower {
  ;
  public static final ThreadLocal<ExceptionContext> EXCEPTION_CONTEXT_THREAD_LOCAL = ThreadLocal.withInitial(ExceptionContext::newExceptionContext);

  static ExceptionContext exceptionContext() {
    return EXCEPTION_CONTEXT_THREAD_LOCAL.get();
  }

  static <E extends RuntimeException> E fileBroken() {
    throw exception(exceptionContext()::fileBrokenMessage, RuntimeException::new);
  }
  static <E extends RuntimeException> E exception(Supplier<String> messageComposer, Function<String, E> exceptionComposer) {
    throw exceptionComposer.apply(messageComposer.get());
  }
}
