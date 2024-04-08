package com.github.dakusui.symfonion.compat.json;

import com.google.gson.*;

import java.util.*;
import java.util.Map.Entry;

import static com.github.dakusui.valid8j.Requires.require;
import static com.github.dakusui.valid8j_pcond.forms.Predicates.callp;
import static java.util.Objects.requireNonNull;

public class CompatJsonUtils {
  public static String summarizeJsonElement(JsonElement jsonElement) {
    if (jsonElement == null || jsonElement.isJsonNull()) {
      return "null";
    }
    JsonElement compact = JsonSummarizer.compactJsonElement(jsonElement, 3, 3);

    if (jsonElement.isJsonPrimitive()) {
      return compact + " (primitive)";
    }
    if (jsonElement.isJsonArray()) {
      return JsonSummarizer.focusedArray((JsonArray) compact) + " (array: size=" + jsonElement.getAsJsonArray().size() + ")";
    }
    if (jsonElement.isJsonObject()) {
      return JsonSummarizer.focusedObject((JsonObject) compact) + " (object: " + jsonElement.getAsJsonObject().entrySet().size() + " entries)";
    }
    return compact + " (unknown)";
  }

  /**
   * Returns a string representation of a path to a JSON element {@code target} in {@code root} JSON object node.
   *
   * @param target A JSON element whose path in {@code root} is searched for.
   * @param root   A root JSON object node where {@code target}'s path is searched.
   * @return A string representation of path to {@code target} in {@code root}.
   */
  public static String findPathStringOf(JsonElement target, JsonObject root) {
    return jsonpathToString(findPathOf(target, root));
  }

  public static List<Object> findPathOf(JsonElement target, JsonObject root) {
    return buildPathInfo(root).get(target);
  }

  @SafeVarargs
  public static JsonObject createSummaryJsonObjectFromPaths(JsonObject rootJsonObject, List<Object>... paths) {
    JsonObject ret = new JsonObject();
    for (List<Object> eachPath : paths) {
      ret = merge(requireJsonObject(createSummaryJsonElementFromPath(rootJsonObject, eachPath)), ret);
    }
    return ret;
  }

  private static JsonElement createSummaryJsonElementFromPath(JsonObject rootJsonObject, List<Object> path) {
    return path.isEmpty() ?
        JsonSummarizer.focusedElement(JsonSummarizer.compactJsonObject(rootJsonObject, 3, 3)) :
        path.size() == 1 ?
            summaryTopLevelElement(rootJsonObject, path.getFirst()) :
            JsonSummarizer.summaryObject(rootJsonObject, path.subList(0, path.size() - 1), path.getLast());
  }

  private static JsonElement summaryTopLevelElement(JsonObject rootJsonObject, Object topLevelAttribute) {
    assert topLevelAttribute instanceof String;
    JsonObject ret = new JsonObject();
    String topLevelAttributeName = (String) topLevelAttribute;
    ret.add(topLevelAttributeName, JsonSummarizer.focusedElement(asJsonElement(rootJsonObject, topLevelAttribute)));
    return ret;
  }

  private static JsonObject requireJsonObject(JsonElement jsonElement) {
    return require(jsonElement, callp("isJsonObject")).getAsJsonObject();
  }

  enum JsonTypes {
    OBJECT {
      @Override
      JsonObject _validate(JsonElement value) throws JsonTypeMismatchException {
        if (!value.isJsonObject()) {
          throw new JsonTypeMismatchException(value, this);
        }
        return value.getAsJsonObject();
      }
    },
    ARRAY {
      @Override
      JsonArray _validate(JsonElement value) throws JsonTypeMismatchException {
        if (!value.isJsonArray()) {
          throw new JsonTypeMismatchException(value, this);
        }
        return value.getAsJsonArray();
      }

    },
    PRIMITIVE {
      @Override
      JsonPrimitive _validate(JsonElement value)
          throws JsonTypeMismatchException {
        if (!value.isJsonPrimitive()) {
          throw new JsonTypeMismatchException(value, this);
        }
        return value.getAsJsonPrimitive();
      }
    },
    NULL {
      @Override
      JsonNull _validate(JsonElement value) throws JsonTypeMismatchException {
        if (!value.isJsonNull()) {
          throw new JsonTypeMismatchException(value, this);
        }
        return value.getAsJsonNull();
      }
    };

    abstract JsonElement _validate(JsonElement value)
        throws JsonTypeMismatchException;

    JsonElement validate(JsonElement value) throws JsonTypeMismatchException {
      if (value == null) {
        return null;
      }
      return _validate(value);
    }
  }

