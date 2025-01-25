package com.github.dakusui.symfonion.testutils;

import com.github.dakusui.testutils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;

import java.util.List;

import static java.util.Collections.emptyList;

public class TestBase {
  @Before
  public void before() {
    com.github.dakusui.testutils.TestUtils.suppressStdOutErrIfUnderPitestOrSurefire();
  }

  @After
  public void after() {
    TestUtils.restoreStdOutErr();
  }

  /**
   * A method to work around an issue in JCUnit.
   *
   * @return An empty list
   */
  @Parameterized.Parameters
  public static List<Object> dummyParametersMethod() {
    return emptyList();
  }
}
