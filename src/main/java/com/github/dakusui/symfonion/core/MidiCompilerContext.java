package com.github.dakusui.symfonion.core;

import com.github.dakusui.symfonion.song.Groove;
import com.github.dakusui.symfonion.utils.Fraction;

import javax.sound.midi.Track;

public record MidiCompilerContext(Track track,
                                  int channel,
                                  Groove groove,
                                  long absoluteBarPositionInTicks,
                                  Fraction relativeStrokePositionInBar) {
}
