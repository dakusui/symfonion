package com.github.dakusui.exception;

import com.github.dakusui.valid8j_cliche.core.All;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.dakusui.valid8j_pcond.fluent.Statement.objectValue;
import static java.util.Objects.requireNonNull;

public interface ExceptionContext extends Closeable {
  enum Key {
    FILENAME;
  }

  final class Entry {
    final Key key;
    final Object value;

    public Entry(Key key, Object value) {
      this.key = requireNonNull(key);
      this.value = value;
    }
  }

  default String fileBrokenMessage() {
    return "fileBroken";
  }

  Object valueFor(Key key);

  boolean hasValueFor(Key key);

  Optional<ExceptionContext> parent();

  @Override
  default void close() throws IOException {
  }

  static ExceptionContext newExceptionContext(Entry... entries) {
    return createExceptionContext(null, entries);
  }
  static ExceptionContext newExceptionContext(ExceptionContext parent, Entry... entries) {
    assert All.$(objectValue(parent).then().isNotNull());
    return createExceptionContext(parent, entries);
  }

  private static ExceptionContext createExceptionContext(ExceptionContext parent, Entry... entries) {
    Map<Key, Object> data = new HashMap<>();
    for (var entry : entries) {
      data.put(entry.key, entry.value);
    }
    return new ExceptionContext() {
      @Override
      public Object valueFor(Key key) {
        assert All.$(objectValue(key).then().isNotNull());
        return data.get(key);
      }

      @Override
      public boolean hasValueFor(Key key) {
        return data.containsKey(key) || parent().map(p -> hasValueFor(key)).orElse(false);
      }

      @Override
      public Optional<ExceptionContext> parent() {
        return Optional.ofNullable(parent);
      }
    };
  }

  static Entry entry(Key key, Object value) {
    return new Entry(key, value);
  }

  /**
   * A shorthand method for `entry(Key, Object)`.
   * Do `static import` to use this method for make your code look concise.
   *
   * @param key   A key of an entry.
   * @param value A value of an entry.
   * @return An entry.
   */
  static Entry $(Key key, Object value) {
    return entry(key, value);
  }
}
