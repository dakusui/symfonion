package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.utils.Fraction;

import java.util.List;

import static java.util.Collections.unmodifiableList;

public record NoteSet(Fraction length, List<Note> notes) {
  public NoteSet(Fraction length, List<Note> notes) {
    this.length = length;
    this.notes  = unmodifiableList(notes);
  }
}
