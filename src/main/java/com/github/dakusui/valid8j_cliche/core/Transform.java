package com.github.dakusui.valid8j_cliche.core;

import com.github.dakusui.valid8j_pcond.core.printable.PrintablePredicateFactory;
import com.github.dakusui.valid8j_pcond.forms.Predicates;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Function;

public enum Transform {
  ;

  public static <I, O> PrintablePredicateFactory.TransformingPredicate.Factory<O, I> $(Function<I, O> function) {
    return Predicates.transform(function);
  }
}
