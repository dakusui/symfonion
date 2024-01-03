package com.github.dakusui.testutils.forms.core;

import com.github.dakusui.testutils.forms.java.util.stream.StreamTo;
import com.github.dakusui.thincrest_pcond.core.printable.PrintablePredicateFactory;
import com.github.dakusui.thincrest_pcond.experimentals.cursor.Cursors;
import com.github.dakusui.thincrest_pcond.forms.Predicates;

import java.awt.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.thincrest_pcond.core.printable.PrintablePredicateFactory.transform;
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

  public static class ForStream<E, O> extends TransformingPredicateFactory<Stream<E>, O> {
    public ForStream(PrintablePredicateFactory.TransformingPredicate.Factory<Stream<E>, O> base) {
      super(base);
    }

    public Predicate<O> anyMatch(Predicate<E> cond) {
      return check(Predicates.anyMatch(cond));
    }

    public Predicate<O> noneMatch(Predicate<E> cond) {
      return check(Predicates.noneMatch(cond));
    }
    public Predicate<O> allMatch(Predicate<E> cond) {
      return check(Predicates.allMatch(cond));
    }

    public Predicate<O> checkCount(Predicate<Long> cond) {
      return this.check(Predicates.<Stream<E>, Long>transform(StreamTo.count()).check(cond));
    }
  }

  public static class ForString<O> extends TransformingPredicateFactory<String, O> {

    public ForString(PrintablePredicateFactory.TransformingPredicate.Factory<String, O> base) {
      super(base);
    }

    public Predicate<O> matches(String regex) {
      return this.check(Predicates.matchesRegex(regex));
    }

    public Predicate<O> findSubstrings(String... substrings) {
      return this.check(Cursors.findSubstrings(substrings));
    }
  }
}
