package com.github.dakusui.testutils;

import com.github.dakusui.thincrest_pcond.core.printable.PrintableFunctionFactory;
import com.github.dakusui.thincrest_pcond.forms.Printables;

import java.util.*;
import java.util.function.Function;

public enum Cliche {
  ;
  
  public static <K, V> Function<Map<K, V>, Set<K>> keySet() {
    return Printables.function("keySet", Map::keySet);
  }
  
  public static <E, C extends Collection<E>> Function<C, Integer> collectionSize() {
    return PrintableFunctionFactory.Simple.SIZE.instance();
  }
  
  public static <E> Function<Collection<E>, List<E>> collectionToList() {
    return Printables.function("toList", ArrayList::new);
  }
}
