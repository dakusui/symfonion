package com.github.dakusui.exception;

import com.github.dakusui.valid8j_cliche.core.Expectations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.dakusui.valid8j.Requires.require;
import static com.github.dakusui.valid8j.Requires.requireNonNull;
import static com.github.dakusui.valid8j_cliche.core.Expectations.that;
import static com.github.dakusui.valid8j_pcond.forms.Predicates.isInstanceOf;

public interface ExceptionContext<K extends ExceptionContext.Key> extends AutoCloseable {

  @SuppressWarnings("unchecked")
  default <T> T valueFor(K key) {
    assert Expectations.$(that(key).satisfies().isNotNull());
    Map<K, Object> data = data();
    return (T) (data.containsKey(key) ?
        data().get(key) :
        parent().map(p -> p.valueFor(key)).orElseGet(() -> {
          assert false : "No value for key:<" + key + ">";
          return null;
        }));
  }

  Optional<ExceptionContext<K>> parent();

  Map<K, Object> data();

  Manager<K> manager();

  @Override
  default void close() {
    this.manager().close(this);
  }


  static <K extends Key> Entry<K> entry(K key, Object value) {
    return new Entry<>(key, value);
  }

  /**
   * A shorthand method for `entry(Key, Object)`.
   * Do `static import` to use this method for make your code look concise.
   *
   * @param key   A key of an entry.
   * @param value A value of an entry.
   * @return An entry.
   */
  static <K extends Key> Entry<K> $(K key, Object value) {
    return entry(key, value);
  }

  interface Key {
    default Class<?> expectedClass() {
      return Object.class;
    }
  }

  record Entry<K extends Key>(K key, Object value) {
    public Entry(K key, Object value) {
      this.key = requireNonNull(key);
      this.value = require(value, isInstanceOf(this.key.expectedClass()));
    }
  }

  class Factory<K extends Key> {
    @SafeVarargs
    final ExceptionContext<K> create(Manager<K> manager, ExceptionContext<K> parent, Entry<K>... entries) {
      Map<K, Object> data = new HashMap<>();
      for (var entry : entries) {
        data.put(entry.key, entry.value);
      }
      return createContext(manager, parent, data);
    }

    protected ExceptionContext<K> createContext(Manager<K> manager, ExceptionContext<K> parent, Map<K, Object> data) {
      return new ExceptionContext<K>() {
        @Override
        public Optional<ExceptionContext<K>> parent() {
          return Optional.ofNullable(parent);
        }

        @Override
        public Map<K, Object> data() {
          return Collections.unmodifiableMap(data);
        }

        @Override
        public Manager<K> manager() {
          return manager;
        }
      };
    }
  }

  class Manager<K extends Key> {
    private final Factory<K> factory;
    private ExceptionContext<K> current = null;

    public Manager() {
      this(new Factory<>());
    }

    public Manager(Factory<K> factory) {
      assert Expectations.$(that(factory).satisfies().isNotNull());
      this.factory = factory;
    }

    @SafeVarargs
    public final ExceptionContext<K> open(Entry<K>... entries) {
      return this.current = this.factory.create(this, this.current, entries);
    }

    public void close(ExceptionContext<K> context) {
      assert Expectations.$(that(context).satisfies()
          .isNotNull()
          .isEqualTo(this.current));
      this.current = this.current.parent().orElse(null);
    }

    public ExceptionContext<K> current() {
      assert this.current != null;
      return this.current;
    }
  }
}
