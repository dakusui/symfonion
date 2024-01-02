package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.CliUtils;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.exceptions.ExceptionThrower;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.symfonion.core.Symfonion;
import com.github.dakusui.symfonion.song.Song;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.$;

public class Compile implements Subcommand {
    @Override
    public void invoke(Cli cli, PrintStream ps, InputStream inputStream) throws SymfonionException, IOException {
        try (ExceptionThrower.Context ignored = ExceptionThrower.context($(ExceptionThrower.ContextKey.SOURCE_FILE, cli.getSourceFile()))) {
            Symfonion symfonion = cli.getSymfonion();
            Song song = symfonion.load(cli.getSourceFile().getAbsolutePath());
            Map<String, Sequence> sequences = symfonion.compile(song);

            for (String portName : sequences.keySet()) {
                Sequence seq = sequences.get(portName);
                String outfile = cli.getSinkFile().getAbsolutePath();
                File outputFile = CliUtils.composeOutputFile(outfile, portName);
                MidiSystem.write(seq, 1, outputFile);
            }
        }
    }
}
