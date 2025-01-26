package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.logias.lisp.Context;

/**
 * A utility class to access the **Logias** library, which builds and executes S-expressions using **JSON**.
 */
public enum LogiasUtils {
  ;

  /**
   * Creates a `Context` object of **Logias** library.
   *
   * @return A new context object.
   */
  public static Context createLogiasContext() {
    return Context.ROOT.createChild();
  }
}
