package com.github.dakusui.symfonion.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * //@formatter:off
 * //@formatter:on
 */
public record CliIo(ByteArrayOutputStream stdoutBuffer, ByteArrayOutputStream stderrBuffer, String stdinData) {
  static CliIo create() {
    return new CliIo(new ByteArrayOutputStream(), new ByteArrayOutputStream(), "");
  }

  PrintStream stdout() {
    return new PrintStream(stdoutBuffer);
  }

  String stdoutAsString() {
    return stdoutBuffer().toString(StandardCharsets.UTF_8);
  }

  PrintStream stderr() {
    return new PrintStream(stderrBuffer);
  }

  String stderrAsString() {
    return stderrBuffer().toString(StandardCharsets.UTF_8);
  }


  InputStream stdin() {
    return new ByteArrayInputStream(stdinData.getBytes(StandardCharsets.UTF_8));
  }


}
