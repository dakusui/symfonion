package com.github.dakusui.symfonion.testutils;

import com.github.dakusui.testutils.TestUtils;
import org.junit.After;
import org.junit.Before;

public class TestBase {
  @Before
  public void before() {
    com.github.dakusui.testutils.TestUtils.suppressStdOutErrIfUnderPitestOrSurefire();
  }

  @After
  public void after() {
    TestUtils.restoreStdOutErr();
  }
}
