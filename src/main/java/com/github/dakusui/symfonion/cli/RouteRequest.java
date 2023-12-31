package com.github.dakusui.symfonion.cli;

import javax.sound.midi.MidiDevice;

/**
 * A record to define a request for MIDI signal routing.
 * An instance of this record represents a requirement that should be satisfied by a route for MIDI signal as a pair of conditions for MIDI-in and MIDI-out ports.
 * Both conditions are given as regular expressions that should match devices' name returned by {@link MidiDevice.Info#getName()}.
 *
 * @param in A regular expression for MIDI-in port.
 * @param out A regular expression for MIDI-out port.
 */
public record RouteRequest(String in , String out) {
}
