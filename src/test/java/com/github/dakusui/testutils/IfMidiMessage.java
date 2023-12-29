package com.github.dakusui.testutils;

import com.github.dakusui.thincrest_pcond.forms.Printables;

import javax.sound.midi.MidiMessage;
import java.util.function.Predicate;

import static com.github.dakusui.symfonion.scenarios.MidiCompilerTest.toHex;
import static com.github.dakusui.testutils.IfMidiMessage.Status.*;

public enum IfMidiMessage {
  ;
  
  
  Predicate<MidiMessage> isNoteOn() {
    return Printables.predicate("isNoteOn", m -> statusOf(m) == NOTE_ON);
  }
  
  Predicate<MidiMessage> isNoteOff() {
    return Printables.predicate("isNoteOn", m -> statusOf(m) == Status.NOTE_OFF);
  }
  
  Predicate<MidiMessage> channel(Predicate<Integer> cond) {
    return Printables.predicate("channel->" + cond, m -> cond.test(channelOf(m)));
  }
  
  Predicate<MidiMessage> note(Predicate<Byte> cond) {
    return Printables.predicate("note->" + cond, m -> cond.test(data1Of(m)));
  }

  Predicate<MidiMessage> velocity(Predicate<Byte> cond) {
    return Printables.predicate("velocity->" + cond, m -> cond.test(data2Of(m)));
  }
  
  
  enum Status {
    NOTE_OFF(0x08),
    NOTE_ON(0x09),
    POLYPHONIC_AFTERTOUCH(0x0a),
    CONTROL_CHANGE(0x0b),
    PROGRAM_CHANGE(0x0c),
    CHANNEL_AFTERTOUCH(0x0d),
    PITCH_WHEEL(0x0e),
    SYSTEM_EXCLUSIVE(0x0f);
    
    private final int statusCode;
    
    Status(int statusCode) {
      this.statusCode = statusCode;
    }
    
    boolean matches(MidiMessage message) {
      return statusCode(message) == this.statusCode;
    }
    
    int statusCode(MidiMessage message) {
      return extractStatusCode(message.getMessage());
    }
    
    private static int extractStatusCode(byte[] message) {
      return message[0] >> 4;
    }
    
    public static Status statusOf(MidiMessage message) {
      for (Status status : values()) {
        if (status.matches(message))
          return status;
      }
      throw new RuntimeException("Message status unknown: " + toHex(message.getMessage()));
    }
    
    public static int channelOf(MidiMessage message) {
      return message.getMessage()[0] & 0x0f;
    }
    
    public static byte data1Of(MidiMessage message) {
      return message.getMessage()[1];
    }
    
    public static byte data2Of(MidiMessage message) {
      return message.getMessage()[2];
    }
  }
}
