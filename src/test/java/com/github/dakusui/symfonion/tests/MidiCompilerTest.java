package com.github.dakusui.symfonion.tests;

import com.github.dakusui.symfonion.testutils.SymfonionTestCase;
import com.github.dakusui.symfonion.testutils.forms.FromSong;
import com.github.dakusui.symfonion.testutils.json.StrokeBuilder;
import com.github.dakusui.symfonion.testutils.json.SymfonionJsonTestUtils;
import com.github.dakusui.testutils.TestUtils;
import com.github.dakusui.testutils.forms.AllOf;
import com.github.dakusui.testutils.forms.FromList;
import com.github.dakusui.testutils.forms.FromStream;
import com.github.dakusui.testutils.forms.Transform;
import com.github.dakusui.testutils.forms.midi.FromTrack;
import com.github.dakusui.testutils.forms.midi.IfMidiMessage;
import com.github.dakusui.thincrest_pcond.validator.Validator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.symfonion.testutils.SymfonionTestCase.createNegativeTestCase;
import static com.github.dakusui.symfonion.testutils.SymfonionTestCase.createPositiveTestCase;
import static com.github.dakusui.symfonion.testutils.json.SymfonionJsonTestUtils.sixteenBeatsGrooveFlat;
import static com.github.dakusui.testutils.forms.midi.FromSequence.toTickLength;
import static com.github.dakusui.testutils.forms.midi.FromSequence.toTrackList;
import static com.github.dakusui.testutils.forms.midi.FromTrack.*;
import static com.github.dakusui.testutils.forms.midi.IfMidiMessage.*;
import static com.github.dakusui.testutils.json.JsonTestUtils.*;
import static com.github.dakusui.testutils.midi.Controls.VOLUME;
import static com.github.dakusui.testutils.midi.Notes.C3;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.*;

@RunWith(Parameterized.class)
public class MidiCompilerTest {

  @BeforeClass
  public static void beforeAll() {
    Validator.reconfigure(c -> c.summarizedStringLength(120));
  }

  private final SymfonionTestCase testCase;

  public MidiCompilerTest(SymfonionTestCase testCase) {
    this.testCase = testCase;
  }

  @Test
  public void exercise() {
    this.testCase.executeAndVerify();
  }

  @Parameters(name = "{index}: {0}")
  public static Collection<Object[]> parameters() {
    return Stream.concat(
        positiveTestCases().stream().map(c -> new Object[]{c}),
        negativeTestCases().stream().map(c -> new Object[]{c})).collect(Collectors.toList());
  }

