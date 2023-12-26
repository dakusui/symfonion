package com.github.dakusui.symfonion.scenarios;

import com.github.dakusui.json.JsonException;
import com.github.dakusui.json.JsonInvalidPathException;
import com.github.dakusui.json.JsonPathNotFoundException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.core.exceptions.SymfonionSyntaxException;
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
        ret = new Song(logiasContext, json);
        ret.init();
      } catch (JsonSyntaxException e) {
        throw loadFileException(new File(fileName), e.getCause());
      } catch (IllegalStateException e) {
        throw loadFileException(new File(fileName), e);
      } catch (JsonPathNotFoundException e) {
        throw requiredElementMissingException(e.getLocation(), JsonUtils.formatPath(e.getPath()));
      } catch (JsonException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    } catch (SymfonionSyntaxException e) {
      e.setSourceFile(new File(this.fileName));
      String path = JsonUtils.buildPathInfo(this.json).get(e.getLocation());
      e.setJsonPath(path);
      throw e;
    } catch (SymfonionException e) {
      e.setSourceFile(new File(this.fileName));
      throw e;
    }
    return ret;
  }
  
  private JsonObject loadSymfonionFile(String fileName, Map<String, JsonObject> alreadyReadFiles) throws SymfonionException, JsonException {
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
    } catch (SymfonionSyntaxException e) {
      e.setSourceFile(new File(this.fileName));
      String path = JsonUtils.buildPathInfo(this.json).get(e.getLocation());
      e.setJsonPath(path);
      throw e;
    } catch (SymfonionException e) {
      e.setSourceFile(new File(this.fileName));
      throw e;
    } catch (InvalidMidiDataException e) {
      throw compilationException("Failed to compile a song.", e);
    }
    return ret;
  }
  
  private Map<String, Sequencer> prepareSequencers(List<String> portNames, Map<String, MidiDevice> devices, Map<String, Sequence> sequences) throws MidiUnavailableException, InvalidMidiDataException {
    Map<String, Sequencer> ret = new HashMap<>();
    final List<Sequencer> playingSequencers = new LinkedList<>();
    for (String portName : portNames) {
      final Sequencer sequencer = MidiSystem.getSequencer();
      playingSequencers.add(sequencer);
      sequencer.open();
      ret.put(portName, sequencer);
      MidiDevice dev = devices.get(portName);
      if (dev != null) {
        dev.open();
        for (Transmitter tr : sequencer.getTransmitters()) {
          tr.setReceiver(null);
        }
        sequencer.getTransmitter().setReceiver(dev.getReceiver());
      }
      sequencer.setSequence(sequences.get(portName));
      sequencer.addMetaEventListener(new MetaEventListener() {
        final Sequencer seq = sequencer;
        
        @Override
        public void meta(MetaMessage meta) {
          if (meta.getType() == 0x2f) {
            synchronized (Symfonion.this) {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                throw interrupted(e);
              }
              playingSequencers.remove(this.seq);
              if (playingSequencers.isEmpty()) {
                Symfonion.this.notifyAll();
              }
            }
          }
        }
      });
    }
    return ret;
  }
  
  private void startSequencers(List<String> portNames, Map<String, Sequencer> sequencers) {
    for (String portName : portNames) {
      System.out.println("Start playing on " + portName + "(" + System.currentTimeMillis() + ")");
      sequencers.get(portName).start();
    }
  }
  
  private void cleanUpSequencers(List<String> portNames, Map<String, MidiDevice> devices, Map<String, Sequencer> sequencers) {
    List<String> tmp = new LinkedList<>(portNames);
    Collections.reverse(portNames);
    for (String portName : tmp) {
      MidiDevice dev = devices.get(portName);
      if (dev != null) {
        dev.close();
      }
      Sequencer sequencer = sequencers.get(portName);
      if (sequencer != null) {
        sequencer.close();
      }
    }
  }
  
  public synchronized void play(Map<String, MidiDevice> devices, Map<String, Sequence> sequences) throws SymfonionException {
    List<String> portNames = new LinkedList<>(sequences.keySet());
    Map<String, Sequencer> sequencers;
    try {
      sequencers = prepareSequencers(portNames, devices, sequences);
      try {
        startSequencers(portNames, sequencers);
        this.wait();
      } finally {
        System.out.println("Finished playing.");
        cleanUpSequencers(portNames, devices, sequencers);
      }
    } catch (MidiUnavailableException e) {
      throw deviceException("Midi device was not available.", e);
    } catch (InvalidMidiDataException e) {
      throw deviceException("Data was invalid.", e);
    } catch (InterruptedException e) {
      throw deviceException("Operation was interrupted.", e);
    }
  }
}
