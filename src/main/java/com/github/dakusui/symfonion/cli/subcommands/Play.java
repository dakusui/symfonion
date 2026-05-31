package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.compat.exceptions.ExceptionContext;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.core.Symfonion;
import com.github.dakusui.symfonion.song.CompatSong;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceScanner;
import com.github.dakusui.symfonion.utils.midi.MidiUtils;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.cli.subcommands.LogiasUtils.createLogiasContext;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.SOURCE_FILE;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.ExceptionContext.entry;

public class Play implements Subcommand {

  @Override
  public void invoke(Cli cli, PrintStream ps, InputStream inputStream) throws IOException {
    try (ExceptionContext ignored = exceptionContext(entry(SOURCE_FILE, cli.source()))) {
      Symfonion symfonion = cli.symfonion();

      CompatSong            song      = symfonion.load(cli.source().getAbsolutePath(), cli.barFilter(), cli.partFilter());
      Map<String, Sequence> sequences = symfonion.compile(song, createLogiasContext());
      Supplier<Map<String, Sequence>> recompiler = () -> {
        CompatSong s = symfonion.load(cli.source().getAbsolutePath(), cli.barFilter(), cli.partFilter());
        return symfonion.compile(s, createLogiasContext());
      };
      ps.println();
      Map<String, MidiDevice> midiOutDevices = prepareMidiOutDevices(ps, cli.midiOutRegexPatterns());
      ps.println();
      play(ps, midiOutDevices, sequences, recompiler);
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

  private static Map<String, Sequencer> prepareSequencers(List<String> portNames, Map<String, MidiDevice> midiOutDevices, Map<String, Sequence> sequences, List<Sequencer> playingSequencers, PrintStream ps) throws MidiUnavailableException, InvalidMidiDataException {
    Map<String, Sequencer> ret = new HashMap<>();
    for (String portName : portNames) {
      MidiDevice      midiOutDevice = midiOutDevices.get(portName);
      Sequence        sequence      = sequences.get(portName);
      final Sequencer sequencer     = prepareSequencer(midiOutDevice, sequence, seq -> createMetaEventListener(playingSequencers, seq, ps));
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

  private static MetaEventListener createMetaEventListener(List<Sequencer> playingSequencers, Sequencer sequencer, PrintStream ps) {
    return new MetaEventListener() {
      final Sequencer seq = sequencer;

      @Override
      public void meta(MetaMessage meta) {
        if (meta.getType() == 0x06) {
          ps.println(new String(meta.getData(), StandardCharsets.UTF_8));
        } else if (meta.getType() == 0x2f) {
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

  // --- Measure marker tick extraction ---

  static long[] extractMeasureMarkerTicks(Map<String, Sequence> sequences) {
    if (sequences.isEmpty()) return new long[0];
    Track[] tracks = sequences.values().iterator().next().getTracks();
    if (tracks.length == 0) return new long[0];
    Track markerTrack = tracks[tracks.length - 1];
    List<Long> ticks = new ArrayList<>();
    for (int i = 0; i < markerTrack.size(); i++) {
      MidiEvent event = markerTrack.get(i);
      if (event.getMessage() instanceof MetaMessage mm && mm.getType() == 0x06) {
        ticks.add(event.getTick());
      }
    }
    return ticks.stream().mapToLong(Long::longValue).sorted().toArray();
  }

  static long findNextTick(long[] ticks, long current) {
    for (long tick : ticks) {
      if (tick > current) return tick;
    }
    return current;
  }

  static long findPrevTick(long[] ticks, long current) {
    // Step 1: find the start of the bar we are currently in (largest tick <= current).
    long currentBarStart = 0;
    for (long tick : ticks) {
      if (tick > current) break;
      currentBarStart = tick;
    }
    // Step 2: return the tick that starts the bar before the current one.
    long prev = 0;
    for (long tick : ticks) {
      if (tick >= currentBarStart) break;
      prev = tick;
    }
    return prev;
  }

  static void seekToTick(Collection<Sequencer> sequencers, long tick) {
    boolean wasRunning = sequencers.stream().anyMatch(Sequencer::isRunning);
    for (Sequencer seq : sequencers) seq.stop();
    for (Sequencer seq : sequencers) seq.setTickPosition(tick);
    if (wasRunning) {
      for (Sequencer seq : sequencers) seq.start();
    }
  }

  // --- Terminal raw mode ---

  private static String setRawTerminalMode() {
    if (System.console() == null) return null;
    try {
      Process save = new ProcessBuilder("sh", "-c", "stty -g </dev/tty")
          .redirectErrorStream(true)
          .start();
      String saved = new String(save.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
      save.waitFor();
      new ProcessBuilder("sh", "-c", "stty cbreak -echo </dev/tty")
          .redirectErrorStream(true)
          .start()
          .waitFor();
      return saved;
    } catch (Exception e) {
      return null;
    }
  }

  private static void restoreTerminalMode(String savedSettings) {
    if (savedSettings == null || savedSettings.isEmpty()) return;
    try {
      new ProcessBuilder("sh", "-c", "stty " + savedSettings + " </dev/tty")
          .redirectErrorStream(true)
          .start()
          .waitFor();
    } catch (Exception ignored) {
    }
  }

  // --- Keyboard controller ---

  private static final class KeyboardController implements Runnable {
    private final Collection<Sequencer>   sequencers;
    private final AtomicReference<long[]> ticksRef;
    private final Runnable                onEnter;

    KeyboardController(Collection<Sequencer> sequencers, AtomicReference<long[]> ticksRef, Runnable onEnter) {
      this.sequencers = sequencers;
      this.ticksRef   = ticksRef;
      this.onEnter    = onEnter;
    }

    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          if (System.in.available() == 0) {
            Thread.sleep(20);
            continue;
          }
          int b = System.in.read();
          if (b == '\r' || b == '\n') { onEnter.run(); continue; }
          if (b != 0x1B) continue;
          // Wait up to 50 ms for the rest of the escape sequence
          long deadline = System.currentTimeMillis() + 50;
          while (System.in.available() < 2 && System.currentTimeMillis() < deadline) {
            Thread.sleep(5);
          }
          if (System.in.available() < 2) continue;
          int b2 = System.in.read();
          int b3 = System.in.read();
          if (b2 != '[') continue;
          switch (b3) {
            case 'C' -> onRight();
            case 'D' -> onLeft();
            case 'A' -> onUp();
            case 'B' -> onDown();
          }
        }
      } catch (IOException | InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    private long currentTick() {
      return sequencers.iterator().next().getTickPosition();
    }

    private void onRight() {
      seekToTick(sequencers, findNextTick(ticksRef.get(), currentTick()));
    }

    private void onLeft() {
      seekToTick(sequencers, findPrevTick(ticksRef.get(), currentTick()));
    }

    private void onUp() {
      for (Sequencer seq : sequencers) {
        if (!seq.isRunning()) seq.start();
      }
    }

    private void onDown() {
      for (Sequencer seq : sequencers) {
        if (seq.isRunning()) seq.stop();
      }
    }
  }

  // --- Main playback entry point ---

  static synchronized void play(PrintStream ps, Map<String, MidiDevice> midiOutDevices, Map<String, Sequence> sequences, Supplier<Map<String, Sequence>> recompiler) throws SymfonionException {
    List<String> portNames = new LinkedList<>(sequences.keySet());
    String       savedTty  = setRawTerminalMode();
    // Shutdown hook ensures the terminal is restored even when Ctrl-C sends SIGINT
    // (SIGINT triggers JVM shutdown without running finally blocks).
    Thread restoreHook = new Thread(() -> restoreTerminalMode(savedTty));
    Runtime.getRuntime().addShutdownHook(restoreHook);
    try {
      List<Sequencer>         playingSequencers = new LinkedList<>();
      AtomicReference<long[]> ticksRef          = new AtomicReference<>(extractMeasureMarkerTicks(sequences));
      Map<String, Sequencer>  sequencerMap      = prepareSequencers(portNames, midiOutDevices, sequences, playingSequencers, ps);
      Runnable onEnter = () -> {
        Map<String, Sequence> newSeqs;
        try {
          newSeqs = recompiler.get();
        } catch (Exception e) {
          ps.println("[recompile failed] " + e.getMessage());
          return;
        }
        synchronized (Play.class) {
          for (Sequencer seq : sequencerMap.values()) seq.stop();
          try {
            for (Map.Entry<String, Sequencer> entry : sequencerMap.entrySet())
              entry.getValue().setSequence(newSeqs.get(entry.getKey()));
          } catch (InvalidMidiDataException e) {
            ps.println("[recompile failed] " + e.getMessage());
            return;
          }
          playingSequencers.clear();
          playingSequencers.addAll(sequencerMap.values());
          ticksRef.set(extractMeasureMarkerTicks(newSeqs));
          for (Sequencer seq : sequencerMap.values()) {
            seq.setTickPosition(0);
            seq.start();
          }
          ps.println("[recompiled OK]");
        }
      };
      Thread kbThread = new Thread(new KeyboardController(sequencerMap.values(), ticksRef, onEnter));
      kbThread.setDaemon(true);
      try {
        startSequencers(portNames, sequencerMap);
        kbThread.start();
        Play.class.wait();
      } finally {
        kbThread.interrupt();
        try { Runtime.getRuntime().removeShutdownHook(restoreHook); } catch (IllegalStateException ignored) {}
        restoreTerminalMode(savedTty);
        System.out.println("Finished playing.");
        cleanUpSequencers(portNames, midiOutDevices, sequencerMap);
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