  public static JsonElement asJsonElementWithDefault(JsonElement base, JsonElement defaultValue, int from, Object[] path)
      throws JsonInvalidPathException {
    JsonElement ret = _asJsonElement(base, defaultValue, from, path);
    // //
    // TODO: To workaround test failure. Need to come up with more consistent
    // policy to handle JsonNull.INSTANCE.
    if (ret == null || ret == JsonNull.INSTANCE) {
      ret = defaultValue;
    }
    return ret;
  }

  public static JsonElement asJsonElement(JsonElement base, int from, Object[] path) throws JsonInvalidPathException {
    JsonElement ret = _asJsonElement(base, null, from, path);
    if (ret == null) {
      throw new JsonPathNotFoundException(base, path, from);
    }
    return ret;
  }

  private static JsonElement _asJsonElement(JsonElement base,
                                            JsonElement defaultValue, int from, Object[] path)
      throws JsonInvalidPathException {
    if (path.length == from || base == null) {
      return base;
    }
    JsonElement newbase;
    if (path[from] == null) {
      throw new JsonInvalidPathException(base, path, from); // invalid path;
    }
    if (base.isJsonObject()) {
      newbase = base.getAsJsonObject().get(path[from].toString());
    } else {
      if (base.isJsonArray()) {
        Integer index;
        if ((path[from] instanceof Integer) || (path[from] instanceof Long)
            || (path[from] instanceof Short)) {
          index = ((Number) path[from]).intValue();
        } else {
          if ((index = parseInt(path[from])) == null) {
            throw new JsonInvalidPathException(base, path, from);
          }
        }
        if (index < 0 || index >= base.getAsJsonArray().size()) {
          throw new JsonIndexOutOfBoundsException(base, path, from);
        }
        newbase = base.getAsJsonArray().get(index);
      } else if (base.isJsonPrimitive()) {
        return null;
      } else {
        // JsonNull
        return null;
      }
    }

    JsonElement ret = _asJsonElement(newbase, defaultValue, from + 1, path);

    if (ret == null) {
      if (defaultValue != null) {
        return defaultValue;
      }
    }
    return ret;
  }

  private static Integer parseInt(Object object) {
    Integer ret = null;
    try {
      String str = object.toString();
      ret = Integer.parseInt(str);
    } catch (NumberFormatException ignored) {
    }

    return ret;
  }

  public static boolean hasPath(JsonElement base, Object... path) {
    try {
      return _asJsonElement(base, null, 0, path) != null;
    } catch (JsonInvalidPathException e) {
      return false;
    }
  }

  public static JsonObject asJsonObjectWithDefault(JsonObject base,
                                                   JsonObject defaultValue, Object... path)
      throws JsonTypeMismatchException,
      JsonInvalidPathException {
    return (JsonObject) JsonTypes.OBJECT.validate(asJsonElementWithDefault(
        base, defaultValue, path));
  }

  public static JsonObject asJsonObject(JsonObject base, Object... path)
      throws JsonTypeMismatchException,
      JsonInvalidPathException {
    return asJsonObjectWithDefault(base, null, path);
  }

  public static JsonObject asJsonObjectWithPromotion(JsonElement base,
                                                     String[] prioritizedKeys, Object... path)
      throws JsonInvalidPathException,
      JsonTypeMismatchException {
    JsonObject ret;
    JsonElement elem = asJsonElement(base, path);
    if (elem.isJsonObject()) {
      ret = elem.getAsJsonObject();
    } else {
      JsonArray arr = asJsonArrayWithPromotion(base, path);
      ret = new JsonObject();
      int i = 0;
      for (JsonElement item : arr) {
        if (i >= prioritizedKeys.length) {
          if (prioritizedKeys.length == 0) {
            throw new JsonTypeMismatchException(elem,
                "An object or an empty array are acceptable");
          } else {
            throw new JsonTypeMismatchException(
                elem,
                String
                    .format(
                        "A primitive, an array whose length is less than or equal to %d, or an object are acceptable",
                        prioritizedKeys.length));
          }
        }
        String key = prioritizedKeys[i];
        ret.add(key, item);
        i++;
      }
    }
    return ret;
  }

  public static JsonArray asJsonArrayWithPromotion(JsonElement base,
                                                   Object... path) throws
      JsonInvalidPathException, JsonTypeMismatchException {
    JsonArray ret;
    JsonElement elem = asJsonElement(base, path);
    if (elem.isJsonObject()) {
      throw new JsonTypeMismatchException(elem, JsonTypes.ARRAY,
          JsonTypes.NULL, JsonTypes.PRIMITIVE);
    }
    if (elem.isJsonArray()) {
      ret = elem.getAsJsonArray();
    } else {
      ret = new JsonArray();
      ret.add(elem);
    }
    return ret;
  }

