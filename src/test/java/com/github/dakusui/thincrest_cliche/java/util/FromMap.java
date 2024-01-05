package com.github.dakusui.thincrest_cliche.java.util;

import com.github.dakusui.thincrest_cliche.core.Transform;
import com.github.dakusui.thincrest_cliche.core.TransformingPredicateFactory;

import java.util.Map;

public enum FromMap {
  ;
  public static <K> TransformingPredicateFactory.ForList<K, Map<K, ?>> toKeyList() {
    return Transform.$(MapTo.keyList()).asList();
  }
}
