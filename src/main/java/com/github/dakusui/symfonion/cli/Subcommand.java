package com.github.dakusui.symfonion.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public interface Subcommand {
    /**
     * A method to invoke this subcommand.
     *
     * @param cli {@link Cli} object.
     * @param ps A print stream to which this sub-command should write. Usually {@code System.err}.
     * @param inputStream An input stream from which this sub-command should read. Usually {@code System.oin}.
     * @throws IOException An IO problem (file-read, write, etc.) happened during execution.
     */
    void invoke(Cli cli, PrintStream ps, InputStream inputStream) throws IOException;
}
