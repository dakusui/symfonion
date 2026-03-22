package com.github.dakusui.symfonion.cli;

import com.github.dakusui.symfonion.testutils.TestBase;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

/**
 * //@formatter:off
 * //@formatter:on
 */
public class CliBuilderTest extends TestBase {
  @ParameterizedTest
  @ValueSource(strings = {"-V", "--version"})
  public void test(String versionOption) throws ParseException, IOException {
    Cli cli = Cli.cli(versionOption).build();
    System.out.println(cli);
    invoke(cli);
  }

  private static void invoke(Cli cli) throws IOException {
    CliIo io = CliIo.create();
    cli.subcommand().invoke(cli, io.stderr(), io.stdin());
  }
}
