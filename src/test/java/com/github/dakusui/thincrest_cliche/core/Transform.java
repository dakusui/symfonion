package com.github.dakusui.thincrest_cliche.core;

import com.github.dakusui.thincrest_pcond.forms.Predicates;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public enum Transform {
  ;
  public static <O, P> TransformingPredicateFactory<P, O> $(Function<O, P> func) {
    return new TransformingPredicateFactory<>(Predicates.transform(func));
  }
  
  public static <O, P> TransformingPredicateFactory<P, O> $(String name, Function<O, P> func) {
    return new TransformingPredicateFactory<>(Predicates.transform(name, func));
  }

  public static <O, E> TransformingPredicateFactory.ForList<E, O> toListBy(Function<O, List<E>> func) {
    return new TransformingPredicateFactory.ForList<>($(func));
  }

  public static <O, E> TransformingPredicateFactory.ForStream<E, O> toStreamBy(Function<O, Stream<E>> func) {
    return new TransformingPredicateFactory.ForStream<>($(func));
  }

}
