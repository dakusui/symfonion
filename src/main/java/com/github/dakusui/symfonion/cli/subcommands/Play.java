package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.song.Song;
import com.github.dakusui.symfonion.core.Symfonion;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.deviceException;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.interrupted;

public class Play implements Subcommand {
    private static Map<String, Sequencer> prepareSequencers(List<String> portNames, Map<String, MidiDevice> devices, Map<String, Sequence> sequences) throws MidiUnavailableException, InvalidMidiDataException {
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
        });
      }
      return ret;
    }

    private static void startSequencers(List<String> portNames, Map<String, Sequencer> sequencers) {
      for (String portName : portNames) {
        System.out.println("Start playing on " + portName + "(" + System.currentTimeMillis() + ")");
        sequencers.get(portName).start();
      }
    }

    private static void cleanUpSequencers(List<String> portNames, Map<String, MidiDevice> devices, Map<String, Sequencer> sequencers) {
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

    private static synchronized void play(Map<String, MidiDevice> devices, Map<String, Sequence> sequences) throws SymfonionException {
      List<String> portNames = new LinkedList<>(sequences.keySet());
      Map<String, Sequencer> sequencers;
      try {
        sequencers = prepareSequencers(portNames, devices, sequences);
        try {
          startSequencers(portNames, sequencers);
          Play.class.wait();
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

    @Override
    public void invoke(Cli cli, PrintStream ps) throws SymfonionException, IOException {
        Symfonion symfonion = cli.getSymfonion();

        Song song = symfonion.load(cli.getSourceFile().getAbsolutePath());
        Map<String, Sequence> sequences = symfonion.compile(song);
        ps.println();
        Map<String, MidiDevice> devices = cli.prepareMidiOutDevices(ps);
        ps.println();
        play(devices, sequences);
    }
}
