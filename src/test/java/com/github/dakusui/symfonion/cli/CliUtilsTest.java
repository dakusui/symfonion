package com.github.dakusui.symfonion.cli;

import com.github.dakusui.symfonion.compat.exceptions.CliException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.File;

import static com.github.dakusui.symfonion.cli.Cli.parseArgs;
import static com.github.valid8j.fluent.Expectations.assertStatement;
import static com.github.valid8j.fluent.Expectations.value;

/**
 * //@formatter:off
 * //@formatter:on
 */
public class CliUtilsTest {

  @Test
  public void givenShortOption_whenComposeErrorMessage_thenMessageComposed() {
    String message = CliUtils.composeErrMsgForShortOption("Error found", "o");

    value(message)
        .toBe()
        .containing("Error found")
        .containing("-o");
  }

  @Test
  public void givenOption_whenComposeErrorMessage_thenMessageComposed() {
    String message = CliUtils.composeErrMsgForOption("Error found", "o", "output");

    assertStatement(value(message)
                        .toBe()
                        .containing("Error found")
                        .containing("-o")
                        .containing("--output"));
  }

  @Test
  public void givenCommandLine_whenGetSingleOptionValueFromCommandLine_thenValueReturned() {
    String optionValue = CliUtils.getSingleOptionValueFromCommandLine(createCommandLine("-q", "song.json"), "q");

    assertStatement(value(optionValue).toBe().equalTo("song.json"));
  }

  @Test
  public void givenCommandLineWithLongOptionName_whenGetSingleOptionValueFromCommandLine_thenValueReturned() {
    String optionValue = CliUtils.getSingleOptionValueFromCommandLine(createCommandLine("--play-song", "song.json"), "q");

    assertStatement(value(optionValue).toBe().equalTo("song.json"));
  }

  @Test
  public void givenCommandLine_whenGetSingleOptionValueFromCommandLineWithLongOptionName_thenValueReturned() {
    String optionValue = CliUtils.getSingleOptionValueFromCommandLine(createCommandLine("-q", "song.json"), "play-song");

    assertStatement(value(optionValue).toBe().equalTo("song.json"));
  }

  @Test(expected = CliException.class)
  public void givenCommandLineGivingOutputOptionTwice_whenGetSingleOptionValueFromCommandLine_thenExceptionThrown() {
    try {
      CliUtils.getSingleOptionValueFromCommandLine(createCommandLine("-q", "play.json", "--play-song", "extra.txt"), "q");
    } catch (CliException e) {
      assertStatement(value(e.getMessage()).toBe()
                                           .containing("-q")
                                           .containing("one and only one"));
      throw e;
    }
  }

  @Test
  public void givenNullForPortName_whenComposeOutputFile_thenFileComposedFromGivenName() {
    File f = CliUtils.composeOutputFile("outfile.txt", null);

    assertStatement(value(f).asObject()
                            .stringify()
                            .toBe()
                            .containing("outfile.txt"));
  }

  @Test
  public void givenDefaultPortName_whenComposeOutputFile_thenFileComposedFromGivenName() {
    File f = CliUtils.composeOutputFile("outfile.txt", "$default");

    assertStatement(value(f).asObject()
                            .stringify()
                            .toBe()
                            .containing("outfile.txt"));
  }

  @Test
  public void givenExplicitPortName_whenComposeOutputFile_thenFileComposedFromGivenNameAndPortNam() {
    File f = CliUtils.composeOutputFile("outfile", "port1");

    assertStatement(value(f).asObject()
                            .stringify()
                            .toBe()
                            .containing("outfile.port1"));
  }

  @Test
  public void givenExplicitPortNameAndOutfileWithExtension_whenComposeOutputFile_thenFileComposedFromGivenNameAndPortNam() {
    File f = CliUtils.composeOutputFile("outfile.midi", "port1");

    assertStatement(value(f).asObject()
                            .stringify()
                            .toBe()
                            .endingWith("outfile.port1.midi"));
  }
  static CommandLine createCommandLine(String... args) {
    try {
      return parseArgs(createOptions(), args);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  static Options createOptions() {
    return Cli.buildOptions();
  }
}
