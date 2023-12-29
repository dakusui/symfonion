package com.github.dakusui.testutils.symfonion;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.google.gson.JsonObject;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.Map;
import java.util.function.Predicate;

import static com.github.dakusui.testutils.midi.MidiTestUtils.formatMidiMessage;
import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static java.lang.String.format;

public record SymfonionTestCase(String name, JsonObject input, Predicate<Map<String, Sequence>> testOracleForOutput,
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
  
  public String toString() {
    return this.name();
  }
  
  
  public static SymfonionTestCase createPositiveTestCase(String name, JsonObject input, Predicate<Map<String, Sequence>> testOracleForOutput) {
    return new SymfonionTestCase("POSITIVE: " + name, input, testOracleForOutput, null);
  }
  
  public static SymfonionTestCase createNegativeTestCase(String name, JsonObject input, Predicate<Exception> testOracleForException) {
    return new SymfonionTestCase("NEGATIVE: " + name, input, null, testOracleForException);
  }
  
  private static Map<String, Sequence> execute(JsonObject input) throws JsonException, InvalidMidiDataException, SymfonionException {
    return SymfonionTestUtils.compileJsonObject(input);
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
