package com.github.dakusui.symfonion.tests;

import com.github.dakusui.testutils.TestUtils;
import com.github.dakusui.testutils.forms.*;
import com.github.dakusui.testutils.forms.midi.IfMidiMessage;
import com.github.dakusui.testutils.forms.midi.SequenceTo;
import com.github.dakusui.testutils.forms.midi.TrackTo;
import com.github.dakusui.testutils.forms.symfonion.SongTo;
import com.github.dakusui.testutils.symfonion.json.StrokeBuilder;
import com.github.dakusui.testutils.symfonion.json.SymfonionJsonTestUtils;
import com.github.dakusui.testutils.midi.Controls;
import com.github.dakusui.testutils.symfonion.SymfonionTestCase;
import com.github.dakusui.thincrest_pcond.validator.Validator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.testutils.midi.Notes.C3;
import static com.github.dakusui.testutils.symfonion.SymfonionTestCase.createNegativeTestCase;
import static com.github.dakusui.testutils.symfonion.SymfonionTestCase.createPositiveTestCase;
import static com.github.dakusui.testutils.forms.midi.IfMidiMessage.*;
import static com.github.dakusui.testutils.json.JsonTestUtils.*;
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
            Transform.$(SongTo.keySet()).check(isEmpty())),
        
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
            Transform.$(SongTo.keySet()).check(isEmpty())),
        
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
                Transform.$(MapTo.<String, Sequence>keyList()).allOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<String>elementAt(0)).check(isEqualTo("port1"))),
                Transform.$(SongTo.sequence("port1")).allOf(
                    Transform.$(SequenceTo.trackList()).checkAllOf(
                        Transform.$(ListTo.size()).check(isEqualTo(1)),
                        Transform.$(ListTo.<Track>elementAt(0)).checkAllOf(
                            Transform.$(TrackTo.size()).check(isEqualTo(1)),
                            Transform.$(TrackTo.midiEventAt(0)).check(isNotNull()),
                            Transform.$(TrackTo.ticks()).check(isEqualTo(0L)))),
                    Transform.$(SequenceTo.tickLength()).check(isEqualTo(0L))
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
                Transform.$(MapTo.<String, Sequence>keyList()).checkAllOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<String>elementAt(0)).check(isEqualTo("port1"))),
                Transform.$(SongTo.sequence("port1")).check(
                    allOf(
                        Transform.$(SequenceTo.trackList()).check(AllOf.$(
                            Transform.$(ListTo.size()).check(isEqualTo(1)),
                            Transform.$(ListTo.<Track>elementAt(0)).check(allOf(
                                Transform.$(TrackTo.size()).check(isEqualTo(6)),
                                Transform.$(TrackTo.midiEventAt(0)).check(isNotNull()),
                                Transform.$(TrackTo.ticks()).check(isEqualTo(96L)))))),
                        Transform.$(SequenceTo.tickLength()).check(isEqualTo(96L)))
                ))),
        
        createPositiveTestCase(
            TestUtils.name("sixteen notes are given in a single string element", "compile", "number of events and tick length seem ok"),
            object(
                $("$settings", object()),
                $("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1")))))),
                $("$patterns", object($("C16x16", object($("$body", array(json("C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;"))))))),
                $("$grooves", object($("16beats", SymfonionJsonTestUtils.sixteenBeatsGrooveFlat()))),
                $("$sequence", array(
                    merge(
                        object($("$beats", json("8/4"))),
                        object($("$patterns", object($("piano", array("C16x16"))))),
                        object($("$groove", json("16beats")))
                    )))),
            AllOf.$(
                Transform.$(MapTo.<String, Sequence>keyList()).checkAllOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<String>elementAt(0)).check(isEqualTo("port1"))),
                Transform.$(SongTo.sequence("port1")).allOf(
                    Transform.$(SequenceTo.trackList()).allOf(
                        Transform.$(ListTo.size()).check(isEqualTo(1)),
                        Transform.$(ListTo.<Track>elementAt(0)).allOf(
                            Transform.$(TrackTo.size()).check(isEqualTo(33)),
                            Transform.$(TrackTo.midiEventAt(0)).check(isNotNull()),
                            Transform.$(TrackTo.ticks()).check(isEqualTo(379L)))),
                    Transform.$(SequenceTo.tickLength()).check(isEqualTo(379L)))
            )),
        
        createPositiveTestCase(
            TestUtils.name("sixteen notes are given in a single string element", "compile", "number of events and tick length seem ok"),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port1", json("C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;"), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            AllOf.$(
                Transform.$(MapTo.<String, Sequence>keyList()).allOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<String>elementAt(0)).check(isEqualTo("port1"))),
                Transform.$(SongTo.sequence("port1").andThen(SequenceTo.trackList())).allOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<Track>elementAt(0)).allOf(
                        Transform.$(TrackTo.size()).check(isEqualTo(33)),
                        Transform.$(TrackTo.midiEventAt(0)).check(isNotNull()),
                        Transform.$(TrackTo.ticks()).check(isEqualTo(379L)))))),
        
        createPositiveTestCase(
            TestUtils.name("sixteen notes are given in two string elements", "compile", "number of events and tick length seem ok"),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port1", array(json("C16;C16;C16;C16;C16;C16;C16;C16;"), json("C16;C16;C16;C16;C16;C16;C16;C16;")), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            AllOf.$(
                Transform.$(MapTo.<String, Sequence>keyList()).allOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<String>elementAt(0)).check(isEqualTo("port1"))),
                Transform.$(SongTo.sequence("port1").andThen(SequenceTo.trackList())).allOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<Track>elementAt(0)).allOf(
                        Transform.$(TrackTo.size()).check(isEqualTo(33)),
                        Transform.$(TrackTo.midiEventAt(0)).check(isNotNull()),
                        Transform.$(TrackTo.ticks()).check(isEqualTo(379L)))))),
        
        createPositiveTestCase(
            TestUtils.name("sixteenth note with short gate (0.25)", "compile", "number of events and tick length seem ok"),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port1", array(new StrokeBuilder().notes("C16").program(65).gate(0.25).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            AllOf.$(
                Transform.$(MapTo.<String, Sequence>keyList()).allOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<String>elementAt(0)).check(isEqualTo("port1"))),
                Transform.$(SongTo.sequence("port1").andThen(SequenceTo.trackList())).allOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<Track>elementAt(0)).allOf(
                        Transform.$(TrackTo.size()).check(isEqualTo(4)),
                        Transform.$(TrackTo.midiEventAt(0)).check(isNotNull()),
                        Transform.$(TrackTo.ticks()).check(isEqualTo(7L)))))),
        
        createPositiveTestCase(
            TestUtils.name("a note and controls (program change, volume, pan, chorus, reverb, modulation, and pitch)", "compile", "note on/off, program change, and volume are included."),
            SymfonionJsonTestUtils.composeSymfonionSongJsonObject(
                "port2", array(new StrokeBuilder().notes("C16").program(65).volume(99).pan(101).chorus(102).reverb(103).modulation(104).pitch(105).gate(0.25).build()), SymfonionJsonTestUtils.sixteenBeatsGroove()),
            Transform.$(SongTo.sequence("port2").andThen(SequenceTo.trackList()).andThen(ListTo.elementAt(0))).allOf(
                Transform.$(TrackTo.midiMessageStream(IfMidiMessage.isNoteOn())).check(anyMatch(note(isEqualTo(C3)))),
                Transform.$(TrackTo.midiMessageStream(IfMidiMessage.isNoteOff())).check(anyMatch(note(isEqualTo(C3)))),
                Transform.$(TrackTo.midiMessageStream(IfMidiMessage.isProgramChange())).check(anyMatch(programNumber(isEqualTo((byte) 65)))),
                Transform.$(TrackTo.midiMessageStream(IfMidiMessage.isControlChange())).check(anyMatch(control(isEqualTo(Controls.VOLUME))))
            )));
  }
  
  public static List<SymfonionTestCase> negativeTestCases() {
    return List.of(
        createNegativeTestCase(
            TestUtils.name("empty JSON object", "compile", "An exception is thrown"),
            object(),
            isNotNull()));
  }
  
}
