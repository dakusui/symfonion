package com.github.dakusui.symfonion.testutils;

import com.github.dakusui.testutils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class TestBase {
  @BeforeEach
  public void before() {
    com.github.dakusui.testutils.TestUtils.suppressStdOutErrIfUnderPitestOrSurefire();
  }

  @AfterEach
  public void after() {
    TestUtils.restoreStdOutErr();
  }
}
