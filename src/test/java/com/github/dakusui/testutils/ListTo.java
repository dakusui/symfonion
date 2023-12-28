package com.github.dakusui.testutils;

import com.github.dakusui.thincrest_pcond.forms.Printables;

import java.util.List;
import java.util.function.Function;

public enum ListTo {
  ;
  
  public static Function<List<?>, Integer> size() {
    return Printables.function("listSize", List::size);
  }
  
  public static <E> Function<List<E>, E> elementAt(int i) {
    return Printables.function("listElementAt[" + i + "]", (List<E> list) -> list.get(i));
  }
}
