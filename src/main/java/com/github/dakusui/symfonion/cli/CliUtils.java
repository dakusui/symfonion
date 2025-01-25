package com.github.dakusui.symfonion.cli;

import com.github.dakusui.symfonion.compat.exceptions.CliException;
import com.github.dakusui.symfonion.song.Keyword;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;

import static java.lang.String.format;

public enum CliUtils {
  ;

  /**
   * A method to compose an error message for a specified option.
   * This is equivalent to call {@code composeErrMsg(msg, optionName, null}.
   *
   * @param msg             A message for the option.
   * @param shortOptionName A short form of the option.
   * @return THe composed message.
   */
  public static String composeErrMsgForShortOption(String msg, String shortOptionName) {
    return composeErrMsgForOption(msg, shortOptionName, null);
  }

  /**
   * A method to compose an error message for a specified option.
   *
   * @param msg             A message for the option.
   * @param shortOptionName A short form of the option.
   * @param longOptionName  A long form of the option.
   * @return The composed message.
   */
  public static String composeErrMsgForOption(String msg, String shortOptionName, String longOptionName) {
    if (longOptionName != null) {
      return format("(-%s/--%s) %s", shortOptionName, longOptionName, msg);
    } else {
      return format("(-%s) %s", shortOptionName, msg);
    }
  }

  /**
   * Returns a value of a specified option.
   * This method does not return `null`.
   * In case the specified option doesn't have a value, it will throw a `CliException`.
   *
   * @param cmd        A command line object created by `Cli#parseArgs(...)` method.
   * @param optionName An option whose value should be returned.
   * @return A value of the option specified by `optionName`.
   * @throws CliException Failed to retrieve value for `optionName`. For instance, the value is missing or given more than once.
   * @see Cli#parseArgs(Options, String[])
   */
  static String getSingleOptionValueFromCommandLine(CommandLine cmd, String optionName)
  throws CliException {
    String ret = cmd.getOptionValue(optionName);
    int    sz  = cmd.getOptionProperties(optionName).size();
    if (sz != 1) {
      throw new CliException(composeErrMsgForOption(format("This option requires one and only one value. (found %d times)", sz),
                                                    optionName,
                                                    null));
    }
    return ret;
  }

  public static File composeOutputFile(String outfile, String portName) {
    if (portName == null || Keyword.$default.name().equals(portName)) {
      return new File(outfile);
    }
    File ret;
    int  lastIndexOfDot = outfile.lastIndexOf('.');
    if (lastIndexOfDot == -1) {
      ret = new File(outfile + "." + portName);
    } else {
      ret = new File(outfile.substring(0, lastIndexOfDot) + "." + portName
                     + outfile.substring(lastIndexOfDot));
    }
    return ret;
  }
}
