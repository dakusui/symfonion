package com.github.dakusui.symfonion.tests.core;

import com.github.dakusui.symfonion.utils.Utils;
import com.github.dakusui.thincrest_cliche.core.Transform;
import com.github.dakusui.thincrest.TestAssertions;
import com.github.dakusui.valid8j_pcond.forms.Functions;
import org.junit.Test;

import static com.github.dakusui.valid8j_pcond.forms.Predicates.isEqualTo;

public class UtilsTest {
  @Test
  public void test_intToByteArray() {
    Byte[] result = new Byte[3];
    int i=0;
    for (byte b: Utils.getIntBytes(0x123456))
      result[i++] = b;

    TestAssertions.assertThat(
        result,
        Transform.$(Functions.<Byte>arrayToList()).allOf(
            Transform.$(Functions.<Byte>elementAt(0)).check(isEqualTo((byte)0x12)),
            Transform.$(Functions.<Byte>elementAt(1)).check(isEqualTo((byte)0x34)),
            Transform.$(Functions.<Byte>elementAt(2)).check(isEqualTo((byte)0x56))
        )
    );
  }
}
