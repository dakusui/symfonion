package com.github.dakusui.symfonion.compat.exceptions;

import java.io.Closeable;
import java.util.HashMap;

import static com.github.valid8j.fluent.Expectations.*;

public class ExceptionContext implements Closeable {
  private final ExceptionContext                                   parent;
  private final HashMap<CompatExceptionThrower.ContextKey, Object> values = HashMap.newHashMap(100);

  private ExceptionContext(ExceptionContext parent) {
    this.parent = parent;
  }

  public ExceptionContext() {
    this.parent = null;
  }

  public static ExceptionContextEntry entry(CompatExceptionThrower.ContextKey key, Object value) {
    assert preconditions(value(key).then().notNull().$(),
                         value(value).then().notNull().instanceOf(CompatExceptionThrower.classOfValueFor(key)).$());
    return new ExceptionContextEntry(key, value);
  }

  public ExceptionContext set(CompatExceptionThrower.ContextKey key, Object value) {
    assert preconditions(
        value(key).then().notNull().$(),
        value(value).then().notNull().instanceOf(key.type()).$());
    this.values.put(key, value);
    return this;
  }

  public ExceptionContext parent() {
    return this.parent;
  }

  /**
   *
   */
  @SuppressWarnings({"unchecked"})
  <T> T get(CompatExceptionThrower.ContextKey key) {
    assert preconditions(value(key).then().notNull().$());
    // In production, we do not want to produce a NullPointerException, even if the key is null.
    // Just return null in such a situation.
    if (key == null)
      return null;
    if (!this.values.containsKey(key)) {
      assert invariant(value(this).invoke("parent").then().notNull());
      // In production, we do not want to produce a NullPointerException, even if a value associated with the key doesn't exist.
      // Just return null, in such a situation.
      if (this.parent() == null)
        return null;
      return this.parent().get(key);
    }
    return (T) this.values.get(key);
  }


  @Override
  public void close() {
    CompatExceptionThrower.context.set(this.parent);
  }

  ExceptionContext createChild() {
    return new ExceptionContext(this);
  }
}
