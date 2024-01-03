package com.github.dakusui.testutils.forms.sut.symfonion;

import com.github.dakusui.testutils.forms.core.Transform;
import com.github.dakusui.testutils.forms.core.TransformingPredicateFactory;

import static com.github.dakusui.thincrest_pcond.forms.Functions.call;
import static com.github.dakusui.thincrest_pcond.forms.Functions.stringify;

public class ResultTo {
  public static TransformingPredicateFactory<Object, Object> exitCode() {
    return Transform.$(call("exitCode"));
  }

  public static TransformingPredicateFactory.ForString<Object> err() {
    return new TransformingPredicateFactory.ForString<>(Transform.$(call("err").andThen(stringify())));
  }

  public static TransformingPredicateFactory.ForString<Object> out() {
    return new TransformingPredicateFactory.ForString<>(Transform.$(call("out").andThen(stringify())));
  }
}
