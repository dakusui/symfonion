package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.CLI;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;

import java.io.IOException;
import java.io.PrintStream;

public enum PresetSubcommand implements Subcommand {
    VERSION {
        @Override
        public void invoke(CLI cli, PrintStream ps) {
            ps.println("SyMFONION " + cli.version());
            ps.println(cli.license());
        }
    }, HELP {
        @Override
        public void invoke(CLI cli, PrintStream ps) {
            new Help().invoke(cli, ps);
        }
    }, LIST {
        @Override
        public void invoke(CLI cli, PrintStream ps) {
            new ListDevices().invoke(cli, ps);
        }
    }, PLAY {
        @Override
        public void invoke(CLI cli, PrintStream ps) throws SymfonionException, IOException {
            new Play().invoke(cli, ps);
        }
    }, COMPILE {
        @Override
        public void invoke(CLI cli, PrintStream ps) throws SymfonionException, IOException {
            new Compile().invoke(cli, ps);
        }
    }, ROUTE {
        @Override
        public void invoke(CLI cli, PrintStream ps) throws SymfonionException, IOException {
            new PatchBay().invoke(cli, ps);
        }
    };

    public abstract void invoke(CLI cli, PrintStream ps) throws SymfonionException, IOException;
}
