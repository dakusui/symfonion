package com.github.dakusui.json;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonUtilTest {
	private JsonObject obj;
	private JsonArray arr;
	/**
	 * <code>
	 * {
	 *     "key1":"val1",
	 *     "key2":2,
	 *     "key3":3.0f,
	 *     "key4":4L,
	 *     "key5":5.0d,
	 *     "key6":"600",
	 *     "key7":[
	 *         "val71",
	 *         100,
	 *         {
	 *             "key72":"val72"
	 *         }
	 *     ],
	 *     "key8":128L,
	 *     "key9":Long.MAX_Value
	 *     ]
	 * }
	 * </code>
	 */
	private JsonObject testObject() {
		JsonObject ret = new JsonObject();
		
		ret.add("key1", new JsonPrimitive("val1"));
		ret.add("key2", new JsonPrimitive(2));
		ret.add("key3", new JsonPrimitive(3.0f));
		ret.add("key4", new JsonPrimitive(4L));
		ret.add("key5", new JsonPrimitive(5.0d));
		ret.add("key6", new JsonPrimitive("600"));
		
		JsonArray arr = new JsonArray();
		arr.add(new JsonPrimitive("val71"));
		arr.add(new JsonPrimitive(100));
		JsonObject obj1 = new JsonObject();
		obj1.addProperty("key72", "val72");
		
		arr.add(obj1);
		ret.add("key7", arr);
		ret.add("key8", new JsonPrimitive(128L));
		ret.add("key9", new JsonPrimitive(Long.MAX_VALUE));
		return ret;
	}
	
	/**
	 * <code>
	 * [ "val1", 123, { "hello":"world", "hi":null } ]
	 * </code>
	 * @return
	 */
	private JsonArray testArray() {
		JsonArray ret = new JsonArray();
		
		ret.add(new JsonPrimitive("val1"));
		ret.add(new JsonPrimitive(123));
		
		JsonObject obj = new JsonObject();
		obj.addProperty("hello", "world");
		obj.add("hi", JsonNull.INSTANCE);
		ret.add(obj);
		
		return ret;
	}
	
	@Before
	public void setUp() {
		this.obj = testObject();
		this.arr = testArray();
	}
	
	@Test
	public void obj_N01a() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException {
		Assert.assertEquals("val1", JsonUtil.asString(this.obj, "key1"));
	}
	
	@Test
	public void obj_N01b() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException {
		Assert.assertEquals("val1", JsonUtil.asStringWithDefault(this.obj, "VAL1", "key1"));
	}
	
	@Test
	public void obj_N01c() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException {
		Assert.assertEquals("VAL1", JsonUtil.asStringWithDefault(this.obj, "VAL1", "_key1"));
	}
	
	@Test
	public void obj_N01d() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException {
		Assert.assertEquals("val72", JsonUtil.asStringWithDefault(this.obj, "VAL1", "key7", 2, "key72"));
	}
	
	@Test
	public void obj_N01e() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException {
		Assert.assertEquals("val72", JsonUtil.asStringWithDefault(this.obj, "VAL1", "key7", "2", "key72"));
	}
	
	@Test
	public void obj_N02() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException, JsonFormatException {
		Assert.assertEquals(2, JsonUtil.asInt(this.obj, "key2"));
	}
	
	@Test
	public void obj_N03a() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
		JsonObject expected = new JsonObject(); 
		expected.addProperty("key72", "val72");
		Assert.assertEquals(expected, JsonUtil.asJsonObject(this.obj, "key7", 2));
	}
	
	@Test
	public void obj_N03b() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
		JsonObject defaultValue = new JsonObject(); 
		defaultValue.addProperty("key72", "val72def");
		JsonObject expected = new JsonObject(); 
		expected.addProperty("key72", "val72");
		Assert.assertEquals(expected, JsonUtil.asJsonObjectWithDefault(this.obj, defaultValue, "key7", 2));
	}
	

	@Test
	public void obj_N03c() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
		JsonObject defaultValue = new JsonObject(); 
		defaultValue.addProperty("key72", "val72def");
		JsonObject expected = new JsonObject(); 
		expected.addProperty("key72", "val72def");
		Assert.assertEquals(expected, JsonUtil.asJsonObjectWithDefault(this.obj, defaultValue, "key0"));
	}

	@Test
	public void obj_N03d() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
		JsonObject defaultValue = new JsonObject(); 
		defaultValue.addProperty("key72", "val72def");
		JsonObject expected = new JsonObject(); 
		expected.addProperty("key72", 100);
		Assert.assertEquals(
				expected, 
				JsonUtil.asJsonObjectWithPromotion(
						this.obj, 
						new String[]{"key72"}, 
						defaultValue, 
						"key7", 1
						)
				);
	}

	@Test
	public void obj_N03e() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
		JsonObject defaultValue = new JsonObject(); 
		defaultValue.addProperty("key72", "val72def");
		JsonObject expected = new JsonObject(); 
		expected.addProperty("key72", "val72def");
		Assert.assertEquals(
				expected, 
				JsonUtil.asJsonObjectWithPromotion(
						this.obj, 
						new String[]{"key72"}, 
						defaultValue, 
						"key0", 1
						)
				);
	}
	
	@Test
	public void obj_N03f() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
		JsonObject defaultValue = new JsonObject(); 
		defaultValue.addProperty("key72", "val72def");
		JsonObject expected = new JsonObject(); 
		expected.addProperty("key72", "val72");
		Assert.assertEquals(
				expected, 
				JsonUtil.asJsonObjectWithPromotion(
						this.obj, 
						new String[]{"key72"}, 
						defaultValue, 
						"key7", 2
						)
				);
	}
	
	@Test
	public void obj_E03d() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
		boolean passed = false;
		try {
			JsonUtil.asJsonObjectWithDefault(this.obj, new JsonObject(), "key7", 4);
			fail();
		} catch (JsonIndexOutOfBoudsException e) {
			passed = true;
		}
		assertTrue(passed);
	}

	@Test
	public void obj_E02() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException, JsonFormatException {
		try {
			JsonUtil.asInt(this.obj, "_key2");
			fail();
		} catch (JsonPathNotFoundException e) {
			assertEquals(this.obj, e.getLocation());
			assertArrayEquals(new Object[]{"_key2"}, e.getPath());
			assertEquals(0, e.getIndex());
		}
	}

	@Test
	public void obj_E01d() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException {
		try {
			System.out.println(JsonUtil.asStringWithDefault(this.obj, "VAL1", "key7", "STRING", "key72"));
			fail();
		} catch (JsonInvalidPathException e) {
			assertArrayEquals(
					new Object[]{"key7", "STRING", "key72"},
					e.getPath()
					);
			assertEquals(1, e.getIndex());
			assertEquals(this.obj.get("key7"), e.getLocation());
		}
	}
	
	@Test
	public void arr_N01() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException  {
		Assert.assertEquals("val1", JsonUtil.asString(this.arr, 0));
	}
}
