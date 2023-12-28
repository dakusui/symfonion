package com.github.dakusui.testutils.midi;

import com.github.dakusui.symfonion.scenarios.MidiCompilerTest;

import javax.sound.midi.MidiMessage;

public record TimedMidiMessage(int index, long ticks, MidiMessage msg) {
  String message() {
    return MidiCompilerTest.toHex(msg.getMessage());
  }
  
  boolean isNoteOn() {
    return message().startsWith("9");
  }
  
  boolean isNoteOff() {
    return message().startsWith("8");
  }
  
  String channelInHex() {
    return message().substring(1, 2);
  }
  
  String noteInHex() {
    return message().substring(2, 4);
  }
  
  @Override
  public String toString() {
    return String.format("message: %3s %4s %s", index(), ticks(), message());
  }
}
