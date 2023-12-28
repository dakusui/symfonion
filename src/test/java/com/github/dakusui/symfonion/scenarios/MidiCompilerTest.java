package com.github.dakusui.symfonion.scenarios;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.song.Song;
import com.github.dakusui.testutils.*;
import com.github.dakusui.thincrest_pcond.forms.Functions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.sound.midi.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.testutils.Cliche.*;
import static com.github.dakusui.testutils.json.JsonTestUtils.*;
import static com.github.dakusui.symfonion.scenarios.MidiCompilerTest.TestCase.createNegativeTestCase;
import static com.github.dakusui.symfonion.scenarios.MidiCompilerTest.TestCase.createNormalTestCase;
import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static com.github.dakusui.thincrest_pcond.forms.Functions.elementAt;
import static com.github.dakusui.thincrest_pcond.forms.Functions.size;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.*;
import static com.github.dakusui.thincrest_pcond.forms.Printables.function;
import static java.lang.String.format;

@RunWith(Parameterized.class)
public class MidiCompilerTest {
  public record TestCase(JsonObject input, Predicate<Map<String, Sequence>> testOracleForOutput,
                         Predicate<Exception> testOracleForException) {
    public void executeAndVerify() {
      boolean succeeded = false;
      try {
        this.verifyOutput(execute(this.input));
        succeeded = true;
      } catch (Exception e) {
        this.verifyException(e);
      } finally {
        if (!succeeded)
          System.err.println(this.input);
      }
    }
    
    public void verifyOutput(Map<String, Sequence> output) {
      requireTestOracleForExceptionIsAbsent(output, this.testOracleForException);
      for (String eachPortName : output.keySet()) {
        Sequence sequence = output.get(eachPortName);
        for (Track eachTrack : sequence.getTracks()) {
          for (int i = 0; i < eachTrack.size(); i++) {
            System.err.printf("%3d: %4s: %s%n", i, eachTrack.get(i).getTick(), formatMidiMessage(eachTrack.get(i).getMessage()));
          }
        }
      }
      assertThat(output, this.testOracleForOutput());
    }
    
    
    private void verifyException(Exception e) {
      requireTestOracleForOutputIsAbsent(e, this.testOracleForOutput);
      assertThat(e, this.testOracleForException());
    }
    
    
    public static TestCase createNormalTestCase(JsonObject input, Predicate<Map<String, Sequence>> testOracleForOutput) {
      return new TestCase(input, testOracleForOutput, null);
    }
    
    public static TestCase createNegativeTestCase(JsonObject input, Predicate<Exception> testOracleForException) {
      return new TestCase(input, null, testOracleForException);
    }
    
    private static Map<String, Sequence> execute(JsonObject input) throws JsonException, InvalidMidiDataException, SymfonionException {
      return MidiCompilerTest.compileJsonObject(input);
    }
    
    private static void requireTestOracleForExceptionIsAbsent(Map<String, Sequence> output, Predicate<Exception> testOracleForException) {
      if (testOracleForException != null) {
        throw new RuntimeException(format("A test oracle for an exception: '%s' was provided but execution finished normally with output: '%s'", testOracleForException, output));
      }
    }
    
    private static void requireTestOracleForOutputIsAbsent(Exception e, Predicate<Map<String, Sequence>> testOracleForOutput) {
      if (testOracleForOutput != null) {
        throw new RuntimeException(format("A test oracle for normal output: '%s' was provided but an exception: '%s' was thrown.", testOracleForOutput, e.getMessage()), e);
      }
    }
  }
  
  private static String formatMidiMessage(MidiMessage message) {
    return String.format("message: %-20s: %s", message.getClass().getSimpleName(), toHex(message.getMessage()));
  }
  
  
  public static String toHex(byte[] a) {
    StringBuilder sb = new StringBuilder(a.length * 2);
    for (byte b : a)
      sb.append(String.format("%02x", b));
    return sb.toString();
  }
  
  private final TestCase testCase;
  
  public MidiCompilerTest(TestCase testCase) {
    this.testCase = testCase;
  }
  
  @Test
  public void exercise() {
    this.testCase.executeAndVerify();
  }
  
  @Parameters
  public static Collection<Object[]> parameters() {
    return Stream.concat(
        normalTestCases().stream().map(c -> new Object[]{c}),
        negativeTestCases().stream().map(c -> new Object[]{c})).collect(Collectors.toList());
  }
  
