package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.compat.exceptions.ExceptionContext;
import com.github.dakusui.symfonion.core.Symfonion;
import com.github.dakusui.symfonion.song.Song;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Sequence;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

import static com.github.dakusui.symfonion.cli.subcommands.LogiasUtils.createLogiasContext;
import static com.github.dakusui.symfonion.cli.subcommands.Play.play;
import static com.github.dakusui.symfonion.cli.subcommands.Play.prepareMidiOutDevices;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.SOURCE_FILE;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.exceptionContext;
import static com.github.dakusui.symfonion.compat.exceptions.ExceptionContext.entry;

public class PlaySong implements Subcommand {
  @Override
  public void invoke(Cli cli, PrintStream ps, InputStream inputStream) throws IOException {
    try (ExceptionContext ignored = exceptionContext(entry(SOURCE_FILE, cli.source()))) {
      Symfonion symfonion = cli.symfonion();

      Song                  song      = symfonion.loadSong(cli.source().getAbsolutePath(), cli.measureFilter(), cli.partFilter());
      Map<String, Sequence> sequences = symfonion.compileSong(song, createLogiasContext());
      ps.println();
      Map<String, MidiDevice> midiOutDevices = prepareMidiOutDevices(ps, cli.midiOutRegexPatterns());
      ps.println();
      play(midiOutDevices, sequences);
    }
  }
}
