package com.github.dakusui.symfonion.song;

import com.github.dakusui.json.JsonFormatException;
import com.github.dakusui.json.JsonInvalidPathException;
import com.github.dakusui.json.JsonTypeMismatchException;
import com.github.dakusui.json.JsonUtils;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.google.gson.JsonObject;


public class Part {
  int channel;
  private final String name;
  private final String portName;

  public Part(String name, JsonObject json) throws SymfonionException, JsonTypeMismatchException, JsonFormatException, JsonInvalidPathException {
    this.name = name;
    this.channel = JsonUtils.asInt(json, Keyword.$channel);
    this.portName = JsonUtils.asStringWithDefault(json, null, Keyword.$port);
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
