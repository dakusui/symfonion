package com.github.dakusui.thincrest_cliche.javax.sound.midi;

import com.github.valid8j.pcond.forms.Printables;

import javax.sound.midi.MidiMessage;
import java.util.function.Predicate;

import static com.github.dakusui.testutils.TestUtils.toHex;
import static com.github.dakusui.thincrest_cliche.javax.sound.midi.IfMidiMessage.Status.*;

public enum IfMidiMessage {
  ;
  
  
  public static Predicate<MidiMessage> isNoteOn() {
    return Printables.predicate("isNoteOn", m -> statusOf(m) == NOTE_ON);
  }
  
  public static Predicate<MidiMessage> isNoteOff() {
    return Printables.predicate("isNoteOff", m -> statusOf(m) == Status.NOTE_OFF);
  }
  
  public static Predicate<MidiMessage> channel(Predicate<Integer> cond) {
    return Printables.predicate("channel->" + cond, m -> cond.test(channelOf(m)));
  }
  
  public static Predicate<MidiMessage> note(Predicate<Byte> cond) {
    return Printables.predicate("note->" + cond, m -> cond.test(data1Of(m)));
  }
  
  public static Predicate<MidiMessage> velocity(Predicate<Byte> cond) {
    return Printables.predicate("velocity->" + cond, m -> cond.test(data2Of(m)));
  }
  
  public static Predicate<MidiMessage> isProgramChange() {
    return Printables.predicate("isProgramChange", PROGRAM_CHANGE::matches);
  }
  
  public static Predicate<MidiMessage> isControlChange() {
    return Printables.predicate("isControlChange", CONTROL_CHANGE::matches);
  }
  
  public static Predicate<MidiMessage> programNumber(Predicate<Byte> cond) {
    return Printables.predicate("programNumber->" + cond, m -> cond.test(data1Of(m)));
  }
  
  public static Predicate<MidiMessage> control(Predicate<Byte> cond) {
    return Printables.predicate("controlNumber->" + cond, m -> cond.test(data1Of(m)));
  }
  
  public static Predicate<MidiMessage> controlData(Predicate<Byte> cond) {
    return Printables.predicate("controlData->" + cond, m -> cond.test(data2Of(m)));
  }
  
  enum Status {
    NOTE_OFF((byte) 0x80),
    NOTE_ON((byte) 0x90),
    POLYPHONIC_AFTERTOUCH((byte) 0xa0),
    CONTROL_CHANGE((byte) 0xb0),
    PROGRAM_CHANGE((byte) 0xc0),
    CHANNEL_AFTERTOUCH((byte) 0xd0),
    PITCH_WHEEL((byte) 0xe0),
    SYSTEM_EXCLUSIVE((byte) 0xf0);
    
    private final int statusCode;
    
    Status(byte statusCode) {
      this.statusCode = statusCode;
    }
    
    boolean matches(MidiMessage message) {
      return statusCode(message) == this.statusCode;
    }
    
    byte statusCode(MidiMessage message) {
      return extractStatusCode(message.getMessage());
    }
    
    private static byte extractStatusCode(byte[] message) {
      return (byte) (message[0] & (byte) 0xf0);
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
