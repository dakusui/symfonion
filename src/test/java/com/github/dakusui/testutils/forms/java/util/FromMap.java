package com.github.dakusui.testutils.forms.java.util;

import com.github.dakusui.testutils.forms.core.Transform;
import com.github.dakusui.testutils.forms.core.TransformingPredicateFactory;

import java.util.Map;

public enum FromMap {
  ;
  public static <K> TransformingPredicateFactory.ForList<K, Map<K, ?>> toKeyList() {
    return Transform.$(MapTo.keyList()).asList();
  }
}
