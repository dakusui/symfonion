package com.github.dakusui.testutils.forms;

import com.github.dakusui.thincrest_pcond.forms.Printables;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public enum FromMap {
  ;
  
  public static <K, V> Function<Map<K, V>, List<K>> toKeyList() {
    return Printables.function("keyList", map -> map.keySet().stream().toList());
  }
}
