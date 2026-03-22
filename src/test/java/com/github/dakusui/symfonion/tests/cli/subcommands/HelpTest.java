package com.github.dakusui.symfonion.tests.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.subcommands.Help;
import com.github.dakusui.testutils.TestUtils;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static com.github.dakusui.testutils.TestUtils.immediatelyClosingInputStream;
import static com.github.dakusui.testutils.TestUtils.outputCapturingPrintStream;
import static com.github.dakusui.testutils.midi.MidiTestUtils.assumeRequiredMidiDevicesPresent;
import static com.github.valid8j.classic.TestAssertions.assertThat;
import static com.github.valid8j.pcond.experimentals.cursor.Cursors.findElements;
import static com.github.valid8j.pcond.forms.Predicates.containsString;

public class HelpTest {
  private PrintStream systemOut;

  @BeforeEach
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

  @AfterEach
  public void restoreSystem_out() {
    System.setOut(this.systemOut);
  }
}
