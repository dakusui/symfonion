package com.github.dakusui.symfonion.compat.json;

import com.google.gson.JsonElement;

import java.io.Serial;
import java.util.Arrays;

/**
 * An exception that indicates a path not valid in a given JSON element was accessed.
 */
public class JsonInvalidPathException extends CompatJsonException {
  /**
   * A serial version UID string.
   */
  @Serial
  private static final long serialVersionUID = 7832147182391783569L;

  private final JsonElement base;

  private final Object[] path;

  private final String message;

  public JsonInvalidPathException(JsonElement base, Object[] path) {
    super(base);
    this.message = formatMessage(base, path);
    this.base    = base;
    this.path    = path;
  }

  public JsonElement getProblemCausingNode() {
    return base;
  }

  public Object[] getPath() {
    return path;
  }


  @Override
  public String getMessage() {
    return this.message;
  }

  /**
   * Formats a message for an error that indicates a given `path` caused  a problem in a JSON element `base`.
   *
   * @param base A JSON element, for which a problem is reported.
   * @param path A path that caused a problem in `base`.
   * @return A formatted message.
   */
  protected String formatMessage(JsonElement base, Object[] path) {
    return String.format("This element (%s) doesn't have path: %s", JsonSummarizer.focusedElement(base), Arrays.toString(path));
  }
}
