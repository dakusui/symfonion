package com.github.dakusui.exception;

import com.github.dakusui.valid8j_cliche.core.Expectations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.valid8j.classic.Requires.require;
import static com.github.valid8j.classic.Requires.requireNonNull;
import static com.github.valid8j.fluent.Expectations.that;
import static com.github.valid8j.pcond.forms.Predicates.isInstanceOf;


/**
 * An interface that models a context that handles exceptions.
 * This provides a mechanism to print meaningful exception messages utilizing contextual information.
 *
 * `K` should be a subclass of an `Enum`, but users can use non-subclass of it at the risk of their own.a
 *
 * @param <K> Type of key in this context.
 */
public interface ExceptionContext<K extends ExceptionContext.Key> extends AutoCloseable {

  /**
   * Returns a value for `key`.
   * `K` should extend an {@link Enum} and therefore, a key which doesn't have a mapped value must not be looked up.
   * When looked up, behavior is not defined and `AssertionError` may be thrown.
   *
   * @param key A key whose value should be looked up by this method.
   * @param <T> Type of the value to be returned.
   * @return A value for `key`.
   */
  @SuppressWarnings("unchecked")
  default <T> T valueFor(K key) {
    assert Expectations.$(that(key).satisfies().notNull());
    Map<K, Object> data = data();
    return (T) (data.containsKey(key) ? data().get(key) : parent().map(p -> p.valueFor(key)).orElseGet(() -> {
      assert false : "No value for key:<" + key + ">";
      return null;
    }));
  }

  /**
   * Returns a parent of this object, if any.
   *
   * @return A parent context of this object.
   */
  Optional<ExceptionContext<K>> parent();

  /**
   * Returns entries in the current Context.
   *
   * @return A map that holds entries in the current Context.
   */
  Map<K, Object> data();

  /**
   * Returns a manager of this object.
   *
   * @return A manager object.
   */
  Manager<K> manager();

  @Override
  default void close() {
    this.manager().close(this);
  }


  /**
   * Creates and returns an `Entry` object.
   *
   * @param key   A key of an entry
   * @param value A value of an entry
   * @param <K>   Type of key.
   * @return An entry object.
   * @see Entry
   */
  static <K extends Key> Entry<K> entry(K key, Object value) {
    return new Entry<>(key, value);
  }

  /**
   * A shorthand method for `entry(Key, Object)`.
   * Do `static import` to use this method to make your code look concise.
   *
   * @param key   A key of an entry.
   * @param value A value of an entry.
   * @param <K>   Type of key
   * @return An entry.
   * @see Entry
   */
  static <K extends Key> Entry<K> $(K key, Object value) {
    return entry(key, value);
  }

  /**
   * An interface for keys to be used for `ExceptionContext`.
   *
   * @see ExceptionContext
   */
  interface Key {
    /**
     * Returns a class expected for a value to be associated with this key.
     * @return Expected class for the value.
     */
    default Class<?> expectedClass() {
      return Object.class;
    }
  }

  /**
   * An entry that can be registered in the `ExceptionContext`.
   *
   * @param key   A key of an entry.
   * @param value A value of an entry.
   * @param <K>   Type of key.
   */
  record Entry<K extends Key>(K key, Object value) {
    /**
     * Creates a new entry.
     *
     * @param key of the entry.
     * @param value Associated with the `key`.
     */
    public Entry(K key, Object value) {
      this.key = requireNonNull(key);
      this.value = require(value, isInstanceOf(this.key.expectedClass()));
    }
  }

  /**
   * A factory of an {@link ExceptionContext} object.
   *
   * @param <K> Type of key.
   */
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
      return new ExceptionContext<>() {
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

  /**
   * A manager class of an {@link ExceptionContext}.
   *
   * @param <K> Type of key used by exception contexts managed under this object.
   */
  class Manager<K extends Key> {
    private final Factory<K> factory;
    private ExceptionContext<K> current = null;

    /**
     * Creates an object of this class.
     * This internally calls {@link Manager#Manager(Factory)} constructor with a new `Factory` object.
     *
     * @see Factory
     */
    public Manager() {
      this(new Factory<>());
    }

    /**
     * Creates an object of this class using a given `factory`.
     *
     * @param factory A factory with which this object creates a new context.
     */
    public Manager(Factory<K> factory) {
      assert Expectations.$(that(factory).satisfies().notNull());
      this.factory = factory;
    }

    /**
     * Creates a new exception context as a child of the current one.
     *
     * @param entries Entries to be added to the new child context.
     * @return A new context.
     */
    @SafeVarargs
    public final ExceptionContext<K> open(Entry<K>... entries) {
      return this.current = this.factory.create(this, this.current, entries);
    }

    /**
     * Closes a given `context`.
     * The current context will be set to a parent of the current one, if any.
     *
     * @param context A context to be closed
     */
    public void close(ExceptionContext<K> context) {
      assert Expectations.$(that(context).satisfies().notNull().equalTo(this.current));
      this.current = this.current.parent().orElse(null);
    }

    /**
     * Returns a current context.
     *
     * @return A current context.
     */
    public ExceptionContext<K> current() {
      assert this.current != null;
      return this.current;
    }
  }
}
