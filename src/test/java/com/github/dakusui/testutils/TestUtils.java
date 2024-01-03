package com.github.dakusui.testutils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public enum TestUtils {
  ;

  public static final PrintStream NOP    = new PrintStream(new OutputStream() {
    @Override
    public void write(int b) {
    }
  });
  static final PrintStream STDOUT = System.out;
  static final        PrintStream STDERR = System.err;

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

  public static InputStream immediatelyClosingInputStream() {
    return new InputStream() {
      @Override
      public int read() {
        return -1;
      }
    };
  }

  public static OutputCapturingPrintStream outputCapturingPrintStream() {
    return new OutputCapturingPrintStream();
  }

  /**
   * Typically called from a method annotated with {@literal @}{@code Before} method.
   */
  public static void suppressStdOutErrIfUnderPitestOrSurefire() {
    if (isRunUnderPitest() || isRunUnderSurefire()) {
      System.setOut(NOP);
      System.setErr(NOP);
    }
  }

  /**
   * Typically called from a method annotated with {@literal @}{@code After} method.
   */
  public static void restoreStdOutErr() {
    System.setOut(STDOUT);
    System.setErr(STDERR);
  }

  public static boolean isRunUnderSurefire() {
    return System.getProperty("surefire.real.class.path") != null;
  }

  public static boolean isRunUnderPitest() {
    return Objects.equals(System.getProperty("underpitest"), "yes");
  }

  public static class OutputCapturingPrintStream extends PrintStream {
    private final ByteArrayOutputStream baos;

    public OutputCapturingPrintStream() {
      super(new ByteArrayOutputStream());
      this.baos = (ByteArrayOutputStream) this.out;
    }

    public byte[] toByteArray() {
      return this.baos.toByteArray();
    }

    public String toString(Charset charset) {
      return new String(toByteArray(), charset);
    }

    @Override
    public String toString() {
      return this.toString(UTF_8);
    }

    public List<String> toStringList(Charset charset) {
      return Arrays.asList(this.toString(charset).split("\n"));
    }

    public List<String> toStringList() {
      return toStringList(UTF_8);
    }
  }
}
