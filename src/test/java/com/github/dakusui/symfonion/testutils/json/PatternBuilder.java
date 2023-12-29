package com.github.dakusui.symfonion.testutils.json;

import com.github.dakusui.testutils.json.JsonObjectBuilder;

public class PatternBuilder extends JsonObjectBuilder<PatternBuilder> {
  public PatternBuilder length(Object length) {
    return this.add("$length", length);
  }
  
  public PatternBuilder gate(Object gate) {
    return this.add("$gate", gate);
  }
}
