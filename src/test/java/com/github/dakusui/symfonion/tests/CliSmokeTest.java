package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.testutils.CliTestBase;
import com.github.dakusui.symfonion.testutils.json.StrokeBuilder;
import com.github.dakusui.symfonion.testutils.json.SymfonionJsonTestUtils;
import com.github.dakusui.thincrest_cliche.core.AllOf;
import com.github.dakusui.thincrest_cliche.sut.symfonion.ResultTo;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import static com.github.dakusui.symfonion.testutils.json.SymfonionJsonTestUtils.sixteenBeatsGroove;
import static com.github.dakusui.testutils.TestUtils.isRunUnderPitest;
import static com.github.dakusui.testutils.json.JsonTestUtils.*;
import static com.github.dakusui.testutils.midi.MidiTestUtils.assumeRequiredMidiDevicesPresent;
import static com.github.valid8j.classic.TestAssertions.assertThat;
import static com.github.valid8j.classic.TestAssertions.assumeThat;
import static com.github.valid8j.pcond.forms.Predicates.isFalse;

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
        $("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("GERVILL_PORT")))))),
        $("$patterns", object(
            $("R4", object($("$body", json("r4;r4;r4;r4")))),
            $("main", object($("$body", array(json("BGE8|BGE8;r8;AFD8;r8;GEC8;r8"))))))),
        $("$grooves", object($("16beats", sixteenBeatsGroove()))),
        $("$sequence", array(
            object(
                $("$labels", array(json("reference"))),
                $("$beats", json("4/4")),
                $("$parts", object($("piano", array("R4"))))),
            object(
                $("$labels", array(json("reference"))),
                $("$beats", json("4/4")),
                $("$parts", object($("piano", array("main")))),
                $("$groove", json("16beats"))),
            object(
                $("$labels", array(json("reference"))),
                $("$beats", json("4/4")),
                $("$parts", object($("piano", array("main")))),
                $("$groove", json("16beats"))),
            object(
                $("$labels", array(json("inline"))),
                $("$beats", json("4/4")),
                $("$parts", object($("piano", array("$inline:" + object($("$body", array(json("C8;D8;E8|GEC8;r8;AFD8;r8;BGE8;r8")))))))),
                $("$groove", json("16beats"))))));

    Result result = invokeCliWithArguments("-p", writeContentToTempFile(Objects.toString(song)).getAbsolutePath(), "-OGERVILL_PORT=Gervill", "--bars=*", "--parts=p.*");

    System.err.println("[source]");
    System.err.println(".Song");
    System.err.println("----");
    System.err.println(new GsonBuilder().setPrettyPrinting().create().toJson(song));
    System.err.println("----");
    System.err.println();

    System.err.println("[source]");
    System.err.println(".Result");
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
  public void givenK311_whenPlaySubcommandIsInvoked_thenPlayed() throws IOException {
    assumeRequiredMidiDevicesPresent();
    assumeThat(isRunUnderPitest(), isFalse());

    Result result = compileAndPlayResourceWithCli("examples/example-k311.json");

    System.out.println(result);
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
            $("drums", object($("$channel", json(9)), $("$port", json("port1"))))
                          )),
        $("$patterns", object(
            $("R4", object($("$body", json("r4")))),
            $("R8", object($("$body", json("r8")))),
            $("pgchg-piano", object($("$body", array(object(
                $("$notes", json("r16")),
                $("$volume", json(120)),
                $("$pan", json(0)),
                $("$reverb", json(120)),
                $("$bank", json(5.15)),
                $("$program", json(33))))))),
            $("pgchg-guitar", object($("$body", array(object(
                $("$notes", json("r16")),
                $("$volume", json(120)),
                $("$pan", json(60)),
                $("$bank", json(5.45)),
                $("$program", json(1))))))),
            $("pan:left-to-right", object($("$body", array(object(
                $("$notes", json("r2")), $("$pan", array(0, "..............", 127))))))),
            $("pan:right-to-left", object($("$body", array(object(
                $("$notes", json("r2")), $("$pan", array(1, "..............", 0))))))),
            $("main", object($("$body", array(json("BGE4;AFD4;GEC2"))))),
            $("sub", object(
                $("$body", array(json("BGE8;BGE8;AFD8;AFD8;GEC8;GEC8"))))),
            $("drum-1", object(
                $("$notemap", json("$percussion")),
                $("$body", array(json("BH16;H16;H16;H16;BSH32;H32;H16;H16;H16;BH16;H16;H16;H16;BSH32;H32;H16;H16;H16;")))))
                             )),
        $("$grooves", object($("16beats", sixteenBeatsGroove()))),
        $("$sequence", array(
            object(
                $("$beats", json("1/8")),
                $("$patterns", object(
                    $("piano", array("R8", "pgchg-piano")),
                    $("guitar", array("R8", "pgchg-guitar"))))),
            object(
                $("$beats", json("4/4")),
                $("$patterns", object(
                    $("piano", array("main")),
                    $("guitar", array("sub", "pan:left-to-right")),
                    $("drums", array("drum-1"))
                                     )),
                $("$groove", json("16beats"))
                  ),
            object(
                $("$beats", json("4/4")),
                $("$patterns", object(
                    $("drums", array("drum-1"))
                                     )),
                $("$groove", json("16beats"))
                  )
                            )));

    System.err.println("[source, json]");
    System.err.println("----");
    System.err.println(new GsonBuilder().setPrettyPrinting().create().toJson(song));
    System.err.println("----");
    System.err.println();

    Result result = invokeCliWithArguments("-p", writeContentToTempFile(Objects.toString(song)).getAbsolutePath(), "-Oport2=\\[hw:1,1,0\\]", "-Oport1=\\[hw:1,0,0\\]");

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
