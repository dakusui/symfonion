package com.github.dakusui.symfonion.tests.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.CliRecord;
import com.github.dakusui.symfonion.cli.subcommands.ListDevices;
import com.github.dakusui.symfonion.testutils.CliTestBase;
import com.github.dakusui.symfonion.testutils.TestBase;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ListDevicesTest extends TestBase {
  @Test
  public void whenListDevices_thenLooksOk() throws ParseException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new ListDevices().invoke(new CliRecord.Builder().build(), new PrintStream(out), System.in);
    String s = out.toString(UTF_8);
    System.out.println(s);
  }
}
