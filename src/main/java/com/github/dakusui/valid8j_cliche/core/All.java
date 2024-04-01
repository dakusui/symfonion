package com.github.dakusui.valid8j_cliche.core;

import com.github.dakusui.valid8j.Assertions;
import com.github.dakusui.valid8j_pcond.fluent.Statement;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A utility class for asserting statements.
 */
public enum All {
  ;

  /**
   * Checks if the all the given `statements` are satisfied.
   *
   * @param statements Statements to be asserted.
   * @return `true` if satisfied, `false` otherwise.
   */
  public static boolean $(Statement<?>... statements) {
    List<?> values = Arrays.stream(statements).map(Statement::statementValue).collect(toList());
    return Assertions.that(values, Statement.createPredicateForAllOf(statements));
  }
}
