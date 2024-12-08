package com.github.dakusui.exception;


import static com.github.dakusui.exception.ExceptionContext.Key;

/**
 * A class to throw exceptions with contextual and informative messages.
 *
 * Methods in this class never return and throw exceptions instead.
 * The return type of theirs is a "placeholder" and to enable programmers to let the compiler (linter) know a method stops there
 * by putting a `throw` statement.
 *
 * That is, you can do the following.
 *
 * [source, java]
 * ----
 * class Example {
 * void method() {
 * if (doSomething())
 * throw ExceptionThrower.exception();
 * }
 * }
 * ----
 *
 * Suppose if we have a method `void throwException()`,
 *
 * ----
 * if (somethingFails)
 * throwException();
 * nextStatement();
 * ----
 *
 * This confuses a compiler and static code analyzer so that it assumes `nextStatement()` may be executed.
 *
 * @param <K> Type of key to access `ExceptionContext`.
 * @see ExceptionContext
 */
public interface ExceptionThrower<K extends Key> {
  ;

  /**
   * Throws a `RuntimeException` with information from the current context.
   *
   * @return This method never returns.
   */
  default RuntimeException exception() {
    throw newException(message(currentContext()));
  }

  /**
   * Throws a `RuntimeException` with information from the current context following a given `message`.
   *
   * @param message A message in the exception
   * @return This method never returns.
   */
  default RuntimeException exception(String message) {
    throw newException(message + ": " + message(currentContext()));
  }

  /**
   * Throws a `RuntimeException` with information from the current context.
   * `cause` will be nested in the exception.
   *
   * @param cause A nested exception.
   * @return This method never returns.
   */
  default RuntimeException exception(Throwable cause) {
    throw newException(message(currentContext()), cause);
  }

  /**
   * Throws a `RuntimeException` with information from the current context following a given `message`.
   * `cause` will be nested in the exception.
   *
   * @param message A message in the exception
   * @param cause   A nested exception.
   * @return This method never returns.
   */
  default RuntimeException exception(String message, Throwable cause) {
    throw newException(message + ": " + message(currentContext()), cause);
  }

  /**
   * Creates an exception with a given `message`.
   * This method internally calls `newException(message, null)`.
   *
   * @param message A message set to the exception
   * @return This method never returns.
   */
  default RuntimeException newException(String message) {
    throw newException(message, null);
  }

  /**
   * Creates an exception with a given `message` and a `cause`.
   * `cause` is supposed to be nested in the created exception.
   * By overriding this method, you can make this object throw your custom exceptions.
   *
   * @param message A message string of the exception.
   * @param cause   A nested exception.
   * @return You can return the created exception but not obliged to
   */
  RuntimeException newException(String message, Throwable cause);

  /**
   * Composes a message string using contextual information stored in a given `context`.
   *
   * @param context A context that stores contextual information.
   * @return A composed message.
   */
  String message(ExceptionContext<K> context);

  /**
   * Returns a context manager of this object.
   *
   * @return A context manager of this object.
   * @see ExceptionContext
   * @see ExceptionContext.Manager
   */
  ExceptionContext.Manager<K> contextManager();

  /**
   * Returns a current context.
   *
   * @return A current context.
   */
  private ExceptionContext<K> currentContext() {
    return contextManager().current();
  }
}
