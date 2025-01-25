package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.CliUtils;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower;
import com.github.dakusui.symfonion.compat.exceptions.ExceptionContext;
import com.github.dakusui.symfonion.core.Symfonion;
import com.github.dakusui.symfonion.song.Song;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

import static com.github.dakusui.symfonion.cli.subcommands.LogiasUtils.createLogiasContext;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.SOURCE_FILE;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.exceptionContext;
import static com.github.dakusui.symfonion.compat.exceptions.ExceptionContext.entry;


public class Compile implements Subcommand {
  @Override
  public void invoke(Cli cli, PrintStream ps, InputStream inputStream) throws IOException {
    try (ExceptionContext ignored = exceptionContext(entry(SOURCE_FILE, cli.source()))) {
      Symfonion symfonion = cli.symfonion();
      Song song = symfonion.loadSong(cli.source().getAbsolutePath(),
                                     cli.measureFilter(),
                                     cli.partFilter());
      Map<String, Sequence> sequences = symfonion.compileSong(song, createLogiasContext());

      for (String portName : sequences.keySet()) {
        Sequence seq        = sequences.get(portName);
        String   outfile    = cli.sink().getAbsolutePath();
        File     outputFile = CliUtils.composeOutputFile(outfile, portName);
        MidiSystem.write(seq, 1, outputFile);
      }
    }
  }
}
