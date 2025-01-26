package com.github.dakusui.symfonion.song;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A sequence of patterns.
 *
 * @see Pattern
 */
public interface PatternSequence extends List<Pattern> {
  /**
   * An implementation of {@link PatternSequence} interface.
   */
  class Impl extends LinkedList<Pattern> implements PatternSequence {
    /**
     * Creates an object of this class.
     *
     * @param patterns Patterns for which a new objecet will be created.
     */
    Impl(Collection<Pattern> patterns) {
      super(patterns);
    }
  }

  /**
   * Creates a new {@link PatternSequence} instance from given `patterns`.
   *
   * @param patterns A list of patterns for which a new `PatternSequence` will be created.
   * @return A new `PatternSequence` object.
   */
  static PatternSequence create(List<Pattern> patterns) {
    return new Impl(patterns);
  }
}
