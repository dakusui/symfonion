package com.github.dakusui.symfonion.tests.utils.midi;

import com.github.dakusui.symfonion.utils.midi.MidiDeviceScanner;

import java.util.regex.Pattern;

public class MidiDeviceScannerTest {
  public static void main(String[] args) {
    MidiDeviceScanner.chooseInputDevices(System.out, Pattern.compile("Real")).scan();
    System.out.println();
    MidiDeviceScanner.chooseOutputDevices(System.out, Pattern.compile("Ger")).scan();
    System.out.println();
    MidiDeviceScanner.listAllDevices(System.out).scan();
  }
}
