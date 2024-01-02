package com.github.dakusui.symfonion.utils.midi;

import javax.sound.midi.MidiDevice;

import static com.github.dakusui.symfonion.utils.midi.MidiUtils.isMidiDeviceForInput;
import static com.github.dakusui.symfonion.utils.midi.MidiUtils.isMidiDeviceForOutput;

public record MidiDeviceRecord(boolean in, boolean out, MidiDevice.Info info) {
  public String io() {
    return (in() ? "I" : "") + (out() ? "O" : "");
  }

  public static MidiDeviceRecord fromMidiDeviceInfo(MidiDevice.Info midiDeviceInfo) {
    return new MidiDeviceRecord(isMidiDeviceForInput(midiDeviceInfo), isMidiDeviceForOutput(midiDeviceInfo), midiDeviceInfo);
  }
}
