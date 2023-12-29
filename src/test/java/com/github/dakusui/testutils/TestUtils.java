package com.github.dakusui.testutils;

public enum TestUtils {
  ;
  
  public static String toHex(byte[] a) {
    StringBuilder sb = new StringBuilder(a.length * 2);
    for (byte b : a)
      sb.append(String.format("%02x", b));
    return sb.toString();
  }
  
  public static String name(String given, String when, String then) {
    return new TestCaseName(given, when, then).toString();
  }
}
