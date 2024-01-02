package com.github.dakusui.testutils.midi;

import org.junit.AssumptionViolatedException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.github.dakusui.testutils.TestUtils.toHex;
import static java.util.Arrays.asList;

public class MidiTestUtils {
  public static String formatMidiMessage(MidiMessage message) {
    return String.format("message: %-20s: %s", message.getClass().getSimpleName(), toHex(message.getMessage()));
  }

  public static void assumeMidiDevicesPresent() {
    MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
    List<String> missingDevices = new LinkedList<>();
    for (String requiredDeviceName : asList("Gervill", "Real Time Sequencer"))
      if (Arrays.stream(infos).noneMatch(i -> Objects.equals(requiredDeviceName, i.getName())))
        missingDevices.add(requiredDeviceName);
    if (!missingDevices.isEmpty())
      throw new AssumptionViolatedException("MIDI-devices: " + missingDevices + " were not found in this system. Known devices are: " + Arrays.toString(infos));
  }
}
