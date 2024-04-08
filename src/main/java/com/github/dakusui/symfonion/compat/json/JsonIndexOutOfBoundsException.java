package com.github.dakusui.symfonion.compat.json;

import java.io.Serial;
import java.util.Arrays;

import com.google.gson.JsonElement;

public class JsonIndexOutOfBoundsException extends JsonInvalidPathException {

  /**
   * A serial version UID string.
   */
  @Serial
  private static final long serialVersionUID = -1088233926881743647L;


  public JsonIndexOutOfBoundsException(JsonElement base, Object[] path, int index) {
    super(base, path, index);
  }

  @Override
  protected String formatMessage(JsonElement base, Object[] path) {
    return String.format("This element doesn't have path (array out of bounds): %s", Arrays.toString(path));
  }
}
