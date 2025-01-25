package com.github.dakusui.symfonion.cli;

import com.github.dakusui.symfonion.compat.exceptions.CliException;
import com.github.dakusui.symfonion.testutils.TestBase;
import org.apache.commons.cli.CommandLine;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.github.valid8j.fluent.Expectations.*;

/**
 * //@formatter:off
 * //@formatter:on
 */
public class CliTest extends TestBase {
  PrintStream printStream(ByteArrayOutputStream stream) {
    return new PrintStream(stream);
  }

  @Test
  public void givenUnrecognizedOption_whenInvoke_thenExitCode1() {
    CliIo  io       = CliIo.create();
    int    exitCode = Cli.invoke(io.stdout(), io.stderr(), "-Q", "xyz");
    String stderr   = io.stderrAsString();
    String stdout   = io.stdoutAsString();

    assertAll(value(exitCode).toBe().equalTo(1),
              value(stdout).toBe().empty(),
              value(stderr).toBe().containing("Unrecognized option: -Q"));
  }

  @Test
  public void givenArgumentMissingOption_whenInvoke_thenExitCode1() {
    CliIo io       = CliIo.create();
    int   exitCode = Cli.invoke(io.stdout(), io.stderr(), "-q");

    String stderr = io.stderrAsString();
    String stdout = io.stdoutAsString();

    assertAll(value(exitCode).toBe().equalTo(1),
              value(stdout).toBe().empty(),
              value(stderr).toBe().containing("Missing argument for option: q"));
  }

  @Test
  public void givenValidDevicePatterns_whenParseSpecifiedOptions_thenParsed() {
    CommandLine commandLine = CliTestUtils.createCommandLine("-I", "k1", "dev1", "-I", "k2", "dev2");
    System.out.println(Cli.parseSpecifiedOptionAsPortNamePatterns(commandLine, "I"));
  }

  @Test(expected = CliException.class)
  public void givenInvalidDevicePatterns_whenParseSpecifiedOptions_thenErrorThrown() {
    CommandLine commandLine = CliTestUtils.createCommandLine("-I", "k1", "\\");
    try {
      System.out.println(Cli.parseSpecifiedOptionAsPortNamePatterns(commandLine, "I"));
    } catch (CliException e) {
      assertStatement(value(e.getMessage()).toBe()
                                           .containing("-I")
                                           .containing("Regular expression")
                                           .containing("'k1'")
                                           .containing("'\\'")
                                           .containing("isn't valid"));

      throw e;
    }
  }
}
