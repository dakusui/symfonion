package com.github.dakusui.symfonion.compat.exceptions;

import java.io.Serial;

import static java.lang.String.format;

/**
 * An exception used to indicate an invalid fraction string is found.
 */
public class FractionFormatException extends Exception {

  /**
   * A serial version UID string.
   */
  @Serial
  private static final long serialVersionUID = -8791776177337740280L;


  /**
   * Creates an object of this class.
   *
   * @param invalidFractionString An invalid fraction string.
   */
  public FractionFormatException(String invalidFractionString) {
    super(format("'%s' is an invalid fraction", invalidFractionString));
  }
}
