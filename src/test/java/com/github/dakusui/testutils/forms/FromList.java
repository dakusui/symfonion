package com.github.dakusui.testutils.forms;

import com.github.dakusui.thincrest_pcond.forms.Printables;

import java.util.List;
import java.util.function.Function;

public enum FromList {
  ;
  
  public static Function<List<?>, Integer> toSize() {
    return Printables.function("listSize", List::size);
  }
  
  public static <E> Function<List<E>, E> toElementAt(int i) {
    return Printables.function("listElementAt[" + i + "]", (List<E> list) -> list.get(i));
  }
}
