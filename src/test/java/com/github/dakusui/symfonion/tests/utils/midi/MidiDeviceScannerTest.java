package com.github.dakusui.symfonion.tests.utils.midi;

import com.github.dakusui.symfonion.utils.midi.MidiUtils;

import java.util.regex.Pattern;

public class MidiDeviceScannerTest {
  public static void main(String[] args) {
    MidiUtils.chooseInputDevices(System.out, Pattern.compile("Real")).scan();
    System.out.println();
    MidiUtils.chooseOutputDevices(System.out, Pattern.compile("Ger")).scan();
    System.out.println();
    MidiUtils.listAllDevices(System.out).scan();
  }
}
