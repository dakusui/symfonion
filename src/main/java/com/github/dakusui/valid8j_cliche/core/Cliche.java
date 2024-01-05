package com.github.dakusui.valid8j_cliche.core;

import com.github.dakusui.valid8j_pcond.fluent.Statement;

import java.util.function.Predicate;

public enum Cliche {
  ;

  public static <T> Statement<?> statement(T value, Predicate<T> cond) {
    return Statement.objectValue(value).then().checkWithPredicate(cond);
  }
}
