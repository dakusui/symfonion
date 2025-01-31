package com.github.dakusui.symfonion.tests.json;

import com.github.dakusui.symfonion.compat.json.JsonInvalidPathException;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.testutils.TestBase;
import com.github.dakusui.thincrest_cliche.core.Transform;
import com.github.valid8j.pcond.forms.Printables;
import org.junit.Test;

import static com.github.dakusui.testutils.json.JsonTestUtils.*;
import static com.github.valid8j.classic.TestAssertions.assertThat;
import static com.github.valid8j.pcond.forms.Predicates.isEqualTo;

public class CompatJsonUtilsTest extends TestBase {
  @Test
  public void givenEmptyObjects_whenMerge_thenEmpty() throws JsonInvalidPathException {
    assertThat(CompatJsonUtils.merge(
            object(),
            object()),
        isEqualTo(object())
    );
  }

  @Test
  public void givenEmptyAndSingleEntryObjects_whenMerge_thenSingleEntry() throws JsonInvalidPathException {
    assertThat(CompatJsonUtils.merge(
            object(),
            object($("key", json("value")))),
        isEqualTo(object($("key", json("value"))))
    );
  }

  @Test
  public void givenSingleEntryAndEmptyObjects_whenMerge_thenSingleEntry() throws JsonInvalidPathException {
    assertThat(CompatJsonUtils.merge(
            object($("key", json("value"))),
            object()),
        isEqualTo(object($("key", json("value"))))
    );
  }

  @Test
  public void givenTwoSingleEntryObjects_whenMerge_thenTwoEntries() throws JsonInvalidPathException {
    assertThat(CompatJsonUtils.merge(
            object($("key1", json("value1"))),
            object($("key2", json("value2")))),
        Transform.$(Printables.function("toString", Object::toString)).check(isEqualTo(object(
            $("key1", json("value1")),
            $("key2", json("value2"))).toString()
        )));
  }

  @Test
  public void givenTwoNestedEntryObjects_whenMerge_thenMergesTwoEntries() throws JsonInvalidPathException {
    assertThat(CompatJsonUtils.merge(
            object($("key", object($("key1", json("value1"))))),
            object($("key", object($("key2", json("value2")))))),
        Transform.$(Printables.function("toString", Object::toString)).check(isEqualTo(object(
            $("key", object(
                $("key1", json("value1")),
                $("key2", json("value2"))))).toString()
        )));
  }

  @Test(expected = JsonInvalidPathException.class)
  public void givenConflictingTwoSingleEntryObjects_whenMerge_thenExceptionThrown() throws JsonInvalidPathException {
    CompatJsonUtils.merge(
        object($("key", json("value1"))),
        object($("key", json("value2"))));
  }
}
