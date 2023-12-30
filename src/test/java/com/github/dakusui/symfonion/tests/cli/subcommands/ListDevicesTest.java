package com.github.dakusui.symfonion.tests.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.subcommands.ListDevices;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ListDevicesTest {
  @Test
  public void whenListDevices_thenLooksOk() throws ParseException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new ListDevices().invoke(new Cli(), new PrintStream(out));
    String s = out.toString(UTF_8);
    System.out.println(s);
  }
}