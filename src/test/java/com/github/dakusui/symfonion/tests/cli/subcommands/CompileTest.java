package com.github.dakusui.symfonion.tests.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.subcommands.Compile;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static com.github.dakusui.symfonion.testutils.json.SymfonionJsonTestUtils.composeSymfonionSongJsonObject;
import static com.github.dakusui.symfonion.testutils.json.SymfonionJsonTestUtils.sixteenBeatsGroove;
import static com.github.dakusui.testutils.TestUtils.save;
import static com.github.dakusui.testutils.json.JsonTestUtils.json;
import static java.nio.charset.StandardCharsets.UTF_8;

public class CompileTest {
  @Test
  public void test() throws ParseException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    File f = save(
        composeSymfonionSongJsonObject(
            "port1",
            json("C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;C16;"),
            sixteenBeatsGroove()).toString());
    new Compile().invoke(new Cli("-c", f.getAbsolutePath()), new PrintStream(out));
    String s = out.toString(UTF_8);
    System.out.println(s);
  }
}
