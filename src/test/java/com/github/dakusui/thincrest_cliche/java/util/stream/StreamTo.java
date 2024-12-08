package com.github.dakusui.thincrest_cliche.java.util.stream;

import com.github.valid8j.pcond.forms.Printables;

import java.util.function.Function;
import java.util.stream.Stream;

public enum StreamTo {
  ;
  public static <E> Function<Stream<E>, Long> count() {
    return Printables.function("count", Stream::count);
  }
}
