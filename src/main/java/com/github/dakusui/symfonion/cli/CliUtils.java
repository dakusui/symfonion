package com.github.dakusui.symfonion.cli;

import com.github.dakusui.symfonion.core.exceptions.CLIException;
import org.apache.commons.cli.CommandLine;

import static java.lang.String.format;

public enum CliUtils {
  ;
  
  /**
     * A method to compose an error message for a specified option.
     * This is equivalent to call {@code composeErrMsg(msg, optionName, null}.
     *
     * @param msg        A message for the option.
     * @param optionName A short form of the option.
     * @return THe composed message.
     */
    public static String composeErrMsg(String msg, String optionName) {
      return composeErrMsg(msg, optionName, null);
    }
  
  /**
     * A method to compose an error message for a specified option.
     *
     * @param msg            A message for the option.
     * @param optionName     A short form of the option.
     * @param longOptionName A long form of the option.
     * @return The composed message.
     */
    public static String composeErrMsg(String msg, String optionName, String longOptionName) {
      if (longOptionName != null) {
        return format("(-%s/--%s) %s", optionName, longOptionName, msg);
      } else {
        return format("(-%s) %s", optionName, msg);
      }
    }
  
  static String getSingleOptionValueFromCommandLine(CommandLine cmd, String optionName)
      throws CLIException {
    String ret = cmd.getOptionValue(optionName);
    int sz = cmd.getOptionProperties(optionName).size();
    if (sz != 1) {
      String msg = composeErrMsg(format(
              "This option requires one and only one value. (found %d times)", sz),
          optionName, null);
      throw new CLIException(msg);
    }
    return ret;
  }
}
