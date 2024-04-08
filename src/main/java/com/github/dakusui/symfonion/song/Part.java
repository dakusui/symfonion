package com.github.dakusui.symfonion.song;

import com.github.dakusui.symfonion.compat.json.JsonFormatException;
import com.github.dakusui.symfonion.compat.json.JsonInvalidPathException;
import com.github.dakusui.symfonion.compat.json.JsonTypeMismatchException;
import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.google.gson.JsonObject;


public class Part {
  int channel;
  private final String name;
  private final String portName;

  public Part(String name, JsonObject json) throws SymfonionException, JsonTypeMismatchException, JsonFormatException, JsonInvalidPathException {
    this.name = name;
    this.channel = CompatJsonUtils.asInt(json, Keyword.$channel);
    this.portName = CompatJsonUtils.asStringWithDefault(json, null, Keyword.$port);
  }

  public String name() {
    return this.name;
  }

  public int channel() {
    return this.channel;
  }

  public String portName() {
    return this.portName;
  }
}
