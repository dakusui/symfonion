package com.github.dakusui.testutils;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import java.util.function.Function;

import static com.github.dakusui.thincrest_pcond.forms.Printables.function;

public enum TrackTo {
  ;
  
  public static Function<Track, Integer> size() {
    return function("Track#size", Track::size);
  }
  
  public static Function<Track, MidiEvent> midiEventAt(int index) {
    return function(() -> "Track#get[" + index + "]", t -> t.get(index));
  }
  
  public static Function<Track, Long> ticks() {
    return function(() -> "Track#ticks", Track::ticks);
  }
}
