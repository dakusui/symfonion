package com.github.dakusui.symfonion.utils.midi;

import javax.sound.midi.MidiDevice;

public record MidiDeviceRecord(Io io, MidiDevice.Info info) {
  public enum Io {
    IN {
      @Override
      public boolean isIn() {
        return true;
      }
    },
    OUT {
      @Override
      public boolean isOut() {
        return true;
      }
    },
    IO {
      @Override
      public boolean isIn() {
        return true;
      }

      @Override
      public boolean isOut() {
        return true;
      }
    },
    UNKNOWN;

    public boolean isIn() {
      return false;
    }
    public boolean isOut() {
      return false;
    }

    static Io of(MidiDevice.Info midiDeviceInfo) {
      if (MidiUtils.isMidiDeviceForInput(midiDeviceInfo)) {
        if (MidiUtils.isMidiDeviceForOutput(midiDeviceInfo))
          return IO;
        return OUT;
      }
      if (MidiUtils.isMidiDeviceForOutput(midiDeviceInfo))
        return OUT;
      return UNKNOWN;
    }
  }

  public static MidiDeviceRecord fromMidiDeviceInfo(MidiDevice.Info midiDeviceInfo) {
    return new MidiDeviceRecord(Io.of(midiDeviceInfo), midiDeviceInfo);
  }
}
