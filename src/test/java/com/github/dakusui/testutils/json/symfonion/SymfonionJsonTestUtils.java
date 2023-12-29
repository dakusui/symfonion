package com.github.dakusui.testutils.json.symfonion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static com.github.dakusui.testutils.json.JsonTestUtils.*;

public enum SymfonionJsonTestUtils {
  ;
  
  public static JsonArray sixteenBeatsGrooveFlat() {
    return array(
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0),
        grooveElement("16", 24, 0)
    
    );
  }
  
  public static JsonArray sixteenBeatsGroove() {
    return array(
        grooveElement("16", 28, 30),
        grooveElement("16", 20, -10),
        grooveElement("16", 26, 10),
        grooveElement("16", 22, -5),
        
        grooveElement("16", 28, 20),
        grooveElement("16", 20, -8),
        grooveElement("16", 26, 10),
        grooveElement("16", 22, -4),
        
        grooveElement("16", 28, 25),
        grooveElement("16", 20, -8),
        grooveElement("16", 26, 10),
        grooveElement("16", 22, -5),
        
        grooveElement("16", 28, 15),
        grooveElement("16", 20, -8),
        grooveElement("16", 26, 10),
        grooveElement("16", 22, -10)
    );
  }
  
  public static JsonObject grooveElement(String noteLength, int ticks, int accent) {
    return object($("$length", json(noteLength)), $("$ticks", json(ticks)), $("$accent", json(accent)));
  }
  
  public static JsonObject programChange(int program, double bank) {
    return object($("$program", json(program)), $("$bank", json(bank)));
  }
  
  public static JsonObject composeSymfonionSongJsonObject(String portName, JsonElement strokes, JsonArray groove) {
    String patternName = "C16x16";
    String grooveName = "16beats";
    String partName = "piano";
    String beats = "16/4";
    return object(
        $("$settings", object()),
        $("$parts", object($(partName, object($("$channel", json(0)), $("$port", json(portName)))))),
        $("$patterns", object($(patternName, object($("$body", strokes))))),
        $("$grooves", object($(grooveName, groove))),
        $("$sequence", array(
            merge(
                object($("$beats", json(beats))),
                object($("$patterns", object($(partName, array(patternName))))),
                object($("$groove", json(grooveName)))
            ))));
  }
  
  public static JsonObject rootJsonObjectBase() {
    return object(
        $("$settings", object()),
        $("$parts", object()),
        $("$patterns", object()),
        $("$sequence", array()));
  }
}
