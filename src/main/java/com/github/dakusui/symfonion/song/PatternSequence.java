package com.github.dakusui.symfonion.song;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * //@formatter:off
 * //@formatter:on
 */
public interface PatternSequence extends List<Pattern> {
  class Impl extends LinkedList<Pattern> implements PatternSequence {
    Impl(Collection<Pattern> patterns) {
      super(patterns);
    }
  }

  static PatternSequence create(List<Pattern> patterns) {
    return new Impl(patterns);
  }
}
