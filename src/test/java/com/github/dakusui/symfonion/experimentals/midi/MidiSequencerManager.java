package com.github.dakusui.symfonion.experimentals.midi;

import com.github.dakusui.symfonion.cli.subcommands.Play;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceManager;

import javax.sound.midi.*;
import java.util.*;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.utils.midi.MidiDeviceManager.isMidiDeviceForOutput;
import static com.github.dakusui.symfonion.utils.midi.MidiDeviceManager.matchesPortNameInDefinitions;

public class MidiSequencerManager {
  final List<Sequencer> playingSequencers = new LinkedList<>();
  final Map<String, Sequencer> sequencers = new HashMap<>();

  final MidiDeviceManager deviceManager;

  public MidiSequencerManager(MidiDeviceManager deviceManager) {
    this.deviceManager = deviceManager;
  }

  public Sequencer openSequencerFor(String portName, Map<String, Sequence> sequences, Map<String, Pattern> outportDefinitions) {
    final Sequencer sequencer;
    try {
      sequencer = openSequencer(sequences.get(portName), this.deviceManager.openMidiDevice(
          this.deviceManager.lookUp(isMidiDeviceForOutput().and(matchesPortNameInDefinitions(portName, outportDefinitions)))
      ).getReceiver());
    } catch (MidiUnavailableException e) {
      throw new RuntimeException(e);
    }
    return sequencer;
  }

  private Sequencer openSequencer(Sequence sequence, Receiver receiver) throws RuntimeException {
    try {
      Sequencer sequencer = MidiSystem.getSequencer();
      sequencer.open();
      for (Transmitter tr : sequencer.getTransmitters())
        tr.setReceiver(null);
      this.playingSequencers.add(sequencer);
      sequencer.getTransmitter().setReceiver(receiver);
      sequencer.setSequence(sequence);
      sequencer.addMetaEventListener(createMetaEventListenerFor0x2F(playingSequencers, sequencer));
      return sequencer;
    } catch (MidiUnavailableException e) {
      throw failedToGetTransmitter();
    } catch (InvalidMidiDataException e) {
      throw failedToSetSequence();
    }
  }


  private static void prepareSequencer(String portName, Map<String, MidiDevice> devices, Map<String, Sequence> sequences, List<Sequencer> playingSequencers, Map<String, Sequencer> sequencers) throws MidiUnavailableException, InvalidMidiDataException {
    final Sequencer sequencer = MidiSystem.getSequencer();
    playingSequencers.add(sequencer);
    sequencer.open();
    sequencers.put(portName, sequencer);
    MidiDevice dev = devices.get(portName);
    if (dev != null) {
      dev.open();
      for (Transmitter tr : sequencer.getTransmitters()) {
        tr.setReceiver(null);
      }
      sequencer.getTransmitter().setReceiver(dev.getReceiver());
    }
    sequencer.setSequence(sequences.get(portName));
    sequencer.addMetaEventListener(createMetaEventListenerFor0x2F(playingSequencers, sequencer));
  }

  private static MetaEventListener createMetaEventListenerFor0x2F(List<Sequencer> playingSequencers, Sequencer sequencer) {
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

}
