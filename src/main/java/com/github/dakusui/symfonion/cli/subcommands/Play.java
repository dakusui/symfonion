package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;
import com.github.dakusui.symfonion.song.Song;
import com.github.dakusui.symfonion.scenarios.Symfonion;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Sequence;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

public class Play implements Subcommand {
    @Override
    public void invoke(Cli cli, PrintStream ps) throws SymfonionException, IOException {
        Symfonion symfonion = cli.getSymfonion();

        Song song = symfonion.load(cli.getSourceFile().getAbsolutePath());
        Map<String, Sequence> sequences = symfonion.compile(song);
        ps.println();
        Map<String, MidiDevice> devices = cli.prepareMidiOutDevices(ps);
        ps.println();
        symfonion.play(devices, sequences);

    }
}
