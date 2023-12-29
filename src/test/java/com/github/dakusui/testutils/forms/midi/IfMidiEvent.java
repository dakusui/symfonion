package com.github.dakusui.testutils.forms.midi;

import com.github.dakusui.thincrest_pcond.forms.Printables;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import java.util.function.Predicate;

public class IfMidiEvent {
  Predicate<MidiEvent> tick(Predicate<Long> cond) {
    return Printables.predicate(() -> "tick->" + cond, event -> cond.test(event.getTick()));
  }
  
  Predicate<MidiEvent> message(Predicate<MidiMessage> cond) {
    return Printables.predicate(() -> "message->" + cond, event -> cond.test(event.getMessage()));
  }
}