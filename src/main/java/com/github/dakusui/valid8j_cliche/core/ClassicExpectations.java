package com.github.dakusui.valid8j_cliche.core;

import com.github.dakusui.valid8j_pcond.validator.Validator;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An entry-point for "plain-predicate" style programming model of `valid8j` library.
 */
public enum ClassicExpectations {
  ;

  /**
   * A general purpose value validation method.
   *
   * @param value A value to be validated.
   * @param cond A condition with which the `value` should be validated.
   * @param exceptionFactory A function that creates an exception a failure.
   * @return The validated value.
   * @param <T> The type of the `value`.
   * @param <E> The type of exception to be thrown.
   * @throws E Thrown if `cond.test(value)` returns `false`.
   */
  public static <T, E extends Throwable> T validate(T value, Predicate<? super T> cond, Function<String, Throwable> exceptionFactory) throws E {
    return Validator.instance().require(value, cond, exceptionFactory);
  }

  /**
   * A method to check a given `value` satisfies a condition `predicate`, to be verified by the test.
   * If it is not satisfied, the test should fail.
   *
   * @param value     The value to be checked.
   * @param predicate A condition to check the `value`.
   * @param <T>       The type of the `value`.
   */
  public static <T> void assertThat(T value, Predicate<? super T> predicate) {
    Validator.instance().assertThat(value, predicate);
  }

  /**
   * A method to check a given `value` satisfies a condition `predicate`, which is required by the *test's design* to execute it.
   * If it is not satisfied, that means, the value violates an assumption of the test, therefore the test should be ignored, not fail.
   * If you are using *JUnit4*, an `AssumptionViolatedException` should be thrown.
   *
   * @param value     The value to be checked.
   * @param predicate A condition to check the `value`.
   * @param <T>       The type of the `value`.
   */
  public static <T> void assumeThat(T value, Predicate<? super T> predicate) {
    Validator.instance().assumeThat(value, predicate);
  }
}
