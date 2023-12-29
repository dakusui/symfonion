package com.github.dakusui.symfonion.testutils.json;

import com.github.dakusui.testutils.json.JsonBuilder;
import com.github.dakusui.testutils.json.JsonTestUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Optional;

import static com.github.dakusui.testutils.json.JsonTestUtils.*;

class SimpleSongBuilder extends JsonBuilder<SimpleSongBuilder> {
  
  private JsonElement groove = null;
  private String portName;
  
  public SimpleSongBuilder() {
    this.groove(null).portName("portName1");
  }
  
  public SimpleSongBuilder portName(String portName) {
    this.portName = portName;
    return this;
  }
  
  public SimpleSongBuilder groove(JsonElement groove) {
    this.groove = groove;
    return this;
  }
  
  @Override
  public JsonObject build() {
    String patternName = "pattern1";
    String grooveName = "16beats";
    String partName = "part1";
    String beats = "16/4";
    JsonElement[] noteSequence = null;
    return object(
        $("$settings", object()),
        $("$parts", object($(partName, object($("$channel", json(0)), $("$port", json(portName)))))),
        $("$patterns", object($(patternName, object($("$body", array(noteSequence)))))),
        $("$grooves", Optional.ofNullable(this.groove).map(g -> object($(grooveName, g))).orElseGet(JsonTestUtils::object)),
        $("$sequence", array(
            merge(
                object($("$beats", json(beats))),
                object($("$patterns", object($(partName, array(patternName))))),
                object($("$groove", json(this.groove)))
            ))));
  }
}
