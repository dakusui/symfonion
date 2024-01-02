package com.github.dakusui.testutils.forms;

import com.github.dakusui.thincrest_pcond.core.printable.PrintablePredicate;
import com.github.dakusui.thincrest_pcond.core.printable.PrintablePredicateFactory;
import com.github.dakusui.thincrest_pcond.forms.Predicates;

import java.util.function.Function;
import java.util.function.Predicate;

public enum Transform {
  ;
  public static <O, P> TransformingPredicateFactory<P, O> $(Function<O, P> func) {
    return new TransformingPredicateFactory<>(Predicates.transform(func));
  }
  
  public static <O, P> TransformingPredicateFactory<P, O> $(String name, Function<O, P> func) {
    return new TransformingPredicateFactory<>(Predicates.transform(name, func));
  }

}
