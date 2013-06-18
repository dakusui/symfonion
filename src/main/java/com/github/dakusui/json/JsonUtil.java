package com.github.dakusui.json;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JsonUtil {
	static final ThreadLocal<JsonParser> jsonParser;
	static {
		jsonParser = new ThreadLocal<JsonParser>();
	}
	
	static enum JsonTypes {
		Object {
			@Override
			JsonObject _validate(JsonElement value) throws JsonTypeMismatchException {
				if (!value.isJsonObject()) {
					throw new JsonTypeMismatchException(value, this.name());
				}
				return value.getAsJsonObject();
			}
		},
		Array {
			@Override
			JsonArray _validate(JsonElement value) throws JsonTypeMismatchException {
				if (!value.isJsonArray()) {
					throw new JsonTypeMismatchException(value, this.name());
				}
				return value.getAsJsonArray();
			}

		},
		Primitive {
			@Override
			JsonPrimitive _validate(JsonElement value) throws JsonTypeMismatchException {
				if (!value.isJsonPrimitive()) {
					throw new JsonTypeMismatchException(value, this.name());
				}
				return value.getAsJsonPrimitive();
			}
		},
		Null {
			@Override
			JsonNull _validate(JsonElement value) throws JsonTypeMismatchException {
				if (!value.isJsonNull()) {
					throw new JsonTypeMismatchException(value, this.name());
				}
				return value.getAsJsonNull();
			}
		};
		
		abstract JsonElement _validate(JsonElement value) throws JsonTypeMismatchException;
		JsonElement validate(JsonElement value) throws JsonTypeMismatchException {
			if (value == null) {
				return value;
			}
			return _validate(value);
		}
	}
	
	private static JsonElement asJsonElementWithDefault(JsonElement base, JsonElement defaultValue, int from,  Object[] path) throws JsonPathNotFoundException, JsonInvalidPathException {
		JsonElement ret = _asJsonElement(base, defaultValue, from, path);
		if (ret == null) {
			ret = defaultValue;
		}
		return ret;
	}
	
	private static JsonElement asJsonElement(JsonElement base, int from,  Object[] path) throws JsonPathNotFoundException, JsonInvalidPathException {
		JsonElement ret = _asJsonElement(base, null, from, path);
		if (ret == null) {
			throw new JsonPathNotFoundException(base, path, from);
		}		
		return ret;
	}

	private static JsonElement _asJsonElement(JsonElement base, JsonElement defaultValue, int from,  Object[] path) throws JsonInvalidPathException {
		if (path.length == from || base == null) {
			return base;
		}
		JsonElement newbase = null;
		if (path[from] == null) {
			throw new JsonInvalidPathException(base, path, from); // invalid path;
		}
		if (base.isJsonObject()) {
			newbase = base.getAsJsonObject().get(path[from].toString());
		} else if (base.isJsonArray()) {
			Integer index;
			if ( 
				 (path[from] instanceof Integer) || 
				 (path[from] instanceof Long) ||
				 (path[from] instanceof Short)
					) {
				index = ((Number)path[from]).intValue();
			} else {
				if ( (index = parseInt(path[from])) == null ) {
					throw new JsonInvalidPathException(base, path, from);
				}
			}
			if (index < 0 || index >= base.getAsJsonArray().size()) {
				throw new JsonIndexOutOfBoudsException(base, path, from);
			}
			newbase = base.getAsJsonArray().get(((Number)index).intValue());
		} else if (base.isJsonPrimitive()) {
			return null;
		} else {
			// JsonNull
			return null;
		}

		JsonElement ret = _asJsonElement(
				newbase, 
				defaultValue,
				from + 1,
				path
		);
		
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
		} catch (NumberFormatException e) {}
		
		return ret;
	}
	
	public static boolean hasPath(JsonElement base, Object... path) {
		try {
			return _asJsonElement(base, null, 0, path) != null;
		} catch (JsonInvalidPathException e) {
			return false;
		}
	}

	public static JsonObject asJsonObjectWithPromotion(
			JsonObject base, 
			String[] arrayElementsPromoteTo, 
			JsonObject defaultValue, 
			Object... path
			) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException {
		try {
			return asJsonObjectWithDefault(base, defaultValue, path);
		} catch (JsonTypeMismatchException e) {
			JsonObject ret = new JsonObject();
			JsonArray arr = asJsonArrayWithPromotion(base, null, path);
			for (int i = 0; i < arrayElementsPromoteTo.length && i < arr.size(); i++) {
				ret.add(arrayElementsPromoteTo[i], arr.get(i));
			}
			return ret;
		}
	}
	
	public static JsonObject asJsonObjectWithDefault(JsonObject base,
			JsonObject defaultValue, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException {
		return (JsonObject)JsonTypes.Object.validate(asJsonElementWithDefault(base, defaultValue, path));
	}

	public static JsonObject asJsonObject(JsonObject base, Object... path) throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
		return asJsonObjectWithDefault(base, null, path);
	}
	
	public static JsonArray asJsonArrayWithPromotion(
			JsonElement base, 
			JsonArray defaultValue, Object... path) throws JsonPathNotFoundException, JsonInvalidPathException  {
		try {
			return asJsonArrayWithDefault(base, defaultValue, path);
		} catch (JsonTypeMismatchException e) {
			JsonArray ret = new JsonArray();
			ret.add(asJsonElementWithDefault(base, null, path));
			return ret;
		}
	}
	
	public static JsonArray asJsonArray(JsonElement base, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException {
		return asJsonArrayWithDefault(base, null, path);
	}
	
	public static JsonArray asJsonArrayWithDefault(JsonElement base, JsonArray defaultValue, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException  {
		return (JsonArray) JsonTypes.Array.validate(asJsonElementWithDefault(base, defaultValue, path));
	}
	
	public static JsonElement asJsonElementWithDefault(JsonElement base, JsonElement defaultValue, Object... path) throws JsonPathNotFoundException, JsonInvalidPathException {
		return asJsonElementWithDefault(base, defaultValue, 0, path);
	}

	public static JsonElement asJsonElement(JsonElement base, Object... path) throws JsonPathNotFoundException, JsonInvalidPathException {
		return asJsonElement(base, 0, path);
	}
	
	public static String asString(JsonElement base, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException  {
		JsonPrimitive prim = (JsonPrimitive) JsonTypes.Primitive.validate(asJsonElement(base, path));
		if (prim == null) {
			return null;
		}
		return prim.getAsString();
	}
	
	public static String asStringWithDefault(JsonElement base, String defaultValue, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException  {
		JsonElement dv = null;
		if (defaultValue != null) {
			dv = new JsonPrimitive(defaultValue);
		}
		JsonPrimitive prim = (JsonPrimitive) JsonTypes.Primitive.validate(asJsonElementWithDefault(base, dv, path));
		if (prim == null) {
			return null;
		}
		return prim.getAsString();
	}
	
	public static int asInt(JsonElement base, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonFormatException, JsonInvalidPathException {
		try {
			return Integer.parseInt(asString(base, path));
		} catch (NumberFormatException e) {
			throw new JsonFormatException(asJsonElement(base, path));
		}
	}
	
	public static int asIntWithDefault(JsonElement base, int defaultValue, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonFormatException, JsonInvalidPathException {
		try {
			return Integer.parseInt(asStringWithDefault(base, Integer.toString(defaultValue), path));
		} catch (NumberFormatException e) {
			throw new JsonFormatException(asJsonElement(base, path));
		}
	}
	
	public static long asLong(JsonElement base, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonFormatException, JsonInvalidPathException {
		try {
			return Long.parseLong(asString(base, path));
		} catch (NumberFormatException e) {
			throw new JsonFormatException(asJsonElement(base, path));
		}
	}

	public static long asLongWithDefault(JsonElement base, long defaultValue, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonFormatException, JsonInvalidPathException {
		try {
			return Long.parseLong(asStringWithDefault(base, Long.toString(defaultValue), path));
		} catch (NumberFormatException e) {
			throw new JsonFormatException(asJsonElement(base, path));
		}
	}
	
	public static float asFloat(JsonElement base, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonFormatException, JsonInvalidPathException {
		try {
			return Float.parseFloat(asString(base, path));
		} catch (NumberFormatException e) {
			throw new JsonFormatException(asJsonElement(base, path));
		}
	}

	public static float asFloatWithDefault(JsonElement base, float defaultValue, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonFormatException, JsonInvalidPathException {
		try {
			return Float.parseFloat(asStringWithDefault(base, Float.toString(defaultValue), path));
		} catch (NumberFormatException e) {
			throw new JsonFormatException(asJsonElement(base, path));
		}
	}
	
	public static double asDouble(JsonElement base, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonFormatException, JsonInvalidPathException {
		try {
			return Double.parseDouble(asString(base, path));
		} catch (NumberFormatException e) {
			throw new JsonFormatException(asJsonElement(base, path));
		}
	}

	public static double asDoubleWithDefault(JsonElement base, double defaultValue, Object... path) throws JsonTypeMismatchException, JsonPathNotFoundException, JsonFormatException, JsonInvalidPathException {
		try {
			return Double.parseDouble(asStringWithDefault(base, Double.toString(defaultValue), path));
		} catch (NumberFormatException e) {
			throw new JsonFormatException(asJsonElement(base, path));
		}
	}
	
	public static Map<JsonElement, String> buildPathInfo(JsonObject root) {
		Map<JsonElement, String> ret = new HashMap<JsonElement, String>();
		ret.put(root, "/");
		List<Object> path = new LinkedList<Object>();
		buildPathInfo(ret, path, root);
		return ret;
	}

	private static void buildPathInfo(Map<JsonElement, String> map,
			List<Object> path, JsonElement elem) {
		if (!elem.isJsonNull()) {
			if (elem.isJsonArray()) {
				buildPathInfo(map, path, elem.getAsJsonArray());
			} else if (elem.isJsonObject()) {
				buildPathInfo(map, path, elem.getAsJsonObject());
			}
			map.put(elem, jsonpath(path));
		}
	}

	private static void buildPathInfo(Map<JsonElement, String> map, List<Object> path, JsonArray arr) {
		int len = arr.size();
		for (int i = 0; i < len; i++) {
			path.add(i);
			buildPathInfo(map, path, arr.get(i));
			path.remove(path.size() - 1);
		}
	}
	
	private static void buildPathInfo(Map<JsonElement, String> map, List<Object> path, JsonObject obj) {
		for (Entry<String, JsonElement> ent : obj.entrySet()) {
			String k = ent.getKey();
			JsonElement elem = ent.getValue();
			path.add(k);
			buildPathInfo(map, path, elem);
			path.remove(path.size() - 1);
		}
	}
	
	private static String jsonpath(List<Object> path) {
		StringBuffer buf = new StringBuffer();
		boolean firstTime = true;
		for (Object obj : path) {
			if (obj instanceof Number) {
				buf.append("[");
				buf.append(obj);
				buf.append("]");
			} else {
				if (!firstTime) {
					buf.append(".");
				}
				buf.append(obj);
			}
			firstTime = false;
		}
		return buf.toString();
	}
	
	private static JsonParser jsonParser() {
		JsonParser ret = null;
		if ((ret = jsonParser.get()) == null) {
			jsonParser.set(ret = new JsonParser());
		}
		return ret;
	}

	public static JsonElement toJson(String str) {
		return jsonParser().parse(str);
	}
	
	public static Iterator<String> keyIterator(final JsonObject json) {
		Iterator<String> i = new Iterator<String>() {
			Iterator<Entry<String, JsonElement>> iEntries = json.entrySet().iterator();
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
		return i;
	}
	
	public static String formatPath(Object... relPath) {
		StringBuffer ret = new StringBuffer();
		for (Object obj : relPath) {
			if (ret.length() != 0) ret.append("/");
			ret.append(obj);
		}
		return ret.toString();
	}
}
