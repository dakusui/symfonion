package com.github.dakusui.symfonion.tests.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.subcommands.PatchBay;
import com.github.dakusui.symfonion.testutils.TestBase;
import com.github.dakusui.testutils.TestUtils;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.IOException;

import static com.github.dakusui.testutils.TestUtils.immediatelyClosingInputStream;
import static com.github.dakusui.testutils.midi.MidiTestUtils.assumeRequiredMidiDevicesPresent;
import static com.github.valid8j.classic.TestAssertions.assertThat;
import static com.github.valid8j.pcond.experimentals.cursor.Cursors.findElements;
import static com.github.valid8j.pcond.forms.Predicates.containsString;

public class PatchBayTest extends TestBase {
  @Test
  public void whenPatchBay_thenOutputLooksOk() throws ParseException, IOException {
    assumeRequiredMidiDevicesPresent();

    TestUtils.OutputCapturingPrintStream out = TestUtils.outputCapturingPrintStream();
    new PatchBay().invoke(new Cli.Builder("-r", "in=out", "-I", "in=Real.*", "-O", "out=Gervill").build(), out, immediatelyClosingInputStream());

    for (String s : out.toStringList())
      System.err.println(s);

    assertThat(out.toStringList(), findElements(
        containsString("MIDI patch-bay mode"),
        containsString("closing transmitter"),
        containsString("closing receiver")));
  }
}
