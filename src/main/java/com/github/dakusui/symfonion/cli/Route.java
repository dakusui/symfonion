package com.github.dakusui.symfonion.cli;

public class Route {
    final String in;
    final String out;

    Route(String in, String out) {
        this.in = in;
        this.out = out;
    }

    public String in() {
        return this.in;
    }

    public String out() {
        return this.out;
    }
}
