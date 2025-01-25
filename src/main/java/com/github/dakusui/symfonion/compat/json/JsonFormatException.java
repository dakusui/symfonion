package com.github.dakusui.symfonion.compat.json;

import com.google.gson.JsonElement;

import java.io.Serial;

/**
 * An exception to be thrown a JSON element did not have a value that an application expects.
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
