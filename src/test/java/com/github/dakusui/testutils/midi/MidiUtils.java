package com.github.dakusui.testutils.midi;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.dakusui.thincrest_pcond.forms.Printables.function;

public class MidiUtils {
  private static Function<Sequence, List<TimedMidiMessage>> sequenceToTimedMidiMessage(int trackId) {
    AtomicInteger i = new AtomicInteger(0);
    return function(
        "sequenceToTimedMidiMessage[" + trackId + "]",
        seq -> StreamSupport.stream(toMidiEventIterable(seq.getTracks()[0]).spliterator(), false)
            .map(each -> new TimedMidiMessage(i.getAndIncrement(), each.getTick(), each.getMessage()))
            .collect(Collectors.toList()));
  }
  
  private static Iterable<MidiEvent> toMidiEventIterable(Track track) {
    
    return new AbstractList<>() {
      @Override
      public MidiEvent get(int index) {
        return track.get(index);
      }
      
      @Override
      public int size() {
        return track.size();
      }
    };
  }
}
