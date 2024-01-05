package com.github.dakusui.thincrest_cliche.java.util;

import com.github.dakusui.thincrest_pcond.forms.Printables;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public enum MapTo {
  ;
  
  public static <K, V> Function<Map<K, V>, List<K>> keyList() {
    return Printables.function("keyList", map -> map.keySet().stream().toList());
  }
}
