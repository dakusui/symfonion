package com.github.dakusui.valid8j_cliche.core;

import com.github.dakusui.valid8j_pcond.fluent.Statement;
import com.github.dakusui.valid8j_pcond.validator.Validator;

import java.util.function.Function;
import java.util.function.Predicate;

public enum Cliche {
  ;

  public static <T> Statement<?> statement(T value, Predicate<T> cond) {
    return Statement.objectValue(value).then().checkWithPredicate(cond);
  }

  public static <T, E extends Throwable> T validate(T value, Predicate<? super T> cond, Function<String, Throwable> exceptionFactory) throws E {
    return Validator.instance().require(value, cond, exceptionFactory);
  }
}
