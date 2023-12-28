package com.github.dakusui.symfonion.scenarios;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.song.Song;
import com.github.dakusui.testutils.AllOf;
import com.github.dakusui.testutils.Cliche;
import com.github.dakusui.testutils.Transform;
import com.github.dakusui.thincrest_pcond.forms.Functions;
import com.github.dakusui.thincrest_pcond.forms.Predicates;
import com.github.dakusui.thincrest_pcond.forms.Printables;
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

import static com.github.dakusui.json.JsonTestUtils.*;
import static com.github.dakusui.testutils.Cliche.collectionSize;
import static com.github.dakusui.symfonion.scenarios.MidiCompilerTest.TestCase.createNegativeTestCase;
import static com.github.dakusui.symfonion.scenarios.MidiCompilerTest.TestCase.createNormalTestCase;
import static com.github.dakusui.testutils.Cliche.collectionToList;
import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static com.github.dakusui.thincrest_pcond.forms.Functions.elementAt;
import static com.github.dakusui.thincrest_pcond.forms.Functions.size;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.*;
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
                        transform(tracksFromSequence()).check(
                            allOf(
                                transform(trackListSize()).check(isEqualTo(1)),
                                transform(trackAt(0).andThen(sizeFromTrack())).check(isEqualTo(1)),
                                transform(trackAt(0).andThen(midiEventFromTrack(0))).check(isNotNull()),
                                transform(trackAt(0).andThen(ticksFromTrack())).check(isEqualTo(0L))
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
                        Transform.$(tracksFromSequence()).check(AllOf.$(
                            transform(trackListSize()).check(isEqualTo(1)),
                            transform(trackAt(0)).check(allOf(
                                transform(sizeFromTrack()).check(isEqualTo(6)),
                                transform(midiEventFromTrack(0)).check(isNotNull()),
                                transform(ticksFromTrack()).check(isEqualTo(192L)))))),
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
                        Transform.$(tracksFromSequence()).check(AllOf.$(
                            transform(trackListSize()).check(isEqualTo(1)),
                            transform(trackAt(0)).check(allOf(
                                transform(sizeFromTrack()).check(isEqualTo(6)),
                                transform(midiEventFromTrack(0)).check(isNotNull()),
                                transform(ticksFromTrack()).check(isEqualTo(192L)))))),
                        transform(tickLengthFromSequence()).check(isEqualTo(192L)))
                )))
    );
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
    return Printables.function("get[" + portName + "]", m -> m.get(portName));
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
    return Printables.function("Sequence#getTickLength", Sequence::getTickLength);
  }
  
  private static Function<Sequence, List<Track>> tracksFromSequence() {
    return Printables.function("Sequence#getTracks", seq -> Arrays.asList(seq.getTracks()));
  }
  
  
  private static Function<Track, Integer> sizeFromTrack() {
    return Printables.function("Track#size", Track::size);
  }
  
  private static Function<Track, MidiEvent> midiEventFromTrack(int index) {
    return Printables.function(() -> "Track#get[" + index + "]", t -> t.get(index));
  }
  
  private static Function<Track, Long> ticksFromTrack() {
    return Printables.function(() -> "Track#ticks", Track::ticks);
  }
}
