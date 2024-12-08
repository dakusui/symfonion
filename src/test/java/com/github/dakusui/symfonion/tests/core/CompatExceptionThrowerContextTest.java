package com.github.dakusui.symfonion.tests.core;

import com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionMissingElementException;
import com.github.dakusui.thincrest_cliche.core.Transform;
import com.github.dakusui.testutils.json.JsonTestUtils;
import org.junit.Ignore;
import org.junit.Test;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.$;
import static com.github.valid8j.classic.TestAssertions.assertThat;
import static com.github.valid8j.pcond.forms.Functions.call;
import static com.github.valid8j.pcond.forms.Predicates.containsString;

public class CompatExceptionThrowerContextTest {
  @Ignore
  @Test(expected = SymfonionMissingElementException.class)
  public void testExceptionThrower() {
    try (CompatExceptionThrower.Context ignored = CompatExceptionThrower.context($(CompatExceptionThrower.ContextKey.JSON_ELEMENT_ROOT, JsonTestUtils.object(JsonTestUtils.entry("KEY_FOR_TEST", JsonTestUtils.json("VALUE_FOR_TEST_1")))))) {
      try {
        CompatExceptionThrower.requiredElementMissingException(JsonTestUtils.object(), JsonTestUtils.object(), "RELATIVE_PATH_FOR_TEST");
      } catch (SymfonionMissingElementException e) {
        e.printStackTrace();
        assertThat(e, Transform.<Throwable, String>$(call("getMessage")).check(containsString("VALUE_FOR_TEST_1")));
      }
    }
  }
}
