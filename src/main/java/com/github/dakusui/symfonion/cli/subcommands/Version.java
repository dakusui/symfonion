package com.github.dakusui.symfonion.cli.subcommands;

import com.github.dakusui.symfonion.cli.Cli;
import com.github.dakusui.symfonion.cli.Subcommand;
import com.github.dakusui.symfonion.exceptions.SymfonionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

public class Version implements Subcommand {
    public static String license() {
      return """
          Copyright 2013 Hiroshi Ukai.
          
          Licensed under the Apache License, Version 2.0 (the "License");you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:
          
          https://www.apache.org/licenses/LICENSE-2.0
          
          Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.""";
    }
    
    public static String version() {
      String path = "/META-INF/maven/com.github.dakusui/symfonion/pom.properties";
      Properties props = new Properties();
      String version = "(N/A)";
      InputStream stream = Cli.class.getResourceAsStream(path);
      if (stream != null) {
        try {
          props.load(stream);
          version = props.getProperty("version");
        } catch (IOException ignored) {
        }
      }
      return version;
    }
    
    @Override
    public void invoke(Cli cli, PrintStream ps, InputStream inputStream) throws SymfonionException, IOException {
        ps.println("SyMFONION " + version());
        ps.println(license());
    }
}
