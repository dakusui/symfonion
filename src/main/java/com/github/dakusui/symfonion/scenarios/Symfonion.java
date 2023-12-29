package com.github.dakusui.symfonion.scenarios;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.json.JsonInvalidPathException;
import com.github.dakusui.json.JsonPathNotFoundException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.core.Utils;
import com.github.dakusui.symfonion.song.Keyword;
import com.github.dakusui.symfonion.song.Song;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.sound.midi.*;
import java.io.File;
import java.util.*;

import static com.github.dakusui.symfonion.core.exceptions.ExceptionThrower.*;

public class Symfonion {
  Context logiasContext;
  private String fileName;
  private JsonObject json;
  
  public Symfonion(Context logiasContext) {
    this.logiasContext = logiasContext;
  }
  
  public Song load(String fileName) throws SymfonionException {
    Song ret;
    this.fileName = fileName;
    try {
      try {
        this.json = loadSymfonionFile(fileName, new HashMap<>());
        ret = new Song.Builder(logiasContext, json).build();
      } catch (JsonSyntaxException e) {
        throw loadFileException(new File(fileName), e.getCause());
      } catch (IllegalStateException e) {
        throw loadFileException(new File(fileName), e);
      } catch (JsonPathNotFoundException e) {
        throw requiredElementMissingException(e.getLocation(), this.json, JsonUtils.formatPath(e.getPath()));
      } catch (JsonException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    } catch (SymfonionException e) {
      e.setSourceFile(new File(this.fileName));
      throw e;
    }
    return ret;
  }
  
  private static JsonObject loadSymfonionFile(String fileName, Map<String, JsonObject> alreadyReadFiles) throws SymfonionException, JsonException {
    if (alreadyReadFiles.containsKey(fileName)) return alreadyReadFiles.get(fileName);
    JsonObject ret = JsonUtils.toJson(Utils.loadFile(fileName)).getAsJsonObject();
    if (ret.has(Keyword.$include.name())) {
      File dir = new File(fileName).getParentFile();
      JsonArray includedFiles = JsonUtils.asJsonArray(ret, Keyword.$include.name());
      int i = 0;
      for (JsonElement each : includedFiles) {
        String eachFileName = JsonUtils.asString(each);
        if (eachFileName == null) {
          throw new JsonInvalidPathException(ret, new Object[]{Keyword.$include, i});
        }
        String eachAbsFileName = new File(dir, eachFileName).getAbsolutePath();
        JsonObject included = JsonUtils.toJson(Utils.loadFile(eachAbsFileName)).getAsJsonObject();
        alreadyReadFiles.put(eachAbsFileName, included);
        ret = JsonUtils.merge(ret, included);
        i++;
      }
    }
    return ret;
  }
  
  public Map<String, Sequence> compile(Song song) throws SymfonionException {
    MidiCompiler compiler = new MidiCompiler(song.getLogiasContext());
    Map<String, Sequence> ret;
    try {
      ret = compiler.compile(song);
    } catch (SymfonionException e) {
      e.setSourceFile(new File(this.fileName));
      throw e;
    } catch (InvalidMidiDataException e) {
      throw compilationException("Failed to compile a song.", e);
    }
    return ret;
  }
}
