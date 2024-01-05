package com.github.dakusui.symfonion.testutils.forms;

import com.github.dakusui.thincrest_cliche.core.Cliche;

import javax.sound.midi.Sequence;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.github.dakusui.thincrest_pcond.forms.Printables.function;

public enum FromSong {
  ;
  
  public static Function<Map<String, Sequence>, Sequence> toSequence(String portName) {
    return function("get[" + portName + "]", m -> m.get(portName));
  }
  
  public static Function<Map<String, Sequence>, Set<String>> toKeySet() {
    return Cliche.keySet();
  }
}
