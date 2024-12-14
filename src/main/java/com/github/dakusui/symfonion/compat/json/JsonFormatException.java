package com.github.dakusui.symfonion.compat.json;

import com.google.gson.JsonElement;

import java.io.Serial;

/**
 * An exception that indicates JSON data format is not complying with **SyMFONION**'s specification.
 */
public class JsonFormatException extends CompatJsonException {

  /**
   * Creates an object of this instance.
   *
   * @param elem An element, where an error is found.
   */
  public JsonFormatException(JsonElement elem) {
    super(elem);
  }

  /**
   * A serial version UID string.
   */
  @Serial
  private static final long serialVersionUID = 7421426791291041934L;

}
