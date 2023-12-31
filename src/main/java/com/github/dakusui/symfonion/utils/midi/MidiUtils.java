package com.github.dakusui.symfonion.utils.midi;

import com.github.dakusui.symfonion.exceptions.CliException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

public enum MidiUtils {
  ;

  public record MidiDeviceRecord(boolean matched, Io io, MidiDevice.Info info) {
    public enum Io {
      IN,
      OUT,
      UNKNOWN;

      static Io of(MidiDevice.Info midiDeviceInfo) {
        if (isMidiDeviceForInput(midiDeviceInfo))
          return IN;
        if (isMidiDeviceForOutput(midiDeviceInfo))
          return OUT;
        return UNKNOWN;
      }
    }

    public static MidiDeviceRecord fromMidiDeviceInfo(MidiDevice.Info midiDeviceInfo, Predicate<MidiDevice.Info> cond) {
      return new MidiDeviceRecord(cond.test(midiDeviceInfo), Io.of(midiDeviceInfo), midiDeviceInfo);
    }
  }


  public static class MidiDeviceReportComposer {
    public interface Formatter {
      List<String> header(MidiDevice.Info info, String title);

      String formatRecord(MidiDeviceRecord record);

      List<String> footer();
    }

    final List<MidiDeviceRecord> records;
    final Formatter recordFormatter;


    public MidiDeviceReportComposer(Formatter formatter) {
      this.recordFormatter = formatter;
      this.records = new LinkedList<>();
    }

    public MidiDeviceReportComposer add(MidiDeviceRecord record) {
      this.records.add(record);
      return this;
    }

    public List<String> build() {
      MidiDevice.Info dummyInfoForHeader = new MidiDevice.Info("name", "vendor", "description", "version") {
      };
      return new ArrayList<>() {
        {
          this.addAll(MidiDeviceReportComposer.this.recordFormatter.header(dummyInfoForHeader, "Available MIDI devices"));
          this.addAll(MidiDeviceReportComposer.this.records.stream()
              .map(MidiDeviceReportComposer.this.recordFormatter::formatRecord)
              .toList());
          this.addAll(MidiDeviceReportComposer.this.recordFormatter.footer());
        }
      };
    }
  }

  public static MidiDeviceReportComposer createMidiDeviceReportComposer(Function<String, String> titleFormatter, Function<MidiDeviceRecord, String> recordFormatter, Predicate<MidiDevice.Info> cond, Stream<MidiDevice.Info> midiDeviceInfoStream) {
    return createMidiDeviceReportComposer(titleFormatter, recordFormatter, (MidiDevice.Info i) -> MidiDeviceRecord.fromMidiDeviceInfo(i, cond), midiDeviceInfoStream);
  }

  private static MidiDeviceReportComposer createMidiDeviceReportComposer(Function<String, String> titleFormatter, Function<MidiDeviceRecord, String> recordFormatter, Function<MidiDevice.Info, MidiDeviceRecord> midiDeviceRecordFactory, Stream<MidiDevice.Info> midiDeviceInfoStream) {
    return createMidiDeviceReportComposer(new MidiDeviceReportComposer.Formatter() {
      @Override
      public List<String> header(MidiDevice.Info info, String title) {
        return List.of(
            titleFormatter.apply(title),
            recordFormatter.apply(new MidiDeviceRecord(false, MidiDeviceRecord.Io.UNKNOWN, info)),
            "---------------------------------------------------------------------------"
        );
      }

      @Override
      public String formatRecord(MidiDeviceRecord record) {
        return recordFormatter.apply(record);
      }

      @Override
      public List<String> footer() {
        return emptyList();
      }
    }, midiDeviceRecordFactory, midiDeviceInfoStream);
  }

  private static MidiDeviceReportComposer createMidiDeviceReportComposer(MidiDeviceReportComposer.Formatter reportFormatter, Function<MidiDevice.Info, MidiDeviceRecord> midiDeviceRecordFactory, Stream<MidiDevice.Info> midiDeviceInfoStream) {
    MidiDeviceReportComposer reportComposer = new MidiDeviceReportComposer(reportFormatter);
    midiDeviceInfoStream.map(midiDeviceRecordFactory).forEach(reportComposer::add);
    return reportComposer;
  }


  public static String formatMidiDeviceInfo(MidiDevice.Info info) {
    return String.format(
        "%-25s %-15s %-35s",
        info == null ? "name" : info.getName(),
        info == null ? "version" : info.getVersion(),
        info == null ? "vendor" : info.getVendor()
    );
  }

  public static boolean isMidiDeviceForInput(MidiDevice.Info device) {
    Object tmp = null;
    try {
      MidiDevice dev = MidiSystem.getMidiDevice(device);
      try (dev) {
        dev.open();
        tmp = dev.getTransmitter();
      }
    } catch (Exception ignored) {
    }
    return tmp != null;
  }

  public static boolean isMidiDeviceForOutput(MidiDevice.Info info) {
    Object tmp = null;
    try {
      MidiDevice dev = MidiSystem.getMidiDevice(info);
      try (dev) {
        dev.open();
        tmp = dev.getReceiver();
      }
    } catch (Exception ignored) {
    }
    return tmp != null;
  }


  public static MidiDeviceScanner chooseInputDevices(PrintStream ps, Pattern regex) {
    return new MidiDeviceScanner.RegexMidiDeviceScanner(ps, regex) {
      @Override
      protected String getTitle() {
        return "MIDI-in devices";
      }

      @Override
      protected boolean matches(MidiDevice.Info device) {
        return super.matches(device) && isMidiDeviceForInput(device);
      }
    };
  }

  public static MidiDeviceScanner chooseOutputDevices(PrintStream ps, Pattern regex) {
    return new MidiDeviceScanner.RegexMidiDeviceScanner(ps, regex) {
      @Override
      protected String getTitle() {
        return "MIDI-out devices";
      }

      @Override
      protected boolean matches(MidiDevice.Info device) {
        return super.matches(device) && isMidiDeviceForOutput(device);
      }
    };
  }

  public static MidiDevice.Info[] getInfos(String portName, MidiDeviceScanner scanner, Pattern regex) throws CliException {
    MidiDevice.Info[] matchedInfos = scanner.getMatchedDevices();
    if (matchedInfos.length > 1) {
      String msg = format("Device for port '%s' (regex:%s) wasn't unique (%d)", portName, regex, matchedInfos.length);
      throw new CliException(msg);
    } else if (matchedInfos.length == 0) {
      String msg = format("No matching device was found for port '%s' (regex:%s)", portName, regex);
      throw new CliException(msg);
    }
    return matchedInfos;
  }
}
