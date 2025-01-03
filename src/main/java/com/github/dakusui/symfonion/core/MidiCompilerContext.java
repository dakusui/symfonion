package com.github.dakusui.symfonion.core;

import com.github.dakusui.symfonion.song.Groove;
import com.github.dakusui.symfonion.song.PartMeasure;
import com.github.dakusui.symfonion.song.PartMeasureParameters;
import com.github.dakusui.symfonion.utils.Fraction;

import javax.sound.midi.Track;

public record MidiCompilerContext(Track track,
                                  int channel,
                                  PartMeasureParameters params,
                                  Fraction relativeStrokePositionInBar,
                                  long barPositionInTicks,
                                  Groove groove) {
  public long getPartMeasureLengthInTicks(PartMeasure partMeasure) {
    return convertRelativePositionInPartMeasureToAbsolutePosition(partMeasure.length());
  }

  public long convertRelativePositionInPartMeasureToAbsolutePosition(Fraction relativePositionInStroke) {
    Groove.Unit unit                         = resolveRelativePositionInStroke(relativePositionInStroke);
    long        relativePositionInBarInTicks = unit.pos();
    return barPositionInTicks() + relativePositionInBarInTicks;
  }

  public int getGrooveAccent(Fraction relPosInStroke) {
    Groove.Unit unit = resolveRelativePositionInStroke(relPosInStroke);
    return unit.accentDelta();
  }

  private Groove.Unit resolveRelativePositionInStroke(
      Fraction relativePositionInStroke) {
    return this.groove().resolve(
        Fraction.add(
            this.relativeStrokePositionInBar(),
            relativePositionInStroke
                    )
                                );
  }
}
