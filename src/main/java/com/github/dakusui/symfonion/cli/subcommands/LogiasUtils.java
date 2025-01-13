package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.logias.lisp.Context;

/**
 * //@formatter:off
 * //@formatter:on
 */
public enum LogiasUtils {
  ;

  public static Context createLogiasContext() {
    return Context.ROOT.createChild();
  }
}
