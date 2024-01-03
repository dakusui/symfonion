package com.github.dakusui.symfonion.tests.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.subcommands.Help;
import com.github.dakusui.testutils.TestUtils;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;

import static com.github.dakusui.testutils.TestUtils.immediatelyClosingInputStream;
import static com.github.dakusui.testutils.TestUtils.outputCapturingPrintStream;
import static com.github.dakusui.testutils.midi.MidiTestUtils.assumeRequiredMidiDevicesPresent;
import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static com.github.dakusui.thincrest_pcond.experimentals.cursor.Cursors.findElements;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.containsString;

public class HelpTest {
  private PrintStream systemOut;

  @Before
  public void keepSystem_out() {
    this.systemOut = System.out;
  }
  @Test
  public void whenHelp_thenLooksOk() throws ParseException {
    assumeRequiredMidiDevicesPresent();

    TestUtils.OutputCapturingPrintStream out = outputCapturingPrintStream();
    System.setOut(out);
    new Help().invoke(new Cli.Builder().build(), out, immediatelyClosingInputStream());

    assertThat(out.toStringList(), findElements(
        containsString("usage: SYNTAX"),
        containsString("-c,--compile"),
        containsString("-V,--version")
    ));
  }

  @After
  public void restoreSystem_out() {
    System.setOut(this.systemOut);
  }
}
