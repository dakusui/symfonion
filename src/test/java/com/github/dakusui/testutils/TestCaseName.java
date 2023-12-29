package com.github.dakusui.testutils;

public record TestCaseName(String given, String when, String then) {
  public String toString() {
    return String.format("given: '%s' when: '%s' then: '%s'", given, when, then);
  }
}
