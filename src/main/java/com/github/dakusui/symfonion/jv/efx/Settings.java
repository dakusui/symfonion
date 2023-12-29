package com.github.dakusui.symfonion.jv.efx;

public class Settings {
  enum SignalSink {
    MIX,
    EFX,
    REV,
    OUT12,
    MIXREV
  }

  static abstract class SignalSource {
    SignalSink outputAssign;
    int        outputLevel;
    int        chorusSendLevel;
    int        reverbSendLevel;

    abstract SignalSink[] allowedSinks();
  }

  static class Tone extends SignalSource {
    @Override
    SignalSink[] allowedSinks() {
      return new SignalSink[] { SignalSink.MIX, SignalSink.EFX, SignalSink.OUT12 };
    }
  }

  static class EFX extends SignalSource {
    @Override
    SignalSink[] allowedSinks() {
      return new SignalSink[] { SignalSink.MIX, SignalSink.OUT12 };
    }
  }

  static class Chorus extends SignalSource {
    @Override
    SignalSink[] allowedSinks() {
      return new SignalSink[] { SignalSink.MIX, SignalSink.REV };
    }
  }

  static class Reverb extends SignalSource {
    @Override
    SignalSink[] allowedSinks() {
      return new SignalSink[] { SignalSink.MIX };
    }
  }

  Tone   tone   = new Tone();
  EFX    efx    = new EFX();
  Chorus chorus = new Chorus();
  Reverb reverb = new Reverb();

  String description = null;

  public static Settings unprocessedSound(int outputLevel) {
    Settings ret = new Settings();
    return ret;
  }

  public static Settings chorusAndReverbInSeries() {
    return null;
  }

  public static Settings efxOnly() {
    return null;
  }

  public static Settings reverbToChorusedSoundAndEfxInParallel() {
    return null;
  }

  public static Settings efxChorusAndReverbInSeries() {
    return null;
  }

  public static Settings chorusAndReverbInParallelAndEfxInSeries() {
    return null;
  }

  public static Settings efxChorusAndReverbInParallel() {
    return null;
  }

  public static Settings parallelAndSeries() {
    return null;
  }

  public static Settings unprocessedSoundFromOUTPUT12() {
    return null;
  }

  public static Settings efxProcessedSoundFromOUTPUT12() {
    return null;
  }
}
/*
 *                   TONE    EFX     Chorus  Reverb
 * Output assign     V       V       V       V
 *     MIX           V       V       V       V
 *     EFX           V       -       -       -
 *     REV           -       -       V       -
 *     OUT1/2        V       V       -       -
 *     MIX+REV       -       -       V       -
 * Output level      V       V       V       V
 * Chorus send level V       V       -       -
 * Reverb send level V       V       -       -
 * 
 * 
 * 
 */
