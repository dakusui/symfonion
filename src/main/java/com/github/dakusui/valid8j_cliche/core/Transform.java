package com.github.dakusui.valid8j_cliche.core;

import com.github.valid8j.pcond.core.printable.PrintablePredicateFactory;
import com.github.valid8j.pcond.forms.Predicates;

import java.util.function.Function;

public enum Transform {
  ;

  public static <I, O> PrintablePredicateFactory.TransformingPredicate.Factory<O, I> $(Function<I, O> function) {
    return Predicates.transform(function);
  }
}
