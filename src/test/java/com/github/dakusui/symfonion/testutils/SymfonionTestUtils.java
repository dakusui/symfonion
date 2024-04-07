package com.github.dakusui.symfonion.testutils;

import com.github.dakusui.json.CompatJsonException;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.core.MidiCompiler;
import com.github.dakusui.symfonion.song.Song;
import com.google.gson.JsonObject;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import java.util.Map;

public enum SymfonionTestUtils {
  ;
  
  public static Map<String, Sequence> compileJsonObject(JsonObject jsonObject) throws InvalidMidiDataException, SymfonionException, CompatJsonException {
    return compileJsonObject(Context.ROOT.createChild(), jsonObject);
  }
  
  private static Map<String, Sequence> compileJsonObject(Context context, JsonObject jsonObject) throws InvalidMidiDataException, SymfonionException, CompatJsonException {
    return new MidiCompiler(context).compile(createSong(context, jsonObject));
  }
  
  private static Song createSong(Context context, JsonObject jsonObject) throws CompatJsonException, SymfonionException {
    return new Song.Builder(context, jsonObject).build();
  }
}
