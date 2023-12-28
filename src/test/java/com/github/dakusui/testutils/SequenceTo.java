package com.github.dakusui.testutils;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.thincrest_pcond.forms.Printables.function;

public enum SequenceTo {
  ;
  
  public static Function<Sequence, List<Track>> trackList() {
    return function("Sequence#getTracks", seq -> Arrays.asList(seq.getTracks()));
  }
}
