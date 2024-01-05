package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.testutils.CliTestBase;
import com.github.dakusui.symfonion.testutils.json.StrokeBuilder;
import com.github.dakusui.symfonion.testutils.json.SymfonionJsonTestUtils;
import com.github.dakusui.thincrest_cliche.core.AllOf;
import com.github.dakusui.thincrest_cliche.sut.symfonion.ResultTo;
import com.google.gson.JsonObject;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Objects;

import static com.github.dakusui.symfonion.testutils.json.SymfonionJsonTestUtils.sixteenBeatsGroove;
import static com.github.dakusui.testutils.TestUtils.isRunUnderPitest;
import static com.github.dakusui.testutils.json.JsonTestUtils.*;
import static com.github.dakusui.testutils.midi.MidiTestUtils.assumeRequiredMidiDevicesPresent;
import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static com.github.dakusui.thincrest.TestAssertions.assumeThat;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.isFalse;

public class CliSmokeTest extends CliTestBase {
  /**
   * Generates three strokes, each of which is a chord. Em, Dm, and C, respectively.
   * This test plays a generated midi, and you will hear it from your speaker.
   *
   * @throws FileNotFoundException This shouldn't be thrown.
   */
  @Test
  public void givenThreeStrokes_whenPlaySubcommandIsInvoked_thenPlayed() throws FileNotFoundException {
    assumeRequiredMidiDevicesPresent();
    assumeThat(isRunUnderPitest(), isFalse());
    JsonObject song = object(
        $("$settings", object()),
        $("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1")))))),
        $("$patterns", object(
            $("R4", object($("$body", json("r4")))),
            $("main", object($("$body", array(json("BGE8;r8;AFD8;r8;GEC8;r8"))))))),
        $("$grooves", object($("16beats", sixteenBeatsGroove()))),
        $("$sequence", array(
            object(
                $("$beats", json("4/4")),
                $("$patterns", object($("piano", array("R4"))))),
            object(
                $("$beats", json("16/4")),
                $("$patterns", object($("piano", array("main")))),
                $("$groove", json("16beats"))
            ))));

    Result result = invokeCliWithArguments("-p", writeContentToTempFile(Objects.toString(song)).getAbsolutePath(), "-Oport1=Gervill");

    System.err.println("[source]");
    System.err.println("----");
    System.err.println(result);
    System.err.println("----");

    assertThat(
        result,
        AllOf.$(
            ResultTo.exitCode().isEqualTo(0),
            ResultTo.out().findSubstrings("*", "Gervill", "Real Time Sequencer")));
  }

  @Ignore
  @Test
  public void givenThreeStrokes_whenPlaySubcommandIsInvoked_thenPlayed2() throws FileNotFoundException {
    assumeRequiredMidiDevicesPresent();
    assumeThat(isRunUnderPitest(), isFalse());
    JsonObject song = object(
        $("$settings", object()),
        $("$parts", object(
            $("piano", object($("$channel", json(0)), $("$port", json("port1")))),
            $("guitar", object($("$channel", json(1)), $("$port", json("port1")))),
            $("drums", object($("$channel", json(9)), $("$port", json("port2"))))
        )),
        $("$patterns", object(
            $("R4", object($("$body", json("r4")))),
            $("pgchg-piano", object($("$body", json("r16")), $("$program", json(1)))),
            $("pgchg-guitar", object($("$body", json("r16")), $("$program", json(12)))),
            $("main", object($("$body", array(json("BGE4;AFD4;GEC4"))))),
            $("sub", object(
                $("$body", array(json("BGE8;BGE8;AFD8;AFD8;GEC8;GEC8"))))),
            $("drum-1", object(
                $("$notemap", json("$percussion")),
                $("$body", array(json("BH8;H8;BSH8;H8;BH8;H8;BSH8;H8;")))))
        )),
        $("$grooves", object($("16beats", sixteenBeatsGroove()))),
        $("$sequence", array(
            object(
                $("$beats", json("4/4")),
                $("$patterns", object(
                    $("piano", array("R4", "pgchg-piano")),
                    $("guitar", array("R4", "pgchg-piano"))))),
            object(
                $("$beats", json("16/4")),
                $("$patterns", object(
                    $("piano", array("main")),
                    $("guitar", array("sub")),
                    $("drums", array("drum-1"))
                )),
                $("$groove", json("16beats"))
            ))));

    Result result = invokeCliWithArguments("-p", writeContentToTempFile(Objects.toString(song)).getAbsolutePath(), "-Oport1=hw:1,0,0", "-Oport2=hw:1,1,0");

    System.err.println("[source]");
    System.err.println("----");
    System.err.println(result);
    System.err.println("----");

    assertThat(
        result,
        AllOf.$(
            ResultTo.exitCode().isEqualTo(0),
            ResultTo.out().findSubstrings("*", "Gervill", "Real Time Sequencer")));
  }

  @Test
  public void givenArrayedVolumeHavingBrokenDotsSyntax_whenCompileThroughCli_thenErrorMessageLooksOkay() throws FileNotFoundException {
    assumeThat(isRunUnderPitest(), isFalse());
    JsonObject song = SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
        "port2", array(new StrokeBuilder().notes("C4").volume(array(10, ".X.", 100)).build()), SymfonionJsonTestUtils.sixteenBeatsGroove());


    Result result = invokeCliWithArguments("-c", writeContentToTempFile(Objects.toString(song)).getAbsolutePath());

    System.err.println("[source]");
    System.err.println("----");
    System.err.println(result);
    System.err.println("----");

    assertThat(
        result,
        AllOf.$(
            ResultTo.exitCode().isEqualTo(3),
            ResultTo.err().findSubstrings(
                "symfonion:",
                "jsonpath:",
                ".\"$patterns\".C16x16.\"$body\"[0].\"$volume\"",
                "In this array, a string can contain only dots. E.g. ",
                "[10,\".X.\",100]")));
  }

  @Test
  public void givenArrayedVolumeHavingInvalidType_whenCompileThroughCli_thenErrorMessageLooksOkay() throws FileNotFoundException {
    assumeThat(isRunUnderPitest(), isFalse());
    JsonObject song = SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
        "port2", array(new StrokeBuilder().notes("C4").volume(array(10, object(), 100)).build()), SymfonionJsonTestUtils.sixteenBeatsGroove());


    Result result = invokeCliWithArguments("-c", writeContentToTempFile(Objects.toString(song)).getAbsolutePath());

    System.err.println("[source]");
    System.err.println("----");
    System.err.println(result);
    System.err.println("----");

    assertThat(
        result,
        AllOf.$(
            ResultTo.exitCode().isEqualTo(3),
            ResultTo.err().findSubstrings(
                "symfonion:",
                "jsonpath:",
                ".\"$patterns\".C16x16.\"$body\"[0].\"$volume\"",
                "This array, only integers, nulls, and strings containing only dots (...) are allowed.",
                "[10,{},100]")));
  }
}
