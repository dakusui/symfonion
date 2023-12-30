package com.github.dakusui.symfonion.tests.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.subcommands.Version;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

public class VersionTest {

  @Test
  public void whenExecuteVersion_thenWorksFind() throws ParseException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new Version().invoke(new Cli(), new PrintStream(out));
    String s = out.toString(UTF_8);
    System.out.println(s);
  }

}
