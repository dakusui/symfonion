package com.github.dakusui.thincrest_cliche.core;

import com.github.dakusui.thincrest_pcond.forms.Predicates;

import java.util.function.Predicate;

public enum AllOf {
  ;
  
  @SafeVarargs
  public static <T> Predicate<T> $(Predicate<? super T>... predicates) {
    return Predicates.allOf(predicates);
  }
}
