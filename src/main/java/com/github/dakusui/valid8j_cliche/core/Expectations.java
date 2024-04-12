package com.github.dakusui.valid8j_cliche.core;

import com.github.dakusui.valid8j.Assertions;
import com.github.dakusui.valid8j.ValidationFluents;
import com.github.dakusui.valid8j_pcond.core.fluent.AbstractObjectTransformer;
import com.github.dakusui.valid8j_pcond.core.fluent.Checker;
import com.github.dakusui.valid8j_pcond.core.fluent.Matcher;
import com.github.dakusui.valid8j_pcond.core.fluent.Transformer;
import com.github.dakusui.valid8j_pcond.core.fluent.builtins.*;
import com.github.dakusui.valid8j_pcond.fluent.Statement;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dakusui.valid8j_pcond.fluent.Statement.*;
import static com.github.dakusui.valid8j_pcond.internals.InternalUtils.trivialIdentityFunction;
import static java.util.stream.Collectors.toList;

/**
 * A facade class for the "fluent" style programming model of `valid8j` library.
 *
 * .General Usage
 * ----
 * ((assert (all|
 *          $|
 *          precondition(s)?|
 *          invariant(s)?|
 *          postcondition(s)?))|
 *   assertAll|
 *   assertStatement|
 *   require|
 *   hold|
 *   ensure|
 *   requireArgument(s)?|
 *   requireState(s)?)
 *     (that satisfies predicate)|
 *     (satisfies predicate)|
 *     statement
 * ----
 */
public enum Expectations {
  ;

  /**
   * Checks if all the given `statements` are satisfied.
   *
   * Purpose:: General
   * Intended Style:: Overhead-Free Assertion
   *
   * Otherwise, this method throws an exception whose message describes what happened (how expectations are not satisfied.)
   * This method is supposed to be used with `assert` statement of Java and when:
   *
   * - yor are checking invariant conditions in DbC ("Design by Contract") approach.
   * - you are not interested in DbC approach.
   *
   *
   * @param statements Statements to be asserted.
   * @return `true` if all the statements are satisfied.
   */
  public static boolean all(Statement<?>... statements) {
    List<?> values = Arrays.stream(statements).map(Statement::statementValue).collect(toList());
    return Assertions.that(values, Statement.createPredicateForAllOf(statements));
  }

  /**
   * A "singular" version of {@link Expectations#all(Statement[])}.
   *
   * Purpose:: General
   * Intended Style:: Overhead-Free Assertion
   *
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
   *
   * Purpose:: DbC
   * Intended Style:: Overhead-Free Assertion
   *
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
   * A "singular" version of {@link Expectations#preconditions(Statement[])}.
   *
   * Purpose:: DbC
   * Intended Style:: Overhead-Free Assertion
   *
   * Use this method if you only have one statement to be asserted.
   * This method is supposed to be used with `assert` statement of Java and when yor are checking a precondition in DbC ("Design by Contract") approach.
   *
   * @param statement to be evaluated by `assert` statement of Java.
   * @return `true` if all the given statements are satisfied.
   * @see Expectations#preconditions(Statement[])
   */
  public static boolean precondition(Statement<?> statement) {
    return preconditions(statement);
  }

  /**
   * Checks if the all the given `statements` are satisfied.
   *
   * Purpose:: DbC
   * Intended Style:: Overhead-Free Assertion
   *
   * Otherwise, this method throws an exception whose message describes what happened (how expectations are not satisfied.)
   * This method is supposed to be used with `assert` statement of Java and when yor are checking preconditions in DbC ("Design by Contract") approach.
   *
   * @param statements Statements to be asserted.
   * @return `true` if all the statements are satisfied.
   */
  public static boolean invariants(Statement<?>... statements) {
    List<?> values = Arrays.stream(statements).map(Statement::statementValue).collect(toList());
    return Assertions.that(values, Statement.createPredicateForAllOf(statements));
  }

  /**
   * A singular version of {@link Expectations#preconditions(Statement[])}.
   *
   * Purpose:: DbC
   * Intended Style:: Overhead-Free Assertion
   *
   * Use this method if you only have one statement to be asserted.
   * This method is supposed to be used with `assert` statement of Java and when yor are checking a precondition in DbC ("Design by Contract") approach.
   *
   * @param statement to be evaluated by `assert` statement of Java.
   * @return `true` if all the given statements are satisfied.
   * @see Expectations#preconditions(Statement[])
   */
  public static boolean invariant(Statement<?> statement) {
    return invariants(statement);
  }

  /**
   * Checks if all the given `statements` are satisfied.
   *
   * Purpose:: DbC
   * Intended Style:: Overhead-Free Assertion
   *
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
   * A singular version of {@link Expectations#postconditions(Statement[])}.
   *
   * Purpose:: DbC
   * Intended Style:: Overhead-Free Assertion
   *
   * Use this method if you only have one statement to be asserted.
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
   * Purpose:: Value-checking, DbC
   * Intended Style:: "Guava"-like Check
   *
   * @param statements Preconditions to be checked.
   */
  public static void require(Statement<?>... statements) {
    ValidationFluents.requireAll(statements);
  }

