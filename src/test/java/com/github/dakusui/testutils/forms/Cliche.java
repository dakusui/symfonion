package com.github.dakusui.testutils.forms;

import com.github.dakusui.thincrest_pcond.core.printable.PrintableFunctionFactory;
import com.github.dakusui.thincrest_pcond.forms.Printables;

import java.util.*;
import java.util.function.Function;

public enum Cliche {
  ;
  
  public static <K, V> Function<Map<K, V>, Set<K>> keySet() {
    return Printables.function("keySet", Map::keySet);
  }
}
