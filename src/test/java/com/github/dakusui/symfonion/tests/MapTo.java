package com.github.dakusui.symfonion.tests;

import com.github.dakusui.testutils.forms.FromMap;
import com.github.dakusui.testutils.forms.Transform;
import com.github.dakusui.testutils.forms.TransformingPredicateFactory;

import java.util.Map;

public enum MapTo {
  ;
  public static <K> TransformingPredicateFactory.ForList<K, Map<K, ?>> keyList() {
    return Transform.$(FromMap.toKeyList()).asList();
  }
}
