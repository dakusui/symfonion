package com.github.dakusui.thincrest_cliche.java.util;

import com.github.dakusui.thincrest_cliche.core.Transform;
import com.github.dakusui.thincrest_cliche.core.TransformingPredicateFactory;

import java.util.List;

public class FromList {
  public static TransformingPredicateFactory<Integer, List<?>> toSize() {
    return Transform.$(ListTo.size());
  }

  public static <T> TransformingPredicateFactory<T, List<T>> toElementAt(int i) {
    return Transform.$(ListTo.elementAt(i));
  }
}
