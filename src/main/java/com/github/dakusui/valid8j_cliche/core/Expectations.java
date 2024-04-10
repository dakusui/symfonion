package com.github.dakusui.valid8j_cliche.core;

import com.github.dakusui.valid8j.Assertions;
import com.github.dakusui.valid8j.Requires;
import com.github.dakusui.valid8j.ValidationFluents;
import com.github.dakusui.valid8j_pcond.core.fluent.Checker;
import com.github.dakusui.valid8j_pcond.core.fluent.Transformer;
import com.github.dakusui.valid8j_pcond.core.fluent.builtins.*;
import com.github.dakusui.valid8j_pcond.fluent.Statement;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.valid8j_pcond.fluent.Statement.*;
import static java.util.stream.Collectors.toList;

/**
 * A facade class for  `pcond` library.
 */
public enum Expectations {
  ;

  /**
   * Checks if the all the given `statements` are satisfied.
   * Otherwise, this method throws an exception whose message describes what happened (how expectations are not satisfied.)
   * This method is supposed to be used with `assert` statement of Java and when:
   *
   * - yor are checking invariant conditions in DbC ("Design by Contract") approach.
   * - you are not interested in DbC approach.
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
   * Prefer this method if you only have one statement to be asserted.
   * This method is supposed to be used with `assert` statement of Java and when:
   *
   * - yor are checking invariant conditions in DbC ("Design by Contract") approach.
   * - you are not interested in DbC approach.
   *
   * @param statements to be evaluated by `assert` statement of Java.
   * @return `true` if all the given statements are satisfied.
   * @see Expectations#all(Statement[])
   */
  public static boolean $(Statement<?>... statements) {
    return all(statements);
  }

  /**
   * Checks if the all the given `statements` are satisfied.
   * Otherwise, this method throws an exception whose message describes what happened (how expectations are not satisfied.)
   * This method is supposed to be used with `assert` statement of Java and when yor are checking preconditions in DbC ("Design by Contract") approach.
   *
   * @param statements Statements to be asserted.
   * @return `true` if all the statements are satisfied.
   */
  public static boolean preconditions(Statement<?>... statements) {
    List<?> values = Arrays.stream(statements).map(Statement::statementValue).collect(toList());
    return Assertions.precondition(values, Statement.createPredicateForAllOf(statements));
  }

  /**
   * A shorthand method of {@link Expectations#preconditions(Statement[])}.
   * Use this method if you only have one statement to be asserted.
   * This method is supposed to be used with `assert` statement of Java and when:
   * This method is supposed to be used with `assert` statement of Java and when yor are checking a precondition in DbC ("Design by Contract") approach.
   *
   * @param statement to be evaluated by `assert` statement of Java.
   * @return `true` if all the given statements are satisfied.
   * @see Expectations#preconditions(Statement[])
   */
  public static boolean precondition(Statement<?> statement) {
    return all(statement);
  }

  /**
   * Checks if the all the given `statements` are satisfied.
   * Otherwise, this method throws an exception whose message describes what happened (how expectations are not satisfied.)
   * This method is supposed to be used with `assert` statement of Java and when yor are checking post-conditions in DbC ("Design by Contract") approach.
   *
   * @param statements Statements to be asserted.
   * @return `true` if all the statements are satisfied.
   */
  public static boolean postconditions(Statement<?>... statements) {
    List<?> values = Arrays.stream(statements).map(Statement::statementValue).collect(toList());
    return Assertions.postcondition(values, Statement.createPredicateForAllOf(statements));
  }

  /**
   * A shorthand method of {@link Expectations#preconditions(Statement[])}.
   * Use this method if you only have one statement to be asserted.
   * This method is supposed to be used with `assert` statement of Java and when:
   * This method is supposed to be used with `assert` statement of Java and when yor are checking a post-condition in DbC ("Design by Contract") approach.
   *
   * @param statement to be evaluated by `assert` statement of Java.
   * @return `true` if all the given statements are satisfied.
   * @see Expectations#postconditions(Statement[])
   */
  public static boolean postcondition(Statement<?> statement) {
    return all(statement);
  }
  /**
   * A method to check the given `statements` as preconditions.
   *
   * @param statements preconditions to be checked.
   */
  public static void require(Statement<?>... statements) {
    ValidationFluents.requireAll(statements);
  }

  /**
   * A method to check the given `statements` as invariant conditions.
   *
   * @param statements invariant conditions.
   */
  public static void retain(Statement<?>... statements) {
    ValidationFluents.all(statements);
  }

  /**
   * A method to check the given `statements` as post-conditions.
   *
   * @param statements post-conditions.
   */
  public static void ensure(Statement<?>... statements) {
    ValidationFluents.ensureAll(statements);
  }

  public static <T, TX extends Transformer<TX, V, T, T>, V extends Checker<V, T, T>> TX that(T value, Function<T, TX> transformer) {
    return transformer.apply(value);
  }

  public static <T> ObjectTransformer<T, T> that(T value) {
    return that(value, Statement::objectValue);
  }

  public static <T> ListTransformer<List<T>, T> that(List<T> value) {
    return that(value, Statement::listValue);
  }

  public static <T> StreamTransformer<Stream<T>, T> that(Stream<T> value) {
    return that(value, Statement::streamValue);
  }

  public static StringTransformer<String> that(String value) {
    return that(value, Statement::stringValue);
  }

  public static IntegerTransformer<Integer> that(int value) {
    return that(value, Statement::integerValue);
  }

  public static LongTransformer<Long> that(long value) {
    return that(value, Statement::longValue);
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
