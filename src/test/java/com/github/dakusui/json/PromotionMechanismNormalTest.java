package com.github.dakusui.json;

import org.junit.runner.RunWith;

import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.GeneratorParameters;
import com.github.dakusui.jcunit.core.GeneratorParameters.Type;
import com.github.dakusui.jcunit.core.GeneratorParameters.Value;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.generators.CustomTestArrayGenerator;

@RunWith(JCUnit.class)
@Generator(CustomTestArrayGenerator.class)
@GeneratorParameters({
    @Value(intArrayValue = { 0, 2, 1, 0 }, type = Type.IntArray),
    @Value(intArrayValue = { 0, 2, 1, 1 }, type = Type.IntArray),
    @Value(intArrayValue = { 0, 2, 1, 2 }, type = Type.IntArray),
    @Value(intArrayValue = { 0, 2, 1, 3 }, type = Type.IntArray) })
public class PromotionMechanismNormalTest extends PromotionMechanismTestBase {
}
