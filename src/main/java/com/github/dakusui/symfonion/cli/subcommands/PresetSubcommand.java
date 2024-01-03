package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.CliRecord;
import com.github.dakusui.symfonion.cli.Subcommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import static java.util.Objects.requireNonNull;

public enum PresetSubcommand implements Subcommand {
  VERSION(Version.class),
  HELP(Help.class),
  LIST(ListDevices.class),
  PLAY(Play.class),
  COMPILE(Compile.class),
  ROUTE(PatchBay.class);
  
  private final Class<? extends Subcommand> subcommandClass;
  
  PresetSubcommand(Class<? extends Subcommand> subcommandClass) {
    this.subcommandClass = requireNonNull(subcommandClass);
  }
  
  final public void invoke(CliRecord cli, PrintStream ps, InputStream inputStream) throws IOException {
    try {
      ((Subcommand) this.subcommandClass.getConstructors()[0].newInstance()).invoke(cli, ps, inputStream);
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
  }
}
