package com.github.dakusui.symfonion.testutils;

import java.io.*;
import java.util.Objects;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.thincrest_cliche.core.AllOf;
import com.github.dakusui.thincrest_cliche.core.Transform;
import org.junit.Before;

import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.utils.Utils;

import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static com.github.dakusui.thincrest_pcond.forms.Functions.call;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.*;

public class CliTestBase extends TestBase {
  public record Result(int exitCode, String out, String err) {
    @Override
    public String toString() {
      return String.format("""
          EXIT_CODE:
          %s
          STDOUT:
          %s
                    
          STDERR:
          %s
          """,
          insertOneWhiteSpaceBeforeEveryLine(Objects.toString(this.exitCode())),
          insertOneWhiteSpaceBeforeEveryLine(this.out()),
          insertOneWhiteSpaceBeforeEveryLine(this.err()));
    }

    private static String insertOneWhiteSpaceBeforeEveryLine(String message) {
      return " " + message.replaceAll("\n", "\n ");
    }
  }

  private File workFile;

  public File writeResourceToTempFile(String resourceName) throws IOException, SymfonionException {
    return writeContentToTempFile(Utils.loadResource(resourceName));
  }

  public File writeContentToTempFile(String content) throws FileNotFoundException {
    File ret = this.getWorkFile();
    try (PrintStream ps = new PrintStream(ret)) {
      ps.print(content);
    }
    return ret;
  }

  @Before
  public void createWorkFile() throws IOException {
    this.workFile = File.createTempFile("symfonion-test", ".json");
  }

  protected Result compileResourceWithCli(String resourceName) throws IOException, SymfonionException {
    this.workFile = writeResourceToTempFile(resourceName);
    return invokeCliWithArguments("-c", workFile.getAbsolutePath());
  }

  protected Result invokeCliWithArguments(String... args) {
    ByteArrayOutputStream stdout, stderr;
    PrintStream ps1 = new PrintStream(stdout = new ByteArrayOutputStream());
    PrintStream ps2 = new PrintStream(stderr = new ByteArrayOutputStream());
    int exitCode = Cli.invoke(ps1, ps2, args);
    return new Result(exitCode, stdout.toString(), stderr.toString());
  }

  protected String fmt(String fmt) {
    return String.format(fmt, getWorkFile());
  }

  public File getWorkFile() {
    return this.workFile;
  }

  public static void assertActualObjectToStringValueContainsExpectedString(String expected, Result actual) {
    System.err.println(actual.toString());
    assertThat(
        actual,
        AllOf.$(
            Transform.<Object, String>$(call("toString")).check(containsString(expected)),
            Transform.$(call("exitCode")).check(not(isEqualTo(0)))));
  }
}
