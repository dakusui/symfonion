package com.github.dakusui.symfonion.tests.json;

import com.github.dakusui.symfonion.compat.json.*;
import com.github.dakusui.symfonion.testutils.TestBase;
import com.github.dakusui.thincrest_pcond.forms.Printables;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.thincrest.TestAssertions.assertThat;
import static com.github.dakusui.thincrest_pcond.forms.Functions.*;
import static com.github.dakusui.thincrest_pcond.forms.Predicates.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class JsonUtilTest extends TestBase {
  private JsonObject obj;
  private JsonArray arr;
  
  /**
   * <code>
   * {
   *   "key1":"val1",
   *   "key2":2,
   *   "key3":3.0f,
   *   "key4":4L,
   *   "key5":5.0d,
   * "key6":"600",
   * "key7":[
   * "val71",
   * 100,
   * {
   * "key72":"val72"
   * }
   * ],
   * "key8":128L,
   * "key9":Long.MAX_Value
   * ]
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
   *
   * @return returns a test array shown above.
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
  public void obj_N01a() {
    assertThat(
        this.obj,
        transform(callJsonUtils_asString("key1")).check(isEqualTo("val1")));
  }
  
  private static Function<Object, String> callJsonUtils_asString(Object... args) {
    return call(classMethod(CompatJsonUtils.class, "asString", parameter(), args)).andThen(castTo(value()));
  }
  
  @Test
  public void whenJsonUtils_asStringWithDefault_onExistingJsonPath_thenValueOnSpecifiedPathReturned() {
    assertThat(
        this.obj,
        transform(callJsonUtils_asStringWithDefault("VAL1", "key1")).check(isEqualTo("val1")));
  }
  
  @Test
  public void whenAsStringWithDefault_onNonExistingPath_thenFallbackToDefault() {
    assertThat(
        this.obj,
        transform(callJsonUtils_asStringWithDefault("VAL1", "_key1")).check(isEqualTo("VAL1")));
  }
  
  private static Function<Object, String> callJsonUtils_asStringWithDefault(String defaultValue, Object... args) {
    return call(classMethod(CompatJsonUtils.class, "asStringWithDefault", parameter(), defaultValue, args)).andThen(castTo(value()));
  }
  
  @Test
  public void whenJsonUtils_asStringWithDefault_onExistingIndexIncludingPath_thenValueAtPathReturned() {
    assertThat(
        this.obj,
        transform(callJsonUtils_asStringWithDefault("VAL1", "key7", 2, "key72")).check(isEqualTo("val72")));
  }
  
  @Test
  public void obj_N01e() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException {
    Assert.assertEquals("val72", CompatJsonUtils.asStringWithDefault(this.obj, "DEFAULT_VALUE", "key7", "2", "key72"));
  }
  
  @Test
  public void obj_N02() throws JsonTypeMismatchException, JsonPathNotFoundException, JsonInvalidPathException, JsonFormatException {
    Assert.assertEquals(2, CompatJsonUtils.asInt(this.obj, "key2"));
  }
  
  @Test
  public void obj_N03a() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
    JsonObject expected = new JsonObject();
    expected.addProperty("key72", "val72");
    Assert.assertEquals(expected, CompatJsonUtils.asJsonObject(this.obj, "key7", 2));
  }
  
  @Test
  public void obj_N03b() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
    JsonObject defaultValue = new JsonObject();
    defaultValue.addProperty("key72", "val72def");
    JsonObject expected = new JsonObject();
    expected.addProperty("key72", "val72");
    Assert.assertEquals(expected, CompatJsonUtils.asJsonObjectWithDefault(this.obj, defaultValue, "key7", 2));
  }
  
  
  @Test
  public void obj_N03c() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
    JsonObject defaultValue = new JsonObject();
    defaultValue.addProperty("key72", "val72def");
    JsonObject expected = new JsonObject();
    expected.addProperty("key72", "val72def");
    Assert.assertEquals(expected, CompatJsonUtils.asJsonObjectWithDefault(this.obj, defaultValue, "key0"));
  }
  
  /*
   @Test Disable due to spec change
  */
  public void obj_N03d() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
    JsonObject defaultValue = new JsonObject();
    defaultValue.addProperty("key72", "val72def");
    JsonObject expected = new JsonObject();
    expected.addProperty("key72", 100);
    Assert.assertEquals(
        expected,
        CompatJsonUtils.asJsonObjectWithPromotion(
            this.obj,
            new String[]{"key72"},
            defaultValue,
            "key7", 1
        )
    );
  }
  
  /*
   @Test Disable due to spec change
  */
  public void obj_N03e() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
    JsonObject defaultValue = new JsonObject();
    defaultValue.addProperty("key72", "val72def");
    JsonObject expected = new JsonObject();
    expected.addProperty("key72", "val72def");
    Assert.assertEquals(
        expected,
        CompatJsonUtils.asJsonObjectWithPromotion(
            this.obj,
            new String[]{"key72"},
            defaultValue,
            "key0", 1
        )
    );
  }
  
  /*
   @Test Disable due to spec change
  */
  public void obj_N03f() throws JsonPathNotFoundException, JsonTypeMismatchException, JsonInvalidPathException {
    JsonObject defaultValue = new JsonObject();
    defaultValue.addProperty("key72", "val72def");
    JsonObject expected = new JsonObject();
    expected.addProperty("key72", "val72");
    Assert.assertEquals(
        expected,
        CompatJsonUtils.asJsonObjectWithPromotion(
            this.obj,
            new String[]{"key72"},
            defaultValue,
            "key7", 2
        )
    );
  }
  
  @Test
  public void obj_E03d() throws JsonTypeMismatchException, JsonInvalidPathException {
    boolean passed = false;
    try {
      CompatJsonUtils.asJsonObjectWithDefault(this.obj, new JsonObject(), "key7", 4);
      fail();
    } catch (JsonIndexOutOfBoundsException e) {
      e.printStackTrace();
      passed = true;
    }
    assertTrue(passed);
  }
  
  @Test
  public void obj_E02() throws JsonTypeMismatchException, JsonInvalidPathException, JsonFormatException {
    try {
      CompatJsonUtils.asInt(this.obj, "_key2");
      fail();
    } catch (JsonPathNotFoundException e) {
      e.printStackTrace();
      assertEquals(this.obj, e.getProblemCausingNode());
      assertArrayEquals(new Object[]{"_key2"}, e.getPath());
      assertEquals(0, e.getIndex());
    }
  }
  
  @Test
  public void obj_E01d() throws JsonTypeMismatchException {
    try {
      System.out.println(CompatJsonUtils.asStringWithDefault(this.obj, "VAL1", "key7", "STRING", "key72"));
      fail();
    } catch (JsonInvalidPathException e) {
      e.printStackTrace();
      assertThat(
          e,
          allOf(
              transform(call("getPath").andThen(castTo((Object[]) value()))
                  .andThen(call_Arrays_asList())).check(isEqualTo(asList("key7", "STRING", "key72"))),
              transform(call("getIndex")).check(isEqualTo(1)),
              transform(call("getProblemCausingNode")).check(isEqualTo(this.obj.get("key7")))));
    }
  }
 
  
  @Test
  public void arr_N01() throws JsonTypeMismatchException, JsonInvalidPathException {
    Assert.assertEquals("val1", CompatJsonUtils.asString(this.arr, 0));
  }

  private static Function<Object[], List<Object>> call_Arrays_asList() {
    return Printables.function("Arrays_asList", Arrays::asList);
  }
}
