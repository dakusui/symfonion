package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.testutils.SymfonionTestCase;
import com.github.dakusui.symfonion.testutils.TestBase;
import com.github.dakusui.symfonion.testutils.forms.FromSong;
import com.github.dakusui.symfonion.testutils.json.StrokeBuilder;
import com.github.dakusui.symfonion.testutils.json.SymfonionJsonTestUtils;
import com.github.dakusui.testutils.TestUtils;
import com.github.dakusui.thincrest_cliche.core.AllOf;
import com.github.dakusui.thincrest_cliche.core.Transform;
import com.github.dakusui.thincrest_cliche.java.util.FromList;
import com.github.dakusui.thincrest_cliche.java.util.FromMap;
import com.github.valid8j.pcond.validator.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.dakusui.symfonion.testutils.SymfonionTestCase.createNegativeTestCase;
import static com.github.dakusui.symfonion.testutils.SymfonionTestCase.createPositiveTestCase;
import static com.github.dakusui.symfonion.testutils.SymfonionTestUtils.compileJsonObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.github.dakusui.symfonion.testutils.json.SymfonionJsonTestUtils.sixteenBeatsGrooveFlat;
import static com.github.dakusui.testutils.json.JsonTestUtils.*;
import static com.github.dakusui.testutils.midi.Controls.VOLUME;
import static com.github.dakusui.testutils.midi.Notes.C3;
import static com.github.dakusui.thincrest_cliche.core.Transform.toListBy;
import static com.github.dakusui.thincrest_cliche.core.Transform.toStreamBy;
import static com.github.dakusui.thincrest_cliche.java.util.ListTo.elementAt;
import static com.github.dakusui.thincrest_cliche.javax.sound.midi.IfMidiMessage.*;
import static com.github.dakusui.thincrest_cliche.javax.sound.midi.SequenceTo.tickLength;
import static com.github.dakusui.thincrest_cliche.javax.sound.midi.SequenceTo.trackList;
import static com.github.dakusui.thincrest_cliche.javax.sound.midi.TrackTo.*;
import static com.github.valid8j.pcond.forms.Predicates.*;

public class MidiCompilerTest extends TestBase {

