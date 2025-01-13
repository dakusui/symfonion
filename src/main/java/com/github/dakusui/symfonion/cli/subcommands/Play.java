package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.compat.exceptions.ExceptionContext;
import com.github.dakusui.symfonion.core.Symfonion;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.song.CompatSong;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceScanner;
import com.github.dakusui.symfonion.utils.midi.MidiUtils;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.SOURCE_FILE;
import static com.github.dakusui.symfonion.compat.exceptions.ExceptionContext.entry;

public class Play implements Subcommand {

  @Override
  public void invoke(Cli cli, PrintStream ps, InputStream inputStream) throws IOException {
    try (ExceptionContext ignored = exceptionContext(entry(SOURCE_FILE, cli.source()))) {
      Symfonion symfonion = cli.symfonion();

      CompatSong            song      = symfonion.load(cli.source().getAbsolutePath(), cli.barFilter(), cli.partFilter());
      Map<String, Sequence> sequences = symfonion.compile(song);
      ps.println();
      Map<String, MidiDevice> midiOutDevices = prepareMidiOutDevices(ps, cli.midiOutRegexPatterns());
      ps.println();
      play(midiOutDevices, sequences);
    }
  }

  public static Map<String, MidiDevice> prepareMidiOutDevices(PrintStream ps, Map<String, Pattern> portDefinitions) {
    Map<String, MidiDevice> devices = new HashMap<>();
    for (String portName : portDefinitions.keySet()) {
      Pattern regex = portDefinitions.get(portName);
      ////
      // BEGIN: Trying to find an output device whose name matches the given regex
      MidiDeviceScanner scanner = MidiUtils.chooseOutputDevices(ps, regex);
      scanner.scan();
      MidiDevice.Info[] matchedInfos = MidiUtils.getInfos(portName, scanner, regex);
      // END
      ////
      try {
        devices.put(portName, MidiSystem.getMidiDevice(matchedInfos[0]));
      } catch (MidiUnavailableException e) {
        throw failedToAccessMidiDevice("out", e, matchedInfos);
      }
    }
    return devices;
  }

  private static Map<String, Sequencer> prepareSequencers(List<String> portNames, Map<String, MidiDevice> midiOutDevices, Map<String, Sequence> sequences) throws MidiUnavailableException, InvalidMidiDataException {
    Map<String, Sequencer> ret = new HashMap<>();
    final List<Sequencer> playingSequencers = new LinkedList<>();
    for (String portName : portNames) {
      MidiDevice midiOutDevice = midiOutDevices.get(portName);
      Sequence sequence = sequences.get(portName);
      final Sequencer sequencer = prepareSequencer(midiOutDevice, sequence, seq -> createMetaEventListener(playingSequencers, seq));
      playingSequencers.add(sequencer);
      ret.put(portName, sequencer);
    }
    return ret;
  }

  private static Sequencer prepareSequencer(MidiDevice midiOutDevice, Sequence sequence, Function<Sequencer, MetaEventListener> metaEventListenerFactory) throws MidiUnavailableException, InvalidMidiDataException {
    final Sequencer sequencer = MidiSystem.getSequencer();
    sequencer.open();
    connectMidiDeviceToSequencer(midiOutDevice, sequencer);
    sequencer.setSequence(sequence);
    sequencer.addMetaEventListener(metaEventListenerFactory.apply(sequencer));
    return sequencer;
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
