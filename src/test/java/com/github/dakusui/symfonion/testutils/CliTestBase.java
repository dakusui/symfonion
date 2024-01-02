package com.github.dakusui.symfonion.testutils;

import java.io.*;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.testutils.forms.Transform;
import org.junit.Before;

import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.utils.Utils;

import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static com.github.dakusui.thincrest_pcond.forms.Functions.call;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.containsString;

public class CliTestBase {
  public record StdOutErr(String out, String err) {
    @Override
    public String toString() {
      return String.format("""
          STDOUT:
          %s
          
          STDERR:
          %s
          """, this.out(), this.err());
    }
  }
  private File workFile;
  
  File writeResourceToTempFile(String resourceName) throws IOException, SymfonionException {
    return writeContentToTempFile(Utils.loadResource(resourceName));
  }

  File writeContentToTempFile(String content) throws FileNotFoundException {
    File ret = this.getWorkFile();
    try (PrintStream ps = new PrintStream(ret)) {
      ps.print(content);
    }
    return ret;
  }

  @Before
  public void createWorkFile() throws IOException {
    this.workFile = File.createTempFile("symfonion-test", "json");
  }
  
  protected StdOutErr compileResourceWithCli(String resourceName) throws IOException, SymfonionException {
    this.workFile = writeResourceToTempFile(resourceName);
    return invokeCliWithResource("-c", workFile.getAbsolutePath());
  }

  protected StdOutErr invokeCliWithResource(String... args) {
    ByteArrayOutputStream stdout, stderr;
    PrintStream ps1 = new PrintStream(stdout = new ByteArrayOutputStream());
    PrintStream ps2 = new PrintStream(stderr = new ByteArrayOutputStream());
    Cli.invoke(ps1, ps2, args);
    return new StdOutErr(stdout.toString(), stderr.toString());
  }

  protected String fmt(String fmt) {
    return String.format(fmt, getWorkFile());
  }
  
  public File getWorkFile() {
    return this.workFile;
  }

  public static void assertActualObjectToStringValueContainsExpectedString(String expected, Object actual) {
    assertThat(
        actual,
        Transform.<Object, String>$(call("toString")).check(containsString(expected)));
  }
}
