package com.github.dakusui.symfonion.cli;

/**
 * A record to define a request for MIDI signal routing.
 *
 * @param in A port name from which MIDI signal comes.
 * @param out A port name to which MIDI signal goes.
 */
public record MidiRouteRequest(String in , String out) {
}

