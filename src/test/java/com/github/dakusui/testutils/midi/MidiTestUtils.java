package com.github.dakusui.testutils.midi;

import javax.sound.midi.MidiMessage;

import static com.github.dakusui.testutils.TestUtils.toHex;

public class MidiTestUtils {
  public static String formatMidiMessage(MidiMessage message) {
    return String.format("message: %-20s: %s", message.getClass().getSimpleName(), toHex(message.getMessage()));
  }
}
