package com.github.dakusui.testutils.midi;

import javax.sound.midi.MidiMessage;

import static com.github.dakusui.symfonion.scenarios.MidiCompilerTest.toHex;

public class PrintableMidiMessage extends MidiMessage {
  /**
   * Constructs a new {@code MidiMessage}. This protected constructor is
   * called by concrete subclasses, which should ensure that the data array
   * specifies a complete, valid MIDI message.
   *
   * @param data an array of bytes containing the complete message. The
   *             message data may be changed using the {@code setMessage} method.
   * @see #setMessage
   */
  public PrintableMidiMessage(byte[] data) {
    super(data);
  }
  
  @Override
  public PrintableMidiMessage clone() {
    return new PrintableMidiMessage(this.getMessage());
  }
  
  public String toString() {
    return toHex(this.getMessage());
  }
}