  public static List<TestCase> normalTestCases() {
    return Arrays.asList(
        createNormalTestCase(
            rootJsonObjectBase(),
            transform(compiledSong_keySet()).check(isEmpty())),
        createNormalTestCase(
            merge(
                rootJsonObjectBase(),
                sequenceJsonObjectBase()),
            transform(compiledSong_keySet()).check(isEmpty())),
        createNormalTestCase(
            merge(
                rootJsonObjectBase(),
                object($("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1"))))))),
                object($("$sequence", array(
                    merge(
                        object($("$beats", json("8/4"))),
                        object($("$patterns", object($("piano", array()))))
                    ))))),
            allOf(
                transform(compiledSong_keySet().andThen(castToObjectCollection())).check(allOf(
                    transform(size()).check(isEqualTo(1)),
                    transform(collectionToList().andThen(elementAt(0))).check(isEqualTo("port1")))),
                transform(compiledSong_getSequence("port1")).check(
                    allOf(
                        transform(SequenceTo.trackList()).check(
                            allOf(
                                transform(trackListSize()).check(isEqualTo(1)),
                                transform(trackAt(0).andThen(TrackTo.size())).check(isEqualTo(1)),
                                transform(trackAt(0).andThen(TrackTo.midiEventAt(0))).check(isNotNull()),
                                transform(trackAt(0).andThen(TrackTo.ticks())).check(isEqualTo(0L))
                            )),
                        transform(tickLengthFromSequence()).check(isEqualTo(0L))
                    )))),
        createNormalTestCase(
            merge(
                rootJsonObjectBase(),
                object(
                    $("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1"))))))
                ),
                
                object(
                    $("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1")))))),
                    $("$patterns", object($("pg-change-to-piano", object($("$body", array(json("C"), programChange(100, 83.2))))))),
                    $("$sequence", array(
                        object(
                            $("$beats", json("8/4")),
                            $("$patterns", object($("piano", array("pg-change-to-piano")))))
                    )))),
            allOf(
                Transform.$(compiledSong_keySet().andThen(castToObjectCollection())).check(allOf(
                    transform(size()).check(isEqualTo(1)),
                    transform(collectionToList().andThen(elementAt(0))).check(isEqualTo("port1")))),
                transform(compiledSong_getSequence("port1")).check(
                    allOf(
                        Transform.$(SequenceTo.trackList()).check(AllOf.$(
                            transform(trackListSize()).check(isEqualTo(1)),
                            transform(trackAt(0)).check(allOf(
                                transform(TrackTo.size()).check(isEqualTo(6)),
                                transform(TrackTo.midiEventAt(0)).check(isNotNull()),
                                transform(TrackTo.ticks()).check(isEqualTo(192L)))))),
                        transform(tickLengthFromSequence()).check(isEqualTo(192L)))
                ))),
        createNormalTestCase(
            object(
                $("$settings", object()),
                $("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1")))))),
                $("$patterns", object($("pg-change-to-piano", object($("$body", array(json("C"), programChange(101, 83.3))))))),
                $("$sequence", array(
                    merge(
                        object($("$beats", json("8/4"))),
                        object($("$patterns", object($("piano", array("pg-change-to-piano"))))))
                ))),
            allOf(
                Transform.$(Cliche.<String, Sequence>keySet().andThen(castToObjectCollection())).check(allOf(
                    Transform.$(size()).check(isEqualTo(1)),
                    Transform.$(collectionToList().andThen(elementAt(0))).check(isEqualTo("port1")))),
                transform(compiledSong_getSequence("port1")).check(
                    allOf(
                        Transform.$(SequenceTo.trackList()).check(AllOf.$(
                            transform(trackListSize()).check(isEqualTo(1)),
                            transform(trackAt(0)).check(allOf(
                                transform(TrackTo.size()).check(isEqualTo(6)),
                                transform(TrackTo.midiEventAt(0)).check(isNotNull()),
                                transform(TrackTo.ticks()).check(isEqualTo(192L)))))),
                        transform(tickLengthFromSequence()).check(isEqualTo(192L)))
                ))),
        createNormalTestCase(
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
            allOf(
                Transform.$(Cliche.<String, Sequence>keySet().andThen(castToObjectCollection())).check(allOf(
                    Transform.$(size()).check(isEqualTo(1)),
                    Transform.$(collectionToList().andThen(elementAt(0))).check(isEqualTo("port1")))),
                transform(compiledSong_getSequence("port1")).check(
                    allOf(
                        Transform.$(SequenceTo.trackList()).check(AllOf.$(
                            transform(trackListSize()).check(isEqualTo(1)),
                            transform(trackAt(0)).check(allOf(
                                transform(TrackTo.size()).check(isEqualTo(33)),
                                transform(TrackTo.midiEventAt(0)).check(isNotNull()),
                                transform(TrackTo.ticks()).check(isEqualTo(475L)))))),
                        transform(tickLengthFromSequence()).check(isEqualTo(475L)))
                ))),
        createNormalTestCase(
            composeSymfonionSongJsonObject(
                json("C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;"),
                sixteenBeatsGroove()),
            allOf(
                Transform.$(MapTo.<String, Sequence>keyList()).allOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<String>elementAt(0)).check(isEqualTo("port1"))),
                Transform.$(compiledSong_getSequence("port1").andThen(SequenceTo.trackList())).allOf(
                    Transform.$(ListTo.size()).check(isEqualTo(1)),
                    Transform.$(ListTo.<Track>elementAt(0)).allOf(
                        Transform.$(TrackTo.size()).check(isEqualTo(33)),
                        Transform.$(TrackTo.midiEventAt(0)).check(isNotNull()),
                        Transform.$(TrackTo.ticks()).check(isEqualTo(475L))))))
    );
  }
  
  private static JsonObject composeSymfonionSongJsonObject(JsonElement noteSequence, JsonArray groove) {
    String patternName = "C16x16";
    String grooveName = "16beats";
    String partName = "piano";
    String portName = "port1";
    String beats = "16/4";
    return object(
        $("$settings", object()),
        $("$parts", object($(partName, object($("$channel", json(0)), $("$port", json(portName)))))),
        $("$patterns", object($(patternName, object($("$body", array(noteSequence)))))),
        $("$grooves", object($(grooveName, groove))),
        $("$sequence", array(
            merge(
                object($("$beats", json(beats))),
                object($("$patterns", object($(partName, array(patternName))))),
                object($("$groove", json(grooveName)))
            ))));
  }
  
  private static JsonObject programChange(int program, double bank) {
    return object($("$program", json(program)), $("$bank", json(bank)));
  }
  
  private static JsonObject sequenceJsonObjectBase() {
    return object(
        $("$sequence", array(
            object(
                $("$beats", json("8/4")),
                $("$patterns", object()))
        )));
  }
  
  private static JsonObject rootJsonObjectBase() {
    return object(
        $("$settings", object()),
        $("$parts", object()),
        $("$patterns", object()),
        $("$sequence", array()));
  }
  
  public static List<TestCase> negativeTestCases() {
    return List.of(
        createNegativeTestCase(
            object(),
            isNotNull()));
  }
  
  
  private static Function<List<Track>, Track> trackAt(int i) {
    return elementAt(i);
  }
  
  
  private static Function<? super Object, Collection<Object>> castToObjectCollection() {
    return Functions.castTo(Functions.value());
  }
  
  
  private static Function<List<Track>, Integer> trackListSize() {
    return collectionSize();
  }
  
  private static Function<Map<String, Sequence>, Sequence> compiledSong_getSequence(String portName) {
    return function("get[" + portName + "]", m -> m.get(portName));
  }
  
  private static Function<Map<String, Sequence>, Set<String>> compiledSong_keySet() {
    return Cliche.keySet();
  }
  
  private static Map<String, Sequence> compileJsonObject(JsonObject jsonObject) throws InvalidMidiDataException, SymfonionException, JsonException {
    return compileJsonObject(Context.ROOT.createChild(), jsonObject);
  }
  
  private static Map<String, Sequence> compileJsonObject(Context context, JsonObject jsonObject) throws InvalidMidiDataException, SymfonionException, JsonException {
    return new MidiCompiler(context).compile(createSong(context, jsonObject));
  }
  
  private static Song createSong(Context context, JsonObject jsonObject) throws JsonException, SymfonionException {
    return new Song.Builder(context, jsonObject).build();
  }
  
  private static Function<Sequence, Long> tickLengthFromSequence() {
    return function("Sequence#getTickLength", Sequence::getTickLength);
  }
  
  
  private static JsonArray sixteenBeatsGrooveFlat() {
    return array(
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0)
    
    );
  }
  
  private static JsonArray sixteenBeatsGroove() {
    return array(
        grooveElement("16", 28, 30),
        grooveElement("16", 20, -10),
        grooveElement("16", 26, 10),
        grooveElement("16", 22, -5),
        
        grooveElement("16", 28, 20),
        grooveElement("16", 20, -8),
        grooveElement("16", 26, 10),
        grooveElement("16", 22, -4),
        
        grooveElement("16", 28, 25),
        grooveElement("16", 20, -8),
        grooveElement("16", 26, 10),
        grooveElement("16", 22, -5),
        
        grooveElement("16", 28, 15),
        grooveElement("16", 20, -8),
        grooveElement("16", 26, 10),
        grooveElement("16", 22, -10)
    );
  }
  
  
  private static JsonObject grooveElement(String noteLength, int ticks, int accent) {
    return object($("$length", json(noteLength)), $("$ticks", json(ticks)), $("$accent", json(accent)));
  }
  
}