  /**
   * A singular version of {@link Expectations#require(Statement[])}.
   *
   * Purpose:: Value-checking, DbC
   * Intended Style:: "Guava"-like Check
   *
   * @param statement A precondition to be checked.
   */
  public static <T> T require(Statement<T> statement) {
    ValidationFluents.requireAll(statement);
    return statement.statementValue();
  }

  /**
   * A method to check the given `statements` as invariant conditions.
   *
   * Purpose:: DbC
   * Intended Style:: "Guava"-like Check
   *
   * @param statements Invariant conditions.
   */
  public static void retain(Statement<?>... statements) {
    ValidationFluents.all(statements);
  }

  /**
   * A singular version of {@link Expectations#retain(Statement[])}.
   *
   * @param statement An invariant condition.
   */
  public static <T> T retain(Statement<T> statement) {
    ValidationFluents.all(statement);
    return statement.statementValue();
  }

  /**
   * A method to check the given `statements` as post-conditions.
   *
   * @param statements post-conditions.
   */
  public static void ensure(Statement<?>... statements) {
    ValidationFluents.ensureAll(statements);
  }

  /**
   * A singular version of {@link Expectations#ensure(Statement[])}.
   *
   * @param statement An invariant condition.
   */
  public static <T> T ensure(Statement<T> statement) {
    ValidationFluents.ensureAll(statement);
    return statement.statementValue();
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
    return that(value, Statement::shortValue);
  }

  public static DoubleTransformer<Double> that(double value) {
    return that(value, Statement::doubleValue);
  }

  public static FloatTransformer<Float> that(float value) {
    return that(value, Statement::floatValue);
  }

  public static BooleanTransformer<Boolean> that(boolean value) {
    return that(value, Statement::booleanValue);
  }

  public static <T, V extends Checker<V, T, T>> V satisfies(T value, Function<T, V> checkerFactory) {
    return new LocalTransformer<>(() -> value, checkerFactory).satisfies();
  }

  public static <T> ObjectChecker<T, T> satisfies(T value) {
    return satisfies(value, v -> objectValue(v).satisfies());
  }

  public static <T> ListChecker<List<T>, T> satisfies(List<T> value) {
    return satisfies(value, v -> listValue(v).satisfies());
  }

  public static <T> StreamChecker<Stream<T>, T> satisfies(Stream<T> value) {
    return satisfies(value, v -> streamValue(v).satisfies());
  }

  public static StringChecker<String> satisfies(String value) {
    return satisfies(value, v -> stringValue(v).satisfies());
  }

  public static IntegerChecker<Integer> satisfies(int value) {
    return satisfies(value, v -> integerValue(v).satisfies());
  }

  public static LongChecker<Long> satisfies(long value) {
    return satisfies(value, v -> longValue(v).satisfies());
  }

  public static ShortChecker<Short> satisfies(short value) {
    return satisfies(value, v -> shortValue(v).satisfies());
  }

  public static DoubleChecker<Double> satisfies(double value) {
    return satisfies(value, v -> doubleValue(v).satisfies());
  }

  public static FloatChecker<Float> satisfies(float value) {
    return satisfies(value, v -> floatValue(v).satisfies());
  }

  public static BooleanChecker<Boolean> satisfies(boolean value) {
    return satisfies(value, v -> booleanValue(v).satisfies());
  }


  public static <T> Statement<T> statement(T value, Predicate<T> cond) {
    return satisfies(value).predicate(cond);
  }

  public static <T> Statement<List<T>> statement(List<T> value, Predicate<List<T>> cond) {
    return satisfies(value).predicate(cond);
  }

  public static <T> Statement<Stream<T>> statement(Stream<T> value, Predicate<Stream<T>> cond) {
    return satisfies(value).predicate(cond);
  }

  public static Statement<String> statement(String value, Predicate<String> cond) {
    return satisfies(value).predicate(cond);
  }

  public static Statement<Integer> statement(int value, Predicate<Integer> cond) {
    return satisfies(value).predicate(cond);
  }

  public static Statement<Long> statement(long value, Predicate<Long> cond) {
    return satisfies(value).predicate(cond);
  }

  public static Statement<Short> statement(short value, Predicate<Short> cond) {
    return satisfies(value).predicate(cond);
  }

  public static Statement<Double> statement(double value, Predicate<Double> cond) {
    return satisfies(value).predicate(cond);
  }

  public static Statement<Float> statement(float value, Predicate<Float> cond) {
    return satisfies(value).predicate(cond);
  }

  public static Statement<Boolean> statement(boolean value, Predicate<Boolean> cond) {
    return satisfies(value).predicate(cond);
  }

  private static class LocalTransformer<V extends Checker<V, T, T>, T> extends AbstractObjectTransformer.Base<LocalTransformer<V, T>, V, T, T> {
    private final Function<T, V> checkerFactory;

    protected LocalTransformer(Supplier<T> value, Function<T, V> checkerFactory) {
      super(value, trivialIdentityFunction());
      this.checkerFactory = checkerFactory;
    }

    @Override
    protected V toChecker(Function<T, T> transformFunction) {
      return checkerFactory.apply(transformFunction.apply(this.baseValue()));
    }

    @Override
    protected Matcher<?, T, T> rebase() {
      return new LocalTransformer<>(this::value, checkerFactory);
    }
  }
}
