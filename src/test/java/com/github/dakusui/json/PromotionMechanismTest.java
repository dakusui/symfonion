package com.github.dakusui.json;

import com.github.dakusui.jcunit.core.BasicSummarizer;
import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.In.Domain;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.core.Summarizer;
import com.github.dakusui.jcunit.generators.PairwiseTestArrayGenerator;
import com.github.dakusui.symfonion.core.SymfonionException;
import com.github.dakusui.symfonion.core.Util;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
@Generator(PairwiseTestArrayGenerator.class)
public class PromotionMechanismTest {
	@Rule
	public RuleSet rules = new DefaultRuleSetBuilder().autoRuleSet(this).summarizer(summarizer);

	@ClassRule
	public static Summarizer summarizer = new BasicSummarizer();

	@In(domain = Domain.Method)
	public Object[] pathToParent;

	public static Object[][] pathToParent() {
		return new Object[][] {
				new Object[] {},
				new Object[] { "non-existing" },
				new Object[] { "level1" },
				new Object[] { "level1", "non-existing" },
				new Object[] { "level1", "level2" },
		};
	}

	@In(domain = Domain.Method)
	public Object key;

	public static Object[] key() {
		return new Object[] {
				"key-null-0",
				"key-prim-0",
				"key-arr-0",
				"key-arr-1",
				"key-arr-2",
				"key-arr-3",
				"key-obj-0",
				"key-obj-1",
				"key-obj-2",
				"key-obj-3",
		};
	}

	@In(domain = Domain.Method)
	public String[] prioritizedKeys;

	@Out
	public JsonObject obj;

	@Out
	public String exceptionMessage;

	@Out
	public Class<? extends Exception> exceptionClass;

	public static String[][] prioritizedKeys() {
		return new String[][] {
				new String[] {},
				new String[] { "key-null-0" },
				new String[] { "key-prim-0", "key-null-0" },
				new String[] { "key-prim-0", "key-arr-1", "key-obj-1" },
		};
	}

	private static JsonObject base() throws SymfonionException {
		JsonObject obj = JsonUtil.toJson(
				Util.loadResource(PromotionMechanismTest.class.getCanonicalName().replaceAll("\\.", "/") + ".js")
				).getAsJsonObject();
		return obj;
	}

	@Test
	public void test() throws Exception {
		this.obj = null;
		try {
			this.obj = JsonUtil.asJsonObjectWithPromotion(base(), prioritizedKeys, path(pathToParent, key));
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			this.exceptionClass = e.getClass();
			this.exceptionMessage = e.getMessage();
		}
	}

	private static Object[] path(Object[] pathToParent, Object key) {
		return ArrayUtils.add(pathToParent, key);
	}
}
