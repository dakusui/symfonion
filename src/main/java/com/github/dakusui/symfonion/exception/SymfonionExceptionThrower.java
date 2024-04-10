package com.github.dakusui.symfonion.exception;

import com.github.dakusui.exception.ExceptionContext;
import com.github.dakusui.exception.ExceptionThrower;
import com.github.dakusui.json.JsonPath;
import com.github.dakusui.json.JsonUtils;
import com.google.gson.JsonElement;

import static com.github.dakusui.symfonion.exception.SymfonionExceptionThrower.Key.*;

public enum SymfonionExceptionThrower implements ExceptionThrower<SymfonionExceptionThrower.Key> {
  FILE_BROKEN {
    @Override
    public String message(ExceptionContext<Key> context) {
      return "fileBroken:" + context.valueFor(FILENAME);
    }
  },
  JSON_TYPE_MISMATCH {
    @Override
    public String message(ExceptionContext<Key> context) {
      return String.format("""
          ----
          %s
          ----
          """/*,
          JsonUtils.summarizeJson(
              context.valueFor(ROOT_JSON_ELEMENT),
              context.valueFor(JSON_PATH)
          )*/);
    }
  },
  JSON_ARRAY_INDEX_OUT_OF_BOUNDS {
    @Override
    public String message(ExceptionContext<Key> context) {
      return "";
    }
  },
  UNEXPECTED_JSON_NULL {
    @Override
    public String message(ExceptionContext<Key> context) {
      return "";
    }
  }
  ;

  public enum Key implements ExceptionContext.Key {
    FILENAME(String.class),
    ROOT_JSON_ELEMENT(JsonElement.class),
    JSON_PATH(JsonPath.class),
    JSON_PATH_ELEMENT_INDEX(Integer.class),
    ;

    private final Class<?> expectedClass;

    Key(Class<?> expectedClass) {
      this.expectedClass = expectedClass;
    }

    @Override
    public Class<?> expectedClass() {
      return this.expectedClass;
    }
  }

  @Override
  public RuntimeException newException(String message, Throwable cause) {
    return new RuntimeException(message, cause);
  }

  @Override
  public ExceptionContext.Manager<SymfonionExceptionThrower.Key> contextManager() {
    return Utils.contextManager();
  }

  @SafeVarargs
  public static ExceptionContext<Key> context(ExceptionContext.Entry<Key>... entries) {
    return Utils.contextManager().open(entries);
  }

  public static ExceptionContext.Entry<Key> entry(Key key, Object value) {
    return ExceptionContext.entry(key, value);
  }

  public static ExceptionContext.Entry<Key> $(Key key, Object value) {
    return entry(key, value);
  }


  enum Utils {
    ;
    private static final ThreadLocal<ExceptionContext.Manager<SymfonionExceptionThrower.Key>> EXCEPTION_CONTEXT_MANAGER_THREAD_LOCAL = ThreadLocal.withInitial(ExceptionContext.Manager::new);

    static ExceptionContext.Manager<SymfonionExceptionThrower.Key> contextManager() {
      return EXCEPTION_CONTEXT_MANAGER_THREAD_LOCAL.get();
    }
  }
}