  public static List<SymfonionTestCase> positiveTestCases() {
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
                    $("$sequence", array(
                        object(
                            $("$beats", json("8/4")),
                            $("$patterns", object()))
                    )))),
            Transform.$(FromSong.toKeySet()).check(isEmpty())),

        createPositiveTestCase(
            TestUtils.name("pattern contains no explicit event", "compile", "one message (end of sequence) is found"),
            merge(
                SymfonionJsonTestUtils.rootJsonObjectBase(),
                object($("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1"))))))),
                object($("$sequence", array(
                    merge(
                        object($("$beats", json("8/4"))),
                        object($("$patterns", object($("piano", array()))))
                    ))))),
            AllOf.$(
                MapTo.<String>keyList().allOf(
                    ListTo.size().isEqualTo(1),
                    ListTo.<String>elementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1")).allOf(
                    Transform.$(toTrackList()).allOf(
                        ListTo.size().isEqualTo(1),
                        ListTo.<Track>elementAt(0).allOf(
                            Transform.$(toSize()).isEqualTo(1),
                            Transform.$(toMidiEventAt(0)).isNotNull(),
                            Transform.$(toTicks()).isEqualTo(0L))),
                    Transform.$(toTickLength()).isEqualTo(0L)
                ))),


        createPositiveTestCase(
            TestUtils.name("pattern contains note on, note off, program change, and bank change (LSB and MSB)", "compile", "number of events and tick length seem ok"),
            object(
                $("$settings", object()),
                $("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1")))))),
                $("$patterns", object($("pg-change-to-piano", object($("$body", array(json("C"), SymfonionJsonTestUtils.programChange(101, 83.3))))))),
                $("$sequence", array(
                    merge(
                        object($("$beats", json("8/4"))),
                        object($("$patterns", object($("piano", array("pg-change-to-piano"))))))
                ))),
            allOf(
                MapTo.<String>keyList().allOf(
                    ListTo.size().isEqualTo(1),
                    ListTo.<String>elementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1")).allOf(
                    Transform.$(toTrackList()).allOf(
                        ListTo.size().isEqualTo(1),
                        ListTo.<Track>elementAt(0).allOf(
                            Transform.$(toSize()).isEqualTo(6),
                            Transform.$(toMidiEventAt(0)).isNotNull(),
                            Transform.$(toTicks()).isEqualTo(96L))),
                    Transform.$(toTickLength()).isEqualTo(96L))
            )),

        createPositiveTestCase(
            TestUtils.name("sixteen notes are given in a single string element", "compile", "number of events and tick length seem ok"),
            object(
                $("$settings", object()),
                $("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1")))))),
                $("$patterns", object($("C16x16", object($("$body", array(json("C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;"))))))),
                $("$grooves", object($("16beats", sixteenBeatsGrooveFlat()))),
                $("$sequence", array(
                    merge(
                        object($("$beats", json("8/4"))),
                        object($("$patterns", object($("piano", array("C16x16"))))),
                        object($("$groove", json("16beats")))
                    )))),
            AllOf.$(
                MapTo.<String>keyList().allOf(
                    ListTo.size().isEqualTo(1),
                    ListTo.<String>elementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1")).allOf(
                    Transform.$(toTrackList()).allOf(
                        ListTo.size().isEqualTo(1),
                        ListTo.<Track>elementAt(0).allOf(
                            Transform.$(toSize()).isEqualTo(33),
                            Transform.$(toMidiEventAt(0)).isNotNull(),
                            Transform.$(toTicks()).isEqualTo(379L))),
                    Transform.$(toTickLength()).isEqualTo(379L))
            )),

        createPositiveTestCase(
            TestUtils.name("sixteen notes are given in a single string element", "compile", "number of events and tick length seem ok"),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port1", json("C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;"), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            AllOf.$(
                MapTo.<String>keyList().allOf(
                    ListTo.size().isEqualTo(1),
                    ListTo.<String>elementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1").andThen(toTrackList())).allOf(
                    ListTo.size().isEqualTo(1),
                    ListTo.<Track>elementAt(0).allOf(
                        Transform.$(toSize()).isEqualTo(33),
                        Transform.$(toMidiEventAt(0)).isNotNull(),
                        Transform.$(toTicks()).isEqualTo(379L))))),

        createPositiveTestCase(
            TestUtils.name("sixteen notes are given in two string elements", "compile", "number of events and tick length seem ok"),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port1", array(json("C16;C16;C16;C16;C16;C16;C16;C16;"), json("C16;C16;C16;C16;C16;C16;C16;C16;")), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            AllOf.$(
                MapTo.<String>keyList().allOf(
                    ListTo.size().isEqualTo(1),
                    ListTo.<String>elementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1").andThen(toTrackList())).allOf(
                    ListTo.size().isEqualTo(1),
                    ListTo.<Track>elementAt(0).allOf(
                        Transform.$(toSize()).isEqualTo(33),
                        Transform.$(toMidiEventAt(0)).isNotNull(),
                        Transform.$(toTicks()).isEqualTo(379L))))),

        createPositiveTestCase(
            TestUtils.name("sixteenth note with short gate (0.25)", "compile", "number of events and tick length seem ok"),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port1", array(new StrokeBuilder().notes("C16").program(65).gate(0.25).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            AllOf.$(
                MapTo.<String>keyList().allOf(
                    ListTo.size().isEqualTo(1),
                    ListTo.<String>elementAt(0).isEqualTo("port1")),
                Transform.$(FromSong.toSequence("port1").andThen(toTrackList())).allOf(
                    ListTo.size().isEqualTo(1),
                    ListTo.<Track>elementAt(0).allOf(
                        Transform.$(toSize()).isEqualTo(4),
                        Transform.$(toMidiEventAt(0)).isNotNull(),
                        Transform.$(toTicks()).isEqualTo(7L))))),

        createPositiveTestCase(
            TestUtils.name("a note and controls (program change, volume, pan, chorus, reverb, modulation, and pitch)", "compile", "note on/off, program change, and volume are included."),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port2", array(new StrokeBuilder().notes("C16").program(65).volume(99).pan(101).chorus(102).reverb(103).modulation(104).pitch(105).gate(0.25).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            Transform.$(FromSong.toSequence("port2").andThen(toTrackList()).andThen(FromList.toElementAt(0))).allOf(
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isNoteOn())).check(anyMatch(note(isEqualTo(C3)))),
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isNoteOff())).check(anyMatch(note(isEqualTo(C3)))),
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isProgramChange())).check(anyMatch(programNumber(isEqualTo((byte) 65)))),
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isControlChange())).check(anyMatch(control(isEqualTo(VOLUME))))
            )),

        createPositiveTestCase(
            TestUtils.name("a note and an 'arrayable' control (volume)", "compile", "arrayable control expanded."),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port2", array(new StrokeBuilder().notes("C4").volume(array(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            Transform.$(FromSong.toSequence("port2").andThen(toTrackList()).andThen(FromList.toElementAt(0))).allOf(
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isNoteOn())).check(anyMatch(note(isEqualTo(C3)))),
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isNoteOff())).check(anyMatch(note(isEqualTo(C3)))),
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isControlChange().and(control(isEqualTo(VOLUME)))))
                    .check(allMatch(controlData(greaterThanOrEqualTo((byte) 10)))),
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isControlChange().and(control(isEqualTo(VOLUME)))))
                    .check(Transform.<Stream<MidiMessage>, Long>$(FromStream.count()).isEqualTo(10L))
            )),
        createPositiveTestCase(
            TestUtils.name("a note and an 'arrayable' control (volume) with nulls", "compile", "arrayable control expanded replacing nulls with intermediate values."),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port2", array(new StrokeBuilder().notes("C4").volume(array(10, null, null, null, null, null, null, null, null, 100)).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            Transform.$(FromSong.toSequence("port2").andThen(toTrackList()).andThen(FromList.toElementAt(0))).allOf(
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isNoteOn())).check(anyMatch(note(isEqualTo(C3)))),
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isNoteOff())).check(anyMatch(note(isEqualTo(C3)))),
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isControlChange().and(control(isEqualTo(VOLUME)))))
                    .check(allMatch(controlData(greaterThanOrEqualTo((byte) 10)))),
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isControlChange().and(control(isEqualTo(VOLUME)))))
                    .check(Transform.<Stream<MidiMessage>, Long>$(FromStream.count()).isEqualTo(10L)),
                Transform.$(FromTrack.toMidiMessageStream(IfMidiMessage.isControlChange().and(control(isEqualTo(VOLUME)).and(controlData(greaterThanOrEqualTo((byte) 50))))))
                    .check(Transform.<Stream<MidiMessage>, Long>$(FromStream.count()).check(greaterThan(4L)))
            ))
    );

  }

  public static List<SymfonionTestCase> negativeTestCases() {
    return List.of(
        createNegativeTestCase(
            TestUtils.name("empty JSON object", "compile", "An exception is thrown"),
            object(),
            isNotNull()));
  }

}
