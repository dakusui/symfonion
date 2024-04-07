package com.github.dakusui.valid8j_cliche.core;

import com.github.dakusui.valid8j.Assertions;
import com.github.dakusui.valid8j_pcond.core.fluent.builtins.*;
import com.github.dakusui.valid8j_pcond.fluent.Statement;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.valid8j_pcond.fluent.Statement.*;
import static java.util.stream.Collectors.toList;

/**
 * A utility class for asserting statements.
 */
public enum Expectations {
  ;

  /**
   * Checks if the all the given `statements` are satisfied.
   * Otherwise, this method throws an exception whose message describes what happened (how expectations are not satisfied.)
   *
   * @param statements Statements to be asserted.
   * @return `true` if all the statements are satisfied.
   */
  public static boolean all(Statement<?>... statements) {
    List<?> values = Arrays.stream(statements).map(Statement::statementValue).collect(toList());
    return Assertions.that(values, Statement.createPredicateForAllOf(statements));
  }

  /**
   * A shorthand method of {@link Expectations#all(Statement[])}.
   *
   * @param statements to be evaluated by `assert` statement of Java.
   * @return `true` if all the given statements are satisfied.
   * @see Expectations#all(Statement[])
   */
  public static boolean $(Statement<?>... statements) {
    return all(statements);
  }

  public static <T> ObjectTransformer<T, T> that(T value) {
    return objectValue(value);
  }

  public static <T> ListTransformer<List<T>, T> that(List<T> value) {
    return listValue(value);
  }

  public static <T> StreamTransformer<Stream<T>, T> that(Stream<T> value) {
    return streamValue(value);
  }

  public static StringTransformer<String> that(String value) {
    return stringValue(value);
  }

  public static IntegerTransformer<Integer> that(int value) {
    return integerValue(value);
  }

  public static LongTransformer<Long> that(long value) {
    return longValue(value);
  }
  public static ShortTransformer<Short> that(short value) {
    return shortValue(value);
  }
  public static DoubleTransformer<Double> that(double value) {
    return doubleValue(value);
  }
  public static FloatTransformer<Float> that(float value) {
    return floatValue(value);
  }

  public static BooleanTransformer<Boolean> that(boolean value) {
    return booleanValue(value);
  }
}
