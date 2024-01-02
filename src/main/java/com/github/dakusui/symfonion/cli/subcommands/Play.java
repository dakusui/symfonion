package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.core.Symfonion;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.song.Song;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.*;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.ContextKey.SOURCE_FILE;

public class Play implements Subcommand {

  @Override
  public void invoke(Cli cli, PrintStream ps, InputStream inputStream) throws IOException {
    try (Context ignored = context($(SOURCE_FILE, cli.getSourceFile()))) {
      Symfonion symfonion = cli.getSymfonion();

      Song song = symfonion.load(cli.getSourceFile().getAbsolutePath());
      Map<String, Sequence> sequences = symfonion.compile(song);
      ps.println();
      Map<String, MidiDevice> midiOutDevices = cli.prepareMidiOutDevices(ps);
      ps.println();
      play(midiOutDevices, sequences);
    }
  }

  private static Map<String, Sequencer> prepareSequencers(List<String> portNames, Map<String, MidiDevice> devices, Map<String, Sequence> sequences) throws MidiUnavailableException, InvalidMidiDataException {
    Map<String, Sequencer> ret = new HashMap<>();
    final List<Sequencer> playingSequencers = new LinkedList<>();
    for (String portName : portNames) {
      prepareSequencer(ret, portName, devices, sequences, playingSequencers);
    }
    return ret;
  }

  private static void prepareSequencer(Map<String, Sequencer> sequencers, String portName, Map<String, MidiDevice> devices, Map<String, Sequence> sequences, List<Sequencer> playingSequencers) throws MidiUnavailableException, InvalidMidiDataException {
    final Sequencer sequencer = MidiSystem.getSequencer();
    playingSequencers.add(sequencer);
    sequencers.put(portName, sequencer);
    MidiDevice midiOutDevice = devices.get(portName);
    Sequence sequence = sequences.get(portName);
    sequencer.open();
    connectMidiDeviceToSequencer(midiOutDevice, sequencer);
    sequencer.setSequence(sequence);
    sequencer.addMetaEventListener(createMetaEventListener(playingSequencers, sequencer));
  }

  private static void connectMidiDeviceToSequencer(MidiDevice midiOutDevice, Sequencer sequencer) throws MidiUnavailableException {
    if (midiOutDevice != null) {
      midiOutDevice.open();
      assignDeviceReceiverToSequencer(sequencer, midiOutDevice);
    }
  }

  private static void assignDeviceReceiverToSequencer(Sequencer sequencer, MidiDevice dev) throws MidiUnavailableException {
    for (Transmitter tr : sequencer.getTransmitters()) {
      tr.setReceiver(null);
    }
    sequencer.getTransmitter().setReceiver(dev.getReceiver());
  }

  private static MetaEventListener createMetaEventListener(List<Sequencer> playingSequencers, Sequencer sequencer) {
    return new MetaEventListener() {
      final Sequencer seq = sequencer;

      @Override
      public void meta(MetaMessage meta) {
        if (meta.getType() == 0x2f) {
          synchronized (Play.class) {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              throw interrupted(e);
            }
            playingSequencers.remove(this.seq);
            if (playingSequencers.isEmpty()) {
              Play.class.notifyAll();
            }
          }
        }
      }
    };
  }

  private static void startSequencers(List<String> portNames, Map<String, Sequencer> sequencers) {
    for (String portName : portNames) {
      System.out.println("Start playing on " + portName + "(" + System.currentTimeMillis() + ")");
      sequencers.get(portName).start();
    }
  }

  private static void cleanUpSequencers(List<String> portNames, Map<String, MidiDevice> midiOutDevices, Map<String, Sequencer> sequencers) {
    List<String> tmp = new LinkedList<>(portNames);
    Collections.reverse(portNames);
    for (String portName : tmp) {
      MidiDevice dev = midiOutDevices.get(portName);
      if (dev != null) {
        dev.close();
      }
      Sequencer sequencer = sequencers.get(portName);
      if (sequencer != null) {
        sequencer.close();
      }
    }
  }

  private static synchronized void play(Map<String, MidiDevice> midiOutDevices, Map<String, Sequence> sequences) throws SymfonionException {
    List<String> portNames = new LinkedList<>(sequences.keySet());
    Map<String, Sequencer> sequencers;
    try {
      sequencers = prepareSequencers(portNames, midiOutDevices, sequences);
      try {
        startSequencers(portNames, sequencers);
        Play.class.wait();
      } finally {
        System.out.println("Finished playing.");
        cleanUpSequencers(portNames, midiOutDevices, sequencers);
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
