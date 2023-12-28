package com.github.dakusui.testutils;

import com.github.dakusui.thincrest_pcond.core.printable.PrintablePredicateFactory;
import com.github.dakusui.thincrest_pcond.forms.Predicates;

import java.util.function.Function;

public enum Transform {
  ;
  public static <O, P> PrintablePredicateFactory.TransformingPredicate.Factory<P, O> $(Function<O, P> func) {
    return Predicates.transform(func);
  }
  
  public static <O, P> PrintablePredicateFactory.TransformingPredicate.Factory<P, O> $(String name, Function<O, P> func) {
    return Predicates.transform(name, func);
  }
}
