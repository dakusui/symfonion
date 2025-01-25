package com.github.dakusui.symfonion.cli;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.symfonion.testutils.TestBase;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static java.util.Arrays.asList;

/**
 * //@formatter:off
 * //@formatter:on
 */
@RunWith(JCUnit8.class)
public class CliBuilderTest extends TestBase {
  @ParameterSource
  public Parameter.Simple.Factory<String> versionOptions() {
    return Parameter.Simple.Factory.of(asList("-V", "--version"));
  }

  @Test
  public void test(@From("versionOptions") String versionOption) throws ParseException, IOException {
    Cli cli = Cli.cli(versionOption).build();
    System.out.println(cli);
    invoke(cli);
  }

  private static void invoke(Cli cli) throws IOException {
    CliIo io = CliIo.create();
    cli.subcommand().invoke(cli, io.stderr(), io.stdin());
  }
}
