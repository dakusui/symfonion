package com.github.dakusui.symfonion.core;

import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.compat.json.CompatJsonException;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.compat.json.JsonInvalidPathException;
import com.github.dakusui.symfonion.compat.json.JsonPathNotFoundException;
import com.github.dakusui.symfonion.song.*;
import com.github.dakusui.symfonion.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.ExceptionContext.entry;
import static com.github.dakusui.symfonion.song.CompatSong.Builder.loadMidiDeviceProfile;

public class Symfonion {
  private JsonObject json;

  public Symfonion() {
  }

  public Song loadSong(String fileName, Predicate<Measure> barFilter, Predicate<String> partFilter) {
    Song ret;
    try (var ignored = exceptionContext(entry(ContextKey.SOURCE_FILE, new File(fileName)))) {
      try {
        this.json = loadSymfonionFile(fileName, new HashMap<>());
        ret       = new Song.Builder(json).measureFilter(barFilter)
                                          .partFilter(partFilter)
                                          .build();
      } catch (JsonSyntaxException e) {
        throw loadFileException(e.getCause());
      } catch (IllegalStateException e) {
        throw loadFileException(e);
      } catch (JsonPathNotFoundException e) {
        throw requiredElementMissingException(e.getProblemCausingNode(), this.json, CompatJsonUtils.formatPath(e.getPath()));
      } catch (CompatJsonException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    return ret;
  }

  public CompatSong load(String fileName, Predicate<Bar> barFilter, Predicate<String> partFilter) {
    CompatSong ret;
    try (var ignored = exceptionContext(entry(ContextKey.SOURCE_FILE, new File(fileName)))) {
      try {
        this.json = loadSymfonionFile(fileName, new HashMap<>());
        ret       = new CompatSong.Builder(json).barFilter(barFilter)
                                                .partFilter(partFilter)
                                                .build();
      } catch (JsonSyntaxException e) {
        throw loadFileException(e.getCause());
      } catch (IllegalStateException e) {
        throw loadFileException(e);
      } catch (JsonPathNotFoundException e) {
        throw requiredElementMissingException(e.getProblemCausingNode(), this.json, CompatJsonUtils.formatPath(e.getPath()));
      } catch (CompatJsonException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    return ret;
  }

  private static JsonObject loadSymfonionFile(String fileName, Map<String, JsonObject> alreadyReadFiles) {
    if (alreadyReadFiles.containsKey(fileName)) return alreadyReadFiles.get(fileName);
    JsonObject ret = CompatJsonUtils.toJson(Utils.loadFile(fileName)).getAsJsonObject();
    if (ret.has(Keyword.$include.name())) {
      File      dir           = new File(fileName).getParentFile();
      JsonArray includedFiles = CompatJsonUtils.asJsonArray(ret, Keyword.$include.name());
      int       i             = 0;
      for (JsonElement each : includedFiles) {
        String eachFileName = CompatJsonUtils.asString(each);
        if (eachFileName == null) {
          throw new JsonInvalidPathException(ret, new Object[]{Keyword.$include, i});
        }
        String     eachAbsFileName = new File(dir, eachFileName).getAbsolutePath();
        JsonObject included        = CompatJsonUtils.toJson(Utils.loadFile(eachAbsFileName)).getAsJsonObject();
        alreadyReadFiles.put(eachAbsFileName, included);
        ret = CompatJsonUtils.merge(ret, included);
        i++;
      }
    }
    return ret;
  }

  /**
   * Compiles a {@link CompatSong} object into a map of a part name to {@link Sequence} object.
   *
   * @param song          A song object.
   * @param logiasContext
   * @return A map from part name to a MIDI sequence object.
   */
  @Deprecated
  public Map<String, Sequence> compile(CompatSong song, Context logiasContext) {
    MidiCompiler          compiler = new MidiCompiler(loadMidiDeviceProfile(song.rootJsonObject(), logiasContext));
    Map<String, Sequence> ret;
    try {
      ret = compiler.compile(song);
    } catch (InvalidMidiDataException e) {
      throw compilationException("Failed to compile a song.", e);
    }
    return ret;
  }

  /**
   * Compiles a {@link CompatSong} object into a map of a part name to {@link Sequence} object.
   *
   * @param song          A song object.
   * @param logiasContext
   * @return A map from part name to a MIDI sequence object.
   */
  public Map<String, Sequence> compileSong(Song song, Context logiasContext) {
    MidiCompiler          compiler = new MidiCompiler(loadMidiDeviceProfile(song.rootJsonObject(), logiasContext));
    Map<String, Sequence> ret;
    try {
      ret = compiler.compile(song);
    } catch (InvalidMidiDataException e) {
      throw compilationException("Failed to compile a song.", e);
    }
    return ret;
  }
}
