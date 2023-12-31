package com.github.dakusui.symfonion.tests.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.subcommands.Help;
import com.github.dakusui.testutils.TestUtils;
import com.github.dakusui.thincrest_pcond.experimentals.cursor.Cursors;
import com.github.dakusui.thincrest_pcond.forms.Predicates;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;

import static com.github.dakusui.testutils.TestUtils.immediatelyClosingInputStream;
import static com.github.dakusui.testutils.TestUtils.outputCapturingPrintStream;
import static com.github.dakusui.thincrest.TestAssertions.assertThat;

public class HelpTest {
  private PrintStream systemOut;

  @Before
  public void keepSystem_out() {
    this.systemOut = System.out;
  }
  @Test
  public void whenHelp_thenLooksOk() throws ParseException {
    TestUtils.OutputCapturingPrintStream out = outputCapturingPrintStream();
    System.setOut(out);
    new Help().invoke(new Cli(), out, immediatelyClosingInputStream());

    assertThat(out.toStringList(), Cursors.findElements(
        Predicates.containsString("usage: SYNTAX"),
        Predicates.containsString("-c,--compile"),
        Predicates.containsString("-V,--version")
    ));
  }

  @After
  public void resotreSystem_out() {
    System.setOut(this.systemOut);
  }
}