  public static JsonArray asJsonArray(JsonElement base, Object... path)
      throws CompatJsonException {
    return asJsonArrayWithDefault(base, null, path);
  }

  public static JsonArray asJsonArrayWithDefault(JsonElement base,
                                                 JsonArray defaultValue, Object... path) throws JsonTypeMismatchException,
      JsonInvalidPathException {
    return (JsonArray) JsonTypes.ARRAY.validate(asJsonElementWithDefault(base,
        defaultValue, path));
  }

  public static JsonElement asJsonElementWithDefault(JsonElement base,
                                                     JsonElement defaultValue, Object... path)
      throws JsonInvalidPathException {
    return asJsonElementWithDefault(base, defaultValue, 0, path);
  }

  public static JsonElement asJsonElement(JsonElement base, Object... path)
      throws JsonInvalidPathException {
    return asJsonElement(base, 0, path);
  }

  public static String asString(JsonElement base, Object... path)
      throws JsonTypeMismatchException,
      JsonInvalidPathException {
    JsonPrimitive prim = (JsonPrimitive) JsonTypes.PRIMITIVE
        .validate(asJsonElement(base, path));
    if (prim == null) {
      return null;
    }
    return prim.getAsString();
  }

  public static String asStringWithDefault(JsonElement base,
                                           String defaultValue, Object... path) throws JsonTypeMismatchException,
      JsonInvalidPathException {
    JsonElement dv = null;
    if (defaultValue != null) {
      dv = new JsonPrimitive(defaultValue);
    }
    JsonPrimitive prim = (JsonPrimitive) JsonTypes.PRIMITIVE
        .validate(asJsonElementWithDefault(base, dv, path));
    if (prim == null) {
      return null;
    }
    return prim.getAsString();
  }

  public static int asInt(JsonElement base, Object... path)
      throws JsonTypeMismatchException,
      JsonFormatException, JsonInvalidPathException {
    try {
      return Integer.parseInt(requireNonNull(asString(base, path)));
    } catch (NumberFormatException e) {
      throw new JsonFormatException(asJsonElement(base, path));
    }
  }

  public static int asIntWithDefault(JsonElement base, int defaultValue,
                                     Object... path) throws JsonTypeMismatchException,
      JsonFormatException, JsonInvalidPathException {
    try {
      return Integer.parseInt(requireNonNull(asStringWithDefault(base, Integer.toString(defaultValue), path)));
    } catch (NumberFormatException e) {
      throw new JsonFormatException(asJsonElement(base, path));
    }
  }

