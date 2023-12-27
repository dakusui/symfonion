package com.github.dakusui.symfonion.scenarios;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.song.Song;
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

import static com.github.dakusui.json.JsonTestUtils.*;
import static com.github.dakusui.symfonion.scenarios.Cliche.collectionSize;
import static com.github.dakusui.symfonion.scenarios.MidiCompilerTest.TestCase.createNormalTestCase;
import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static com.github.dakusui.thincrest_pcond.forms.Functions.elementAt;
import static com.github.dakusui.thincrest_pcond.forms.Functions.size;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.*;

@RunWith(Parameterized.class)
public class MidiCompilerTest {
  public record TestCase(JsonObject input, Predicate<Map<String, Sequence>> testOracleForOutput,
                         Predicate<Exception> testOracleForException) {
    public void executeAndVerify() {
      try {
        this.verifyOutput(execute(this.input));
      } catch (Exception e) {
        this.verifyException(e);
      }
    }
    
    public void verifyOutput(Map<String, Sequence> output) {
      assertThat(this.testOracleForException, isNull());
      assertThat(output, this.testOracleForOutput());
    }
    
    private void verifyException(Exception e) {
      assertThat(this.testOracleForOutput, isNull());
      assertThat(e, this.testOracleForException());
    }
    
    public static TestCase createNormalTestCase(JsonObject input, Predicate<Map<String, Sequence>> testOracleForOutput) {
      return new TestCase(input, testOracleForOutput, null);
    }
    
    public static TestCase createNgativeTestCase(JsonObject input, Predicate<Exception> testOracleForException) {
      return new TestCase(input, null, testOracleForException);
    }
    
    private static Map<String, Sequence> execute(JsonObject input) throws JsonException, InvalidMidiDataException, SymfonionException {
      return MidiCompilerTest.compileJsonObject(input);
    }
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
    return testCases().stream().map(c -> new Object[]{c}).collect(Collectors.toList());
  }
  
  public static List<TestCase> testCases() {
    return Arrays.asList(
        createNormalTestCase(
            object(
                $("$settings", object()),
                $("$parts", object()),
                $("$patterns", object()),
                $("$sequence", array())),
            transform(compiledSong_keySet()).check(isEmpty())),
        createNormalTestCase(
            object(
                $("$settings", object()),
                $("$parts", object()),
                $("$patterns", object()),
                $("$sequence", array(
                    object(
                        $("$beats", json("8/4")),
                        $("$patterns", object()))
                ))),
            transform(compiledSong_keySet()).check(isEmpty())),
        createNormalTestCase(
            object(
                $("$settings", object()),
                $("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1")))))),
                $("$patterns", object()),
                $("$sequence", array(
                    object(
                        $("$beats", json("8/4")),
                        $("$patterns", object($("piano", array()))))
                ))),
            allOf(
                transform(compiledSong_keySet().andThen(castToObjectCollection())).check(allOf(
                    transform(size()).check(isEqualTo(1)),
                    transform(toList().andThen(elementAt(0))).check(Predicates.isEqualTo("port1")))),
                transform(compiledSong_getSequence("port1")).check(
                    allOf(
                        transform(tracksFromSequence()).check(
                            allOf(
                                transform(trackListSize()).check(isEqualTo(1)),
                                transform(trackAt(0).andThen(sizeFromTrack())).check(isEqualTo(1)),
                                transform(trackAt(0).andThen(midiEventFromTrack(0))).check(isNotNull()),
                                transform(trackAt(0).andThen(ticksFromTrack())).check(isEqualTo(0L))
                            )),
                        transform(patchListFromSequence()).check(isEmpty()),
                        transform(tickLengthFromSequence()).check(isEqualTo(0L))
                    )))),
        
        createNormalTestCase(
            object(
                $("$settings", object()),
                $("$parts", object($("piano", object($("$channel", json(0)), $("$port", json("port1")))))),
                $("$patterns", object($("pg-change-to-piano", array(object($("$notes", json("C")), $("length", json("2")), $("$program", json(0))))))),
                $("$sequence", array(
                    object(
                        $("$beats", json("8/4")),
                        $("$patterns", object($("piano", object($("$body", array("pg-change-to-piano")))))))
                ))),
            allOf(
                transform(compiledSong_keySet().andThen(castToObjectCollection())).check(allOf(
                    transform(size()).check(isEqualTo(1)),
                    transform(toList().andThen(elementAt(0))).check(Predicates.isEqualTo("port1")))),
                transform(compiledSong_getSequence("port1")).check(
                    allOf(
                        transform(tracksFromSequence()).check(allOf(
                            transform(trackListSize()).check(isEqualTo(1)),
                            transform(trackAt(0).andThen(sizeFromTrack())).check(isEqualTo(1)),
                            transform(trackAt(0).andThen(midiEventFromTrack(0))).check(isNotNull()),
                            transform(trackAt(0).andThen(ticksFromTrack())).check(isEqualTo(0L)))),
                        transform(patchListFromSequence()).check(isEmpty()),
                        transform(tickLengthFromSequence()).check(isEqualTo(0L))
                    ))))
    );
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
  
  private static <E> Function<Collection<E>, List<E>> toList() {
    return Printables.function("toList", ArrayList::new);
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
  
  
  private static Function<Sequence, List<Patch>> patchListFromSequence() {
    return Printables.function("Sequence#getPatchList", seq -> Arrays.asList(seq.getPatchList()));
  }
  
  private static Function<Sequence, Long> tickLengthFromSequence() {
    return Printables.function("Sequence#getTickLength", Sequence::getTickLength);
  }
  
  private static Function<Sequence, List<Track>> tracksFromSequence() {
    return Printables.function("Sequence#getTracks", seq -> Arrays.asList(seq.getTracks()));
  }
  
  private static Function<Patch, Integer> bankFromPatch() {
    return Printables.function("Patch#getBank", Patch::getBank);
  }
  
  private static Function<Patch, Integer> programFromPatch() {
    return Printables.function("Patch#getProgram", Patch::getProgram);
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
