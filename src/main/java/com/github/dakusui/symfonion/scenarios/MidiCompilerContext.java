package com.github.dakusui.symfonion.scenarios;

import com.github.dakusui.symfonion.core.Fraction;
import com.github.dakusui.symfonion.song.Pattern;
import com.github.dakusui.symfonion.song.Stroke;

import javax.sound.midi.Track;

public record MidiCompilerContext(Track track, int channel, Pattern.Parameters params, Fraction relativeStrokePositionInBar,
                                  long barPositionInTicks, Groove groove) {
  
  // this.position = position;
  // this.grooveAccent = grooveAccent;
  // this.strokeLengthInTicks = strokeLengthInTicks;
  
  public long getStrokeLengthInTicks(Stroke stroke) {
    return convertRelativePositionInStrokeToAbsolutePosition(stroke.length());
  }
  
  public long convertRelativePositionInStrokeToAbsolutePosition(Fraction relativePositionInStroke) {
    Groove.Unit unit = resolveRelativePositionInStroke(relativePositionInStroke);
    long relativePositionInBarInTicks = unit.pos();
    return barPositionInTicks() + relativePositionInBarInTicks;
  }
  
  public int getGrooveAccent(Fraction relPosInStroke) {
    Groove.Unit unit = resolveRelativePositionInStroke(relPosInStroke);
    return unit.accent();
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