  public static long asLong(JsonElement base, Object... path)
      throws JsonTypeMismatchException,
      JsonFormatException, JsonInvalidPathException {
    try {
      return Long.parseLong(requireNonNull(asString(base, path)));
    } catch (NumberFormatException e) {
      throw new JsonFormatException(asJsonElement(base, path));
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public static long asLongWithDefault(JsonElement base, long defaultValue,
                                       Object... path) throws JsonTypeMismatchException,
      JsonFormatException, JsonInvalidPathException {
    try {
      return Long.parseLong(requireNonNull(asStringWithDefault(base, Long.toString(defaultValue), path)));
    } catch (NumberFormatException e) {
      throw new JsonFormatException(asJsonElement(base, path));
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public static float asFloat(JsonElement base, Object... path)
      throws JsonTypeMismatchException,
      JsonFormatException, JsonInvalidPathException {
    try {
      return Float.parseFloat(requireNonNull(asString(base, path)));
    } catch (NumberFormatException e) {
      throw new JsonFormatException(asJsonElement(base, path));
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public static float asFloatWithDefault(JsonElement base, float defaultValue,
                                         Object... path) throws JsonTypeMismatchException,
      JsonFormatException, JsonInvalidPathException {
    try {
      return Float.parseFloat(requireNonNull(asStringWithDefault(base, Float.toString(defaultValue), path)));
    } catch (NumberFormatException e) {
      throw new JsonFormatException(asJsonElement(base, path));
    }
  }

  public static double asDouble(JsonElement base, Object... path)
      throws CompatJsonException {
    try {
      return Double.parseDouble(requireNonNull(asString(base, path)));
    } catch (NumberFormatException e) {
      throw new JsonFormatException(asJsonElement(base, path));
    }
  }

  public static double asDoubleWithDefault(JsonElement base,
                                           double defaultValue, Object... path) throws JsonTypeMismatchException,
      JsonFormatException, JsonInvalidPathException {
    try {
      return Double.parseDouble(requireNonNull(asStringWithDefault(base, Double.toString(defaultValue), path)));
    } catch (NumberFormatException e) {
      throw new JsonFormatException(asJsonElement(base, path));
    }
  }

  public static Map<JsonElement, List<Object>> buildPathInfo(JsonObject root) {
    Map<JsonElement, List<Object>> ret = new HashMap<>();
    ret.put(root, List.of());
    List<Object> path = new LinkedList<>();
    buildPathInfo(ret, path, root);
    return ret;
  }

  private static void buildPathInfo(Map<JsonElement, List<Object>> map, List<Object> path, JsonElement elem) {
    if (!elem.isJsonNull()) {
      if (elem.isJsonArray()) {
        buildPathInfo(map, path, elem.getAsJsonArray());
      } else if (elem.isJsonObject()) {
        buildPathInfo(map, path, elem.getAsJsonObject());
      }
      map.put(elem, new ArrayList<>(path));
    }
  }

  private static void buildPathInfo(Map<JsonElement, List<Object>> map, List<Object> path, JsonArray arr) {
    int len = arr.size();
    for (int i = 0; i < len; i++) {
      path.add(i);
      buildPathInfo(map, path, arr.get(i));
      path.removeLast();
    }
  }

  private static void buildPathInfo(Map<JsonElement, List<Object>> map, List<Object> path, JsonObject obj) {
    for (Entry<String, JsonElement> ent : obj.entrySet()) {
      String k = ent.getKey();
      JsonElement elem = ent.getValue();
      path.add(k);
      buildPathInfo(map, path, elem);
      path.removeLast();
    }
  }

  private static String jsonpathToString(List<Object> path) {
    StringBuilder buf = new StringBuilder();
    for (Object each : path) {
      if (each instanceof Number) {
        buf.append("[").append(each).append("]");
      } else {
        buf.append(".").append(quoteIfNonAlphanumericalIsContained(each));
      }
    }
    if (buf.isEmpty())
      buf.append('.');
    return buf.toString();
  }

  private static String quoteIfNonAlphanumericalIsContained(Object obj) {
    if (obj instanceof String s) {
      return s.matches("[a-zA-Z_][a-zA-Z0-9_]+") ? s : '"' + escape(s) + '"';
    }
    return Objects.toString(obj);
  }


  /**
   * <a href="https://stackoverflow.com/a/61628600/820227">Answer by Dan in stackoverflow.com</a>
   * escape()
   * Escape a give String to make it safe to be printed or stored.
   *
   * @param s The input String.
   * @return The output String.
   **/
  private static String escape(String s) {
    return s.replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\b", "\\b")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\f", "\\f")
        .replace("\'", "\\'")      // <== not necessary
        .replace("\"", "\\\"");
  }

  public static JsonElement toJson(String str) {
    return JsonParser.parseString(str);
  }

  public static Iterator<String> keyIterator(final JsonObject json) {
    return new Iterator<>() {
      final Iterator<Entry<String, JsonElement>> iEntries = json.entrySet()
          .iterator();

      public boolean hasNext() {
        return iEntries.hasNext();
      }

      public String next() {
        return iEntries.next().getKey();
      }

      public void remove() {
        iEntries.remove();
      }
    };
  }

  public static String formatPath(Object... relPath) {
    StringBuilder ret = new StringBuilder();
    for (Object obj : relPath) {
      if (!ret.isEmpty())
        ret.append("/");
      ret.append(obj);
    }
    return ret.toString();
  }

  public static JsonObject merge(JsonObject left, JsonObject right) throws JsonInvalidPathException {
    return merge(left, right, new LinkedList<>());
  }

  public static JsonObject merge(JsonObject left, JsonObject right, List<Object> relPath) throws JsonInvalidPathException {
    JsonObject ret = new JsonObject();
    for (Entry<String, JsonElement> each : left.entrySet()) {
      if (right.has(each.getKey())) {
        if (each.getValue().isJsonObject() && right.get(each.getKey()).isJsonObject()) {
          ret.add(
              each.getKey(),
              merge(each.getValue().getAsJsonObject(), right.get(each.getKey()).getAsJsonObject())
          );
        } else {
          throw new JsonInvalidPathException(right, relPath.toArray());
        }
      } else {
        ret.add(each.getKey(), each.getValue());
      }
    }
    for (Entry<String, JsonElement> each : right.entrySet()) {
      if (!ret.has(each.getKey())) {
        ret.add(each.getKey(), each.getValue());
      }
    }
    return ret;
  }
}
