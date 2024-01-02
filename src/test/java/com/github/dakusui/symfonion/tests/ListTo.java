package com.github.dakusui.symfonion.tests;

import com.github.dakusui.testutils.forms.FromList;
import com.github.dakusui.testutils.forms.Transform;
import com.github.dakusui.testutils.forms.TransformingPredicateFactory;

import java.util.List;

public class ListTo {
  public static TransformingPredicateFactory<Integer, List<?>> size() {
    return Transform.$(FromList.toSize());
  }

  public static <T> TransformingPredicateFactory<T, List<T>> elementAt(int i) {
    return Transform.$(FromList.toElementAt(i));
  }
}