  @BeforeAll
  public static void beforeAll() {
    Validator.reconfigure(c -> c.summarizedStringLength(120));
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("parameters")
  public void exercise(SymfonionTestCase testCase) {
    testCase.executeAndVerify();
  }

  public static Stream<SymfonionTestCase> parameters() {
    return Stream.concat(positiveTestCases().stream(), negativeTestCases().stream());
  }

  public static List<SymfonionTestCase> positiveTestCases() {
    String beats = "16/4";
    return Arrays.asList(
        createPositiveTestCase(
            TestUtils.name("top level attributes are all empty", "compile", "empty song"),
            SymfonionJsonTestUtils.rootJsonObjectBase(),
            Transform.$(FromSong.toKeySet()).check(isEmpty())),

        createPositiveTestCase(
            TestUtils.name("no pattern in sequence is given", "compile", "empty song"),
            merge(
                SymfonionJsonTestUtils.rootJsonObjectBase(),
                object(
                    $("sequence", array(
                        object(
                            $("beats", json("8/4")),
                            $("parts", array()))
                                        )))),
            Transform.$(FromSong.toKeySet()).check(isEmpty())),

        createPositiveTestCase(
            TestUtils.name("pattern contains no explicit event", "compile", "one message (end of sequence) is found"),
            merge(
                SymfonionJsonTestUtils.rootJsonObjectBase(),
                object($("parts", object($("piano", object($("channel", json(0)), $("port", json("port1"))))))),
                object($("sequence", array(
                    merge(
                        object($("beats", json("8/4"))),
                        object($("parts", array()))
                         ))))),
            AllOf.$(
                FromMap.<String>toKeyList().allOf(
                    FromList.toSize().isEqualTo(1),
                    FromList.<String>toElementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1")).allOf(
                    Transform.$(trackList()).allOf(
                        FromList.toSize().isEqualTo(2),
                        FromList.<Track>toElementAt(0).allOf(
                            Transform.$(size()).isEqualTo(1),
                            Transform.$(midiEventAt(0)).isNotNull(),
                            Transform.$(ticks()).isEqualTo(0L))),
                    Transform.$(tickLength()).isEqualTo(0L)
                                                               ))),


        createPositiveTestCase(
            TestUtils.name("pattern contains note on, note off, program change, and bank change (LSB and MSB)", "compile", "number of events and tick length seem ok"),
            object(
                $("settings", object()),
                $("parts", object($("piano", object($("channel", json(0)), $("port", json("port1")))))),
                $("sequence", array(
                    merge(
                        object($("beats", json("8/4"))),
                        object($("parts", array(merge(object($("name", json("piano"))), object($("body", array(json("C"), SymfonionJsonTestUtils.programChange(101, 83.3)))))))))
                                    ))),
            allOf(
                FromMap.<String>toKeyList().allOf(
                    FromList.toSize().isEqualTo(1),
                    FromList.<String>toElementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1")).allOf(
                    Transform.$(trackList()).allOf(
                        FromList.toSize().isEqualTo(2),
                        FromList.<Track>toElementAt(0).allOf(
                            Transform.$(size()).isEqualTo(6),
                            Transform.$(midiEventAt(0)).isNotNull(),
                            Transform.$(ticks()).isEqualTo(24L))),
                    Transform.$(tickLength()).isEqualTo(24L))
                 )),

        createPositiveTestCase(
            TestUtils.name("sixteen notes are given in a single string element", "compile", "number of events and tick length seem ok"),
            object(
                $("settings", object()),
                $("parts", object($("piano", object($("channel", json(0)), $("port", json("port1")))))),
                $("grooves", object($("16beats", sixteenBeatsGrooveFlat()))),
                $("sequence", array(
                    merge(
                        object($("beats", json("8/4"))),
                        object($("parts", array(merge(object($("name", json("piano"))), object($("body", array(json("C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;")))))))),
                        object($("groove", json("16beats")))
                         )))),
            AllOf.$(
                FromMap.<String>toKeyList().allOf(
                    FromList.toSize().isEqualTo(1),
                    FromList.<String>toElementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1")).allOf(
                    toListBy(trackList()).allOf(
                        FromList.toSize().isEqualTo(2),
                        FromList.<Track>toElementAt(0).allOf(
                            Transform.$(size()).isEqualTo(33),
                            Transform.$(midiEventAt(0)).isNotNull(),
                            Transform.$(ticks()).isEqualTo(379L))),
                    Transform.$(tickLength()).isEqualTo(379L))
                   )),

        createPositiveTestCase(
            TestUtils.name("sixteen notes are given in a single string element", "compile", "number of events and tick length seem ok"),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port1", json("C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;"), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            AllOf.$(
                FromMap.<String>toKeyList().allOf(
                    FromList.toSize().isEqualTo(1),
                    FromList.<String>toElementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1").andThen(trackList())).allOf(
                    FromList.toSize().isEqualTo(2),
                    FromList.<Track>toElementAt(0).allOf(
                        Transform.$(size()).isEqualTo(33),
                        Transform.$(midiEventAt(0)).isNotNull(),
                        Transform.$(ticks()).isEqualTo(379L))))),

        createPositiveTestCase(
            TestUtils.name("sixteen notes are given in two string elements", "compile", "number of events and tick length seem ok"),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port1", array(json("C16;C16;C16;C16;C16;C16;C16;C16;"), json("C16;C16;C16;C16;C16;C16;C16;C16;")), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            AllOf.$(
                FromMap.<String>toKeyList().allOf(
                    FromList.toSize().isEqualTo(1),
                    FromList.<String>toElementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1").andThen(trackList())).allOf(
                    FromList.toSize().isEqualTo(2),
                    FromList.<Track>toElementAt(0).allOf(
                        Transform.$(size()).isEqualTo(33),
                        Transform.$(midiEventAt(0)).isNotNull(),
                        Transform.$(ticks()).isEqualTo(379L))))),

        createPositiveTestCase(
            TestUtils.name("sixteenth note with short gate (0.25)", "compile", "number of events and tick length seem ok"),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port1", array(new StrokeBuilder().notes("C16").program(65).gate(0.25).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            AllOf.$(
                FromMap.<String>toKeyList().allOf(
                    FromList.toSize().isEqualTo(1),
                    FromList.<String>toElementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1").andThen(trackList())).allOf(
                    FromList.toSize().isEqualTo(2),
                    FromList.<Track>toElementAt(0).allOf(
                        Transform.$(size()).isEqualTo(4),
                        Transform.$(midiEventAt(0)).isNotNull(),
                        Transform.$(ticks()).isEqualTo(7L))))),

