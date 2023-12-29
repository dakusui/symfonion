package com.github.dakusui.testutils.forms.symfonion;

import com.github.dakusui.testutils.forms.Cliche;

import javax.sound.midi.Sequence;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.github.dakusui.thincrest_pcond.forms.Printables.function;

public enum SongTo {
  ;
  
  public static Function<Map<String, Sequence>, Sequence> sequence(String portName) {
    return function("get[" + portName + "]", m -> m.get(portName));
  }
  
  public static Function<Map<String, Sequence>, Set<String>> keySet() {
    return Cliche.keySet();
  }
}
