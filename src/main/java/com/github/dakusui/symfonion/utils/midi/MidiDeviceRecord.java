package com.github.dakusui.symfonion.utils.midi;

import javax.sound.midi.MidiDevice;

public record MidiDeviceRecord(Io io, MidiDevice.Info info) {
  public enum Io {
    IN,
    OUT,
    UNKNOWN;

    static Io of(MidiDevice.Info midiDeviceInfo) {
      if (MidiUtils.isMidiDeviceForInput(midiDeviceInfo))
        return IN;
      if (MidiUtils.isMidiDeviceForOutput(midiDeviceInfo))
        return OUT;
      return UNKNOWN;
    }
  }

  public static MidiDeviceRecord fromMidiDeviceInfo(MidiDevice.Info midiDeviceInfo) {
    return new MidiDeviceRecord(Io.of(midiDeviceInfo), midiDeviceInfo);
  }
}
