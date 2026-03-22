package com.github.dakusui.symfonion.tests.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.subcommands.Play;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PlayTest {
  @Test
  @Disabled
  public void whenPlay_thenLooksOk() throws ParseException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new Play().invoke(new Cli.Builder().build(), new PrintStream(out), System.in);
    String s = out.toString(UTF_8);
    System.out.println(s);
  }
}
