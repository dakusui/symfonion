package com.github.dakusui.symfonion.scenarios;

import com.google.gson.JsonElement;

import static com.github.dakusui.testutils.json.JsonTestUtils.json;

/*
 Stroke

 $notes,
 $volume,
 $pan,
 $reverb,
 $chorus,
 $pitch
 $modulation,
 $program,
 $tempo,
 
 $length,
 $velocitybase
 $velocitydelta
 $gate,
 $transpose
 
 */
public class StrokeBuilder extends JsonObjectBuilder<StrokeBuilder> {
  public StrokeBuilder notes(Object notes) {
    return this.add("$notes", notes);
  }
  
  public StrokeBuilder volume(Object volume) {
    return this.add("$volume", volume);
  }
  
  public StrokeBuilder pan(Object pan) {
    return this.add("$pan", pan);
  }
  
  public StrokeBuilder reverb(Object reverb) {
    return this.add("$reverb", reverb);
  }
  
  public StrokeBuilder chorus(Object chorus) {
    return this.add("$chorus", chorus);
  }
  
  public StrokeBuilder pitch(Object pitch) {
    return this.add("$pitch", pitch);
  }
  
  public StrokeBuilder modulation(Object modulation) {
    return this.add("$modulation", modulation);
  }
  
  public StrokeBuilder program(Object program) {
    return this.add("$program", program);
  }
  
  public StrokeBuilder tempo(Object tempo) {
    return this.add("$tempo", tempo);
  }
  
  public StrokeBuilder length(Object length) {
    return this.add("$length", length);
  }
  
  public StrokeBuilder velocitybase(Object valocitybase) {
    return this.add("$velocitybase", valocitybase);
  }
  
  public StrokeBuilder velocitydelta(Object velocitydelta) {
    return this.add("$velocitydelta", velocitydelta);
  }
  
  public StrokeBuilder gate(Object gate) {
    return this.add("$gate", gate);
  }
  
  public StrokeBuilder transpose(Object transpose) {
    return this.add("$transpose", transpose);
  }
}
