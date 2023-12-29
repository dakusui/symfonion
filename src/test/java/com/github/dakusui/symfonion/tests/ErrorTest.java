package com.github.dakusui.symfonion.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.core.exceptions.CLIException;
import org.apache.commons.cli.ParseException;
import org.junit.Before;

import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.core.Utils;

public class ErrorTest {
  private File workFile;
  
  File load(String resourceName) throws IOException, SymfonionException {
    String content = Utils.loadResource(resourceName);
    File ret = this.getWorkFile();
    PrintStream ps = new PrintStream(ret);
    try {
      ps.print(content);
    } finally {
      ps.close();
    }
    
    return ret;
  }
  
  @Before
  public void createWorkFile() throws IOException {
    this.workFile = File.createTempFile("symfonion-test", "js");
  }
  
  protected String invokeWithResource(String resourceName) throws IOException, SymfonionException {
    ByteArrayOutputStream baos;
    PrintStream ps = new PrintStream(baos = new ByteArrayOutputStream());
    this.workFile = load(resourceName);
    Cli.invoke(ps, ps, "-c", workFile.getAbsolutePath());
    return baos.toString();
  }
  
  protected String fmt(String fmt) {
    return String.format(fmt, getWorkFile());
  }
  
  public File getWorkFile() {
    return this.workFile;
  }
}
