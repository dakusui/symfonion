package com.github.dakusui.symfonion.scenarios;

import com.google.gson.JsonElement;

import static com.github.dakusui.testutils.json.JsonTestUtils.json;

public class PatternBuilder extends JsonObjectBuilder<PatternBuilder> {
  public PatternBuilder length(Object length) {
    return this.add("$length", length);
  }
  
  public PatternBuilder gate(Object gate) {
    return this.add("$gate", gate);
  }
}
