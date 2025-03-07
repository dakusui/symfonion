package com.github.dakusui.thincrest_cliche.javax.sound.midi;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.github.valid8j.pcond.forms.Printables.function;

public enum SequenceTo {
  ;
  
  public static Function<Sequence, List<Track>> trackList() {
    return function("Sequence#getTracks", seq -> Arrays.asList(seq.getTracks()));
  }
  
  public static Function<Sequence, Long> tickLength() {
    return function("Sequence#getTickLength", Sequence::getTickLength);
  }
}
