package com.github.dakusui.symfonion.compat.json;

import com.google.gson.JsonElement;

import java.io.Serial;
import java.util.Arrays;

/**
 * Indicates access happens at an index that a JSON array doesn't have.
 */
public class JsonIndexOutOfBoundsException extends JsonInvalidPathException {

  /**
   * A serial version UID string.
   */
  @Serial
  private static final long serialVersionUID = -1088233926881743647L;

  private final int index;

  /**
   * Creates an object of this method.
   *
   * @param base  A JSON element that holds
   * @param path  A path to the position of the array.
   * @param index An index, where out of bounds access happened.
   */
  public JsonIndexOutOfBoundsException(JsonElement base, Object[] path, int index) {
    super(base, path);
    this.index   = index;
  }

  public int getIndex() {
    return this.index;
  }

  @Override
  protected String formatMessage(JsonElement base, Object[] path) {
    return String.format("This element doesn't have path (array out of bounds): %s", Arrays.toString(path));
  }
}
