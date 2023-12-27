package com.github.dakusui.symfonion.scenarios;

import com.github.dakusui.thincrest_pcond.core.printable.PrintableFunctionFactory;
import com.github.dakusui.thincrest_pcond.forms.Printables;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public enum Cliche {
  ;
  
  static <K, V> Function<Map<K, V>, Set<K>> keySet() {
    return Printables.function("keySet", Map::keySet);
  }
  
  public static <T> T value() {
    return null;
  }
  
  public static <E, C extends Collection<E>> Function<C, Integer> collectionSize() {
    return PrintableFunctionFactory.Simple.SIZE.instance();
  }
}