        createPositiveTestCase(
            TestUtils.name("a note and controls (program change, volume, pan, chorus, reverb, modulation, and pitch)", "compile", "note on/off, program change, and volume are included."),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port2", array(new StrokeBuilder().notes("C16").program(65).volume(99).pan(101).chorus(102).reverb(103).modulation(104).pitch(105).gate(0.25).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            Transform.$(FromSong.toSequence("port2").andThen(trackList()).andThen(elementAt(0))).allOf(
                toStreamBy(midiMessageStream(isNoteOn())).anyMatch(note(isEqualTo(C3))),
                toStreamBy(midiMessageStream(isNoteOff())).anyMatch(note(isEqualTo(C3))),
                toStreamBy(midiMessageStream(isProgramChange())).anyMatch(programNumber(isEqualTo((byte) 65))),
                toStreamBy(midiMessageStream(isControlChange())).anyMatch(control(isEqualTo(VOLUME)))
                                                                                                      )),

        createPositiveTestCase(
            TestUtils.name("a note and an 'arrayable' control (volume)", "compile", "arrayable control expanded."),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port2", array(new StrokeBuilder().notes("C4").volume(array(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            Transform.$(FromSong.toSequence("port2").andThen(trackList()).andThen(elementAt(0))).allOf(
                toStreamBy(midiMessageStream(isNoteOn())).anyMatch(note(isEqualTo(C3))),
                toStreamBy(midiMessageStream(isNoteOff())).anyMatch(note(isEqualTo(C3))),
                toStreamBy(midiMessageStream(isControlChange().and(control(isEqualTo(VOLUME))))).allMatch(controlData(greaterThanOrEqualTo((byte) 10))),
                toStreamBy(midiMessageStream(isControlChange().and(control(isEqualTo(VOLUME))))).checkCount(isEqualTo(10L))
                                                                                                      )),

        createPositiveTestCase(
            TestUtils.name("a note and an 'arrayable' control (volume) with nulls", "compile", "arrayable control expanded replacing nulls with intermediate values."),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port2", array(new StrokeBuilder().notes("C4").volume(array(10, null, null, null, null, null, null, null, null, 100)).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            Transform.$(FromSong.toSequence("port2").andThen(trackList()).andThen(elementAt(0))).allOf(
                toStreamBy(midiMessageStream(isNoteOn())).anyMatch(note(isEqualTo(C3))),
                toStreamBy(midiMessageStream(isNoteOff())).anyMatch(note(isEqualTo(C3))),
                toStreamBy(midiMessageStream(isControlChange().and(control(isEqualTo(VOLUME))))).allMatch(controlData(greaterThanOrEqualTo((byte) 10))),
                toStreamBy(midiMessageStream(isControlChange().and(control(isEqualTo(VOLUME))))).checkCount(isEqualTo(10L)),
                toStreamBy(midiMessageStream(isControlChange().and(control(isEqualTo(VOLUME)).and(controlData(greaterThanOrEqualTo((byte) 50)))))).checkCount(greaterThan(4L))
                                                                                                      )),

        createPositiveTestCase(
            TestUtils.name("a note and an 'arrayable' control (volume) with nulls", "compile", "arrayable control expanded replacing dots with intermediate values."),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port2", array(new StrokeBuilder().notes("C4").volume(array(10, "........", 100)).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            Transform.$(FromSong.toSequence("port2").andThen(trackList()).andThen(elementAt(0))).allOf(
                toStreamBy(midiMessageStream(isNoteOn())).anyMatch(note(isEqualTo(C3))),
                toStreamBy(midiMessageStream(isNoteOff())).anyMatch(note(isEqualTo(C3))),
                toStreamBy(midiMessageStream(isControlChange().and(control(isEqualTo(VOLUME))))).allMatch(controlData(greaterThanOrEqualTo((byte) 10))),
                toStreamBy(midiMessageStream(isControlChange().and(control(isEqualTo(VOLUME))))).checkCount(isEqualTo(10L)),
                toStreamBy(midiMessageStream(isControlChange().and(control(isEqualTo(VOLUME)).and(controlData(greaterThanOrEqualTo((byte) 50)))))).checkCount(greaterThan(4L))
                                                                                                      )),

        createPositiveTestCase(
            TestUtils.name("two stacked patterns on same part", "compile", "both patterns' notes appear in track"),
            object(
                $("settings", object()),
                $("parts", object($("piano", object($("channel", json(0)), $("port", json("port1")))))),
                $("sequence", array(
                    merge(
                        object($("beats", json("4/4"))),
                        object($("parts", array(
                            merge(object($("name", json("piano"))), object($("body", json("C4")))),
                            merge(object($("name", json("piano"))), object($("body", json("E4"))))
                        )))
                    )
                ))),
            Transform.$(FromSong.toSequence("port1").andThen(trackList()).andThen(elementAt(0))).allOf(
                toStreamBy(midiMessageStream(isNoteOn())).checkCount(isEqualTo(2L)),
                toStreamBy(midiMessageStream(isNoteOn())).anyMatch(note(isEqualTo(C3))),
                toStreamBy(midiMessageStream(isNoteOn())).anyMatch(note(isEqualTo((byte) 64))), // E3
                toStreamBy(midiMessageStream(isNoteOff())).checkCount(isEqualTo(2L))))
                        );

  }

  /**
   * Verifies pickup notation: notes before `|` in a body string play in the tail
   * of the preceding bar. With a 4/4 bar (384 ticks) and an E8 pickup (48 ticks),
   * the pickup NoteOn must land at tick 336 = 384 - 48.
   */
  @org.junit.jupiter.api.Test
  void pickupNotation_noteOnAtCorrectTick() throws Exception {
    JsonObject song = object(
        $("settings", object()),
        $("parts", object($("piano", object($("channel", json(0)), $("port", json("port1")))))),
        $("sequence", array(
            merge(object($("beats", json("4/4"))),
                  object($("parts", array(merge(object($("name", json("piano"))),
                                               object($("body", json("r4;r4;r4;r4")))))))),
            merge(object($("beats", json("4/4"))),
                  object($("parts", array(merge(object($("name", json("piano"))),
                                               object($("body", json("E8|E4;D4;C4;r4")))))))))));;

    Map<String, Sequence> result = compileJsonObject(song);
    Track track = result.get("port1").getTracks()[0];

    long pickupTick = -1;
    for (int i = 0; i < track.size(); i++) {
      MidiEvent ev = track.get(i);
      byte[] msg = ev.getMessage().getMessage();
      boolean isNoteOn = (msg[0] & 0xf0) == 0x90;
      boolean isE = msg[1] == 64; // E3 = MIDI key 64
      if (isNoteOn && isE) {
        pickupTick = ev.getTick();
        break;
      }
    }
    assertEquals(336L, pickupTick, "Pickup E8 note-on should land at tick 336 (last 1/8 of the preceding 4/4 bar)");
  }

  /**
   * Verifies that whitespace (spaces, tabs, newlines) around semicolons in a body
   * string is ignored. The multi-line form must produce the same notes as the
   * equivalent compact single-line form.
   */
  @org.junit.jupiter.api.Test
  void bodyWithWhitespaceAroundSemicolons_producesCorrectNoteCount() throws Exception {
    JsonObject song = object(
        $("settings", object()),
        $("parts", object($("piano", object($("channel", json(0)), $("port", json("port1")))))),
        $("sequence", array(
            merge(object($("beats", json("4/4"))),
                  object($("parts", array(merge(object($("name", json("piano"))),
                                               object($("body", json("C4;\n  E4;\n  G4")))))))))));

    Map<String, Sequence> result = compileJsonObject(song);
    Track track = result.get("port1").getTracks()[0];

    long noteOnCount = 0;
    for (int i = 0; i < track.size(); i++) {
      byte[] msg = track.get(i).getMessage().getMessage();
      if ((msg[0] & 0xf0) == 0x90 && msg[2] != 0) noteOnCount++;
    }
    assertEquals(3L, noteOnCount, "body with whitespace around semicolons should produce 3 note-on events");
  }

  public static List<SymfonionTestCase> negativeTestCases() {
    return List.of(
        createNegativeTestCase(
            TestUtils.name("empty JSON object", "compile", "An exception is thrown"),
            object(),
            isNotNull()));
  }

}
