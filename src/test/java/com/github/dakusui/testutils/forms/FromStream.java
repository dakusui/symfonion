package com.github.dakusui.testutils.forms;

import com.github.dakusui.thincrest_pcond.forms.Printables;

import java.util.function.Function;
import java.util.stream.Stream;

public enum FromStream {
  ;
  public static <E> Function<Stream<E>, Long> count() {
    return Printables.function("count", Stream::count);
  }
}
