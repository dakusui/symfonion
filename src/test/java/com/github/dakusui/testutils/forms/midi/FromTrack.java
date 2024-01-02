package com.github.dakusui.testutils.forms.midi;

import com.github.dakusui.testutils.midi.PrintableMidiMessage;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;
import java.util.AbstractList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.thincrest_pcond.forms.Predicates.alwaysTrue;
import static com.github.dakusui.thincrest_pcond.forms.Printables.function;

public enum FromTrack {
  ;
  
  public static Function<Track, Integer> toSize() {
    return function("Track#size", Track::size);
  }
  
  public static Function<Track, MidiEvent> toMidiEventAt(int index) {
    return function(() -> "Track#get[" + index + "]", t -> t.get(index));
  }
  
  public static Function<Track, Long> toTicks() {
    return function(() -> "Track#ticks", Track::ticks);
  }
  
  public static Function<Track, Stream<MidiEvent>> midiEventStream() {
    return function(() -> "midiEventStream", track -> new AbstractList<MidiEvent>() {
      @Override
      public int size() {
        return track.size();
      }
      
      @Override
      public MidiEvent get(int index) {
        return track.get(index);
      }
    }.stream());
  }
  
  public static Function<Track, Stream<MidiMessage>> toMidiMessageStream() {
    return toMidiMessageStream(alwaysTrue());
  }
  
  public static Function<Track, Stream<MidiMessage>> toMidiMessageStream(Predicate<MidiMessage> cond) {
    return function(() -> midiEventStream() + "->map[.message]->filter[" + cond + "]",
        (Track track) -> midiEventStream()
            .apply(track)
            .map(MidiEvent::getMessage)
            .map(message -> new PrintableMidiMessage(message.getMessage()))
            .map(message -> (MidiMessage) message)
            .filter(cond));
  }
}
