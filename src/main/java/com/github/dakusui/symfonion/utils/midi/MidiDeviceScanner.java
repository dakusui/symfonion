package com.github.dakusui.symfonion.utils.midi;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice.Info;

public abstract class MidiDeviceScanner {
  static abstract class RegexMidiDeviceScanner extends MidiDeviceScanner {

    private final Pattern regex;

    public RegexMidiDeviceScanner(PrintStream ps, Pattern regex) {
      super(ps);
      this.regex = regex;
    }

    @Override
    protected void start(Info[] allDevices) {
      PrintStream ps = getPrintStream();
      ps.println("     " + getTitle());
      ps.printf("  io %s%n", MidiUtils.formatMidiDeviceInfo(null));
      ps.println("--------------------------------------------------------------------------------");
    }

    protected abstract String getTitle();

    @Override
    protected boolean matches(Info device) {
      Matcher m = regex.matcher(device.getName());
      return m.find();
    }

    @Override
    protected void matched(Info device) {
      String i = MidiUtils.isMidiDeviceForInput(device) ? "I" : " ";
      String o = MidiUtils.isMidiDeviceForOutput(device) ? "O" : " ";
      getPrintStream().printf("* %1s%1s %s%n", i, o, MidiUtils.formatMidiDeviceInfo(device));
    }

    @Override
    protected void scanned(Info device) {
    }

    @Override
    protected void end(Info[] matchedDevices) {
    }

    @Override
    protected void didntMatch(Info device) {
      String i = MidiUtils.isMidiDeviceForInput(device) ? "I" : " ";
      String o = MidiUtils.isMidiDeviceForOutput(device) ? "O" : " ";
      getPrintStream().printf("  %1s%1s %s%n", i, o, MidiUtils.formatMidiDeviceInfo(device));
    }
  }

  private final PrintStream ps;
  private MidiDevice.Info[] matchedDevices = null;
  protected abstract void start(MidiDevice.Info[] allDevices);

  protected abstract boolean matches(MidiDevice.Info device);

  protected abstract void matched(MidiDevice.Info device);

  protected abstract void scanned(Info device);

  protected abstract void end(MidiDevice.Info[] matchedDevices);

  public MidiDeviceScanner(PrintStream ps) {
    this.ps = ps;
  }

  public void scan() {
    List<MidiDevice.Info> matched = new LinkedList<>();
    MidiDevice.Info[] allDevices = MidiSystem.getMidiDeviceInfo();

    start(allDevices);
    for (MidiDevice.Info cur : allDevices) {
      if (matches(cur)) {
        matched(cur);
        matched.add(cur);
      } else {
        didntMatch(cur);
      }
      scanned(cur);
    }
    end(matchedDevices = matched.toArray(new MidiDevice.Info[0]));
  }


  protected abstract void didntMatch(MidiDevice.Info info);

  public PrintStream getPrintStream() {
    return this.ps;
  }


  public MidiDevice.Info[] getMatchedDevices() {
    return this.matchedDevices;
  }
}
