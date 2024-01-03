package com.github.dakusui.testutils.forms.java.util;

import com.github.dakusui.testutils.forms.core.Transform;
import com.github.dakusui.testutils.forms.core.TransformingPredicateFactory;

import java.util.List;

public class FromList {
  public static TransformingPredicateFactory<Integer, List<?>> toSize() {
    return Transform.$(ListTo.size());
  }

  public static <T> TransformingPredicateFactory<T, List<T>> toElementAt(int i) {
    return Transform.$(ListTo.elementAt(i));
  }
}
