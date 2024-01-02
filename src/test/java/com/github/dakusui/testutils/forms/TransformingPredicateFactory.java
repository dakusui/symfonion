package com.github.dakusui.testutils.forms;

import com.github.dakusui.thincrest_pcond.core.printable.PrintablePredicateFactory;
import com.github.dakusui.valid8j_pcond.forms.Predicates;

import java.util.List;
import java.util.function.Predicate;

import static com.github.dakusui.valid8j.Requires.requireNonNull;

public class TransformingPredicateFactory<P, O> extends PrintablePredicateFactory.TransformingPredicate.Factory<P, O> {
  final PrintablePredicateFactory.TransformingPredicate.Factory<P, O> base;

  public TransformingPredicateFactory(PrintablePredicateFactory.TransformingPredicate.Factory<P, O> base) {
    this.base = requireNonNull(base);
  }

  @Override
  public Predicate<O> check(String condName, Predicate<? super P> cond) {
    return base.check(condName, cond);
  }

  @Override
  public Predicate<O> check(Predicate<? super P> cond) {
    return base.check(cond);
  }


  public Predicate<O> isEqualTo(P value) {
    return check(Predicates.isEqualTo(value));
  }

  public Predicate<O> isNotNull() {
    return check(Predicates.isNotNull());
  }

  @SuppressWarnings("unchecked")
  public <E, OO> TransformingPredicateFactory.ForList<E, OO> asList() {
    return new TransformingPredicateFactory.ForList<>((TransformingPredicateFactory<List<E>, OO>)this);
  }


  public static class ForList<E, O> extends TransformingPredicateFactory<List<E>, O> {
    public ForList(PrintablePredicateFactory.TransformingPredicate.Factory<List<E>, O> base) {
      super(base);
    }

    public Predicate<O> isEmpty() {
      return check(Predicates.isEmpty());
    }
  }
}
