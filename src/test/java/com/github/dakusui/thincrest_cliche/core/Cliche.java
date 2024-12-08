package com.github.dakusui.thincrest_cliche.core;

import com.github.valid8j.pcond.forms.Printables;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public enum Cliche {
  ;
  
  public static <K, V> Function<Map<K, V>, Set<K>> keySet() {
    return Printables.function("keySet", Map::keySet);
  }


  public static <T> Predicate<T> message(Supplier<String> message, Predicate<T> p) {
    return Printables.predicate(message, p);
  }
}
