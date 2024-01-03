package com.github.dakusui.testutils.forms.java.util.stream;

import com.github.dakusui.thincrest_pcond.forms.Printables;

import java.util.function.Function;
import java.util.stream.Stream;

public enum StreamTo {
  ;
  public static <E> Function<Stream<E>, Long> count() {
    return Printables.function("count", Stream::count);
  }
}
