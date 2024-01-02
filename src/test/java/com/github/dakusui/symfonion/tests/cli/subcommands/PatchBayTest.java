package com.github.dakusui.symfonion.tests.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.subcommands.PatchBay;
import com.github.dakusui.testutils.TestUtils;
import com.github.dakusui.testutils.midi.MidiTestUtils;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import javax.sound.midi.MidiSystem;
import java.io.IOException;
import java.util.Arrays;

import static com.github.dakusui.testutils.TestUtils.immediatelyClosingInputStream;
import static com.github.dakusui.testutils.midi.MidiTestUtils.assumeMidiDevicesPresent;
import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static com.github.dakusui.thincrest_pcond.experimentals.cursor.Cursors.findElements;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.containsString;

public class PatchBayTest {
  @Test
  public void whenPatchBay_thenOutputLooksOk() throws ParseException, IOException {
    assumeMidiDevicesPresent();

    TestUtils.OutputCapturingPrintStream out = TestUtils.outputCapturingPrintStream();
    new PatchBay().invoke(new Cli("-r", "in=out", "-I", "in=Real.*", "-O", "out=Gervill"), out, immediatelyClosingInputStream());

    for (String s: out.toStringList())
      System.err.println(s);

    assertThat(out.toStringList(), findElements(
        containsString("MIDI patch-bay mode"),
        containsString("closing transmitter"),
        containsString("closing receiver")));
  }
}
