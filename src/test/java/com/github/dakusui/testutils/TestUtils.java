package com.github.dakusui.testutils;

import java.io.*;
import java.nio.charset.StandardCharsets;

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

  public static File save(String data) {
    return save(data.getBytes(StandardCharsets.UTF_8));
  }

  public static File save(byte[] data) {
    File ret;
    try {
      ret = File.createTempFile("symfonion", "tmp");
      ret.deleteOnExit();
      try (OutputStream os = new BufferedOutputStream(new FileOutputStream(ret))) {
        os.write(data);
      }
      return ret;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
