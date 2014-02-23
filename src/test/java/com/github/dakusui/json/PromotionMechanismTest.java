package com.github.dakusui.json;

import org.junit.Test;

import com.google.gson.JsonObject;

public class PromotionMechanismTest {
	@Test
	public void test() throws JsonPathNotFoundException, JsonInvalidPathException, JsonTypeMismatchException  {
		JsonUtil.asJsonObjectWithPromotion(base(), prioritizedKeys(), path());
	}
	
	private static  Object path() {
		// TODO Auto-generated method stub
		return null;
	}

	private static String[] prioritizedKeys() {
		return new String[]{"key1", "key2"};
	}
	
	private static JsonObject base() {
		JsonObject obj = JsonUtil.toJson(""
/*
{
    "primitive":"hello",
    "array":["hello", "world"],
    "null":null,
    "object":{
        "key1":"hello",
        "key2":"world"
    },
    "path1":{
	    "primitive":"hello",
	    "array":["hello", "world"],
	    "null":null,
	    "object":{
	        "key1":"hello",
	        "key2":"world"
    }
}
*/
		).getAsJsonObject();
		return obj;
	}
}
