package com.github.dakusui.symfonion.core;

import com.github.dakusui.logias.Logias;
import com.github.dakusui.logias.lisp.Context;
import com.github.dakusui.logias.lisp.s.Literal;
import com.github.dakusui.logias.lisp.s.Sexp;
import com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower;
import com.github.dakusui.symfonion.compat.exceptions.SymfonionException;
import com.github.dakusui.symfonion.song.*;
import com.github.dakusui.symfonion.song.PartMeasureParameters;
import com.github.dakusui.symfonion.utils.Fraction;
import com.github.dakusui.symfonion.utils.Utils;
import com.google.gson.JsonArray;

import javax.sound.midi.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.JSON_ELEMENT_ROOT;

/**
 * A class that models a "compiler", which generates MIDI data (`Sequence`) from a given `Song` object.
 */
public class MidiCompiler {

  private final Context logiasContext;

  /**
   * Creates an object of this class.
   *
   * @param logiasContext A context of an interpreter that executes a JSON array as an S-expression.
   * @see Logias
   * @see Context
   */
  public MidiCompiler(Context logiasContext) {
    this.logiasContext = (logiasContext);
  }

  /**
   * Compiles a {@link Song} object into a map from a port name to {@link Sequence} object.
   *
   * @param song An object that holds user-provided music data.
   * @return A map from a port name to {@code Sequence} object.
   * @throws InvalidMidiDataException Won't be thrown.
   * @throws SymfonionException       Undefined part name is referenced by a bar.
   */
  public Map<String, Sequence> compile(Song song) throws InvalidMidiDataException, SymfonionException {
    System.err.println("Now compiling...");
    int                   resolution = 384;
    Map<String, Sequence> ret        = new HashMap<>();
    Map<String, Track>    tracks;
    tracks = new HashMap<>();
    for (String partName : song.partNames()) {
      Part     part     = song.part(partName);
      String   portName = part.portName();
      Sequence sequence = ret.get(portName);
      if (sequence == null) {
        sequence = new Sequence(Sequence.PPQ, resolution / 4);
        ret.put(portName, sequence);
      }
      tracks.put(partName, sequence.createTrack());
    }

    ////
    // position is the offset of a bar from the beginning of the sequence.
    // Giving the sequencer a grace period to initialize its internal state.
    long barPositionInTicks = 0; //= resolution / 4;
    int  barid              = 0;
    for (Bar bar : song.bars()) {
      try (var ignored = context($(JSON_ELEMENT_ROOT, bar.rootJsonObject()))) {
        barStarted(barid);
        Groove groove = bar.groove();
        for (String partName : bar.partNames()) {
          partStarted(partName);
          Track track = tracks.get(partName);
          if (track == null) {
            aborted();
            throw partNotFound(bar.lookUpJsonNode(partName), partName);
          }
          int channel = song.part(partName).channel();
          for (PatternSequence patternSequence : bar.part(partName)) {
            ////
            // relativePosition is a relative position from the beginning
            // of the bar the pattern belongs to.
            Fraction relPosInBar = Fraction.ZERO;
            for (Pattern eachPattern : patternSequence) {
              PartMeasureParameters params = eachPattern.parameters();
              patternStarted();
              for (PartMeasure partMeasure : eachPattern.partMeasures()) {
                try {
                  Fraction endingPos = Fraction.add(relPosInBar, partMeasure.length());

                  partMeasure.compile(this, new MidiCompilerContext(track,
                                                                    channel,
                                                                    params,
                                                                    relPosInBar,
                                                                    barPositionInTicks,
                                                                    groove));

                  relPosInBar = endingPos;
                  ////
                  // Breaks if relative position goes over the length of the bar.
                  if (Fraction.compare(relPosInBar, bar.beats()) >= 0) {
                    break;
                  }
                } finally {
                  partMeasureEnded();
                }
              }
              patternEnded();
            }
          }
          partEnded();
        }
        barEnded();
        barid++;
        barPositionInTicks += (long) (bar.beats().doubleValue() * resolution);
      }
    }
    System.err.println("Compilation finished.");
    return ret;
  }

  public MidiEvent createNoteOnEvent(int ch, int nKey, int velocity, long lTick) throws InvalidMidiDataException {
    return createNoteEvent(ShortMessage.NOTE_ON,
                           ch,
                           nKey,
                           velocity,
                           lTick);
  }

  public MidiEvent createNoteOffEvent(int ch, int nKey, long lTick) throws InvalidMidiDataException {
    return createNoteEvent(ShortMessage.NOTE_OFF,
                           ch,
                           nKey,
                           0,
                           lTick);
  }

  protected MidiEvent createNoteEvent(int nCommand,
                                      int ch,
                                      int nKey,
                                      int nVelocity,
                                      long lTick) throws InvalidMidiDataException {
    ShortMessage message = new ShortMessage();
    message.setMessage(nCommand,
                       ch,
                       nKey,
                       nVelocity);
    return new MidiEvent(message,
                         lTick);
  }

  public MidiEvent createProgramChangeEvent(int ch, int pgnum, long lTick) throws InvalidMidiDataException {
    ShortMessage message = new ShortMessage();
    message.setMessage(ShortMessage.PROGRAM_CHANGE, ch, pgnum, 0);

    return new MidiEvent(message, lTick);
  }

  public MidiEvent createSysexEvent(int ch, JsonArray arr, long lTick) throws InvalidMidiDataException {
    SysexMessage message = new SysexMessage();
    Context      lctxt   = this.logiasContext.createChild();
    Sexp         channel = new Literal(ch);
    lctxt.bind("channel", channel);
    Logias logias    = new Logias(lctxt);
    Sexp   sysexsexp = logias.buildSexp(arr);
    Sexp   sexp      = logias.run(sysexsexp);
    if (Sexp.nil.equals(sexp)) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(SysexMessage.SYSTEM_EXCLUSIVE);    // status: SysEx start
    Iterator<Sexp> i = sexp.iterator().assumeList();
    while (i.hasNext()) {
      Sexp cur = i.next();
      baos.write((byte) cur.asAtom().longValue());
    }
    baos.write(SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE);    // End of exclusive
    try {
      baos.close();
    } catch (IOException e) {
      throw CompatExceptionThrower.runtimeException(e.getMessage(), e);
    }
    byte[] data = baos.toByteArray();
    message.setMessage(data, data.length);
    return new MidiEvent(message, lTick);
  }

  public MidiEvent createControlChangeEvent(int ch, int controllernum, int param, long lTick) throws InvalidMidiDataException {
    ShortMessage message = new ShortMessage();
    message.setMessage(ShortMessage.CONTROL_CHANGE, ch, controllernum, param);
    return new MidiEvent(message, lTick);
  }

  public MidiEvent createBankSelectMSBEvent(int ch, int bkmsb, long lTick) throws InvalidMidiDataException {
    return createControlChangeEvent(ch, 0, bkmsb, lTick);
  }

  public MidiEvent createBankSelectLSBEvent(int ch, int bklsb, long lTick) throws InvalidMidiDataException {
    return createControlChangeEvent(ch, 32, bklsb, lTick);
  }

  public MidiEvent createVolumeChangeEvent(int ch, int volume, long lTick) throws InvalidMidiDataException {
    return createControlChangeEvent(ch, 7, volume, lTick);
  }

  public MidiEvent createPanChangeEvent(int ch, int pan, long lTick) throws InvalidMidiDataException {
    return createControlChangeEvent(ch, 10, pan, lTick);
  }

  public MidiEvent createReverbEvent(int ch, int depth, long lTick) throws InvalidMidiDataException {
    return createControlChangeEvent(ch, 91, depth, lTick);
  }

  public MidiEvent createChorusEvent(int ch, int depth, long lTick) throws InvalidMidiDataException {
    ShortMessage message = new ShortMessage();
    message.setMessage(ShortMessage.CONTROL_CHANGE, ch, 93, depth);
    return new MidiEvent(message, lTick);
  }

  public MidiEvent createPitchBendEvent(int ch, int depth, long lTick) throws InvalidMidiDataException {
    ShortMessage message = new ShortMessage();
    message.setMessage(ShortMessage.PITCH_BEND, ch, 0, depth);
    return new MidiEvent(message, lTick);
  }

  public MidiEvent createModulationEvent(int ch, int depth, long lTick) throws InvalidMidiDataException {
    return createControlChangeEvent(ch, 1, depth, lTick);
  }

  public MidiEvent createAfterTouchChangeEvent(int ch, int v, long lTick) throws InvalidMidiDataException {
    ShortMessage message = new ShortMessage();
    message.setMessage(ShortMessage.CHANNEL_PRESSURE, ch, v, 0);
    return new MidiEvent(message, lTick);
  }

  public MidiEvent createTempoEvent(int tempo, long lTick) throws InvalidMidiDataException {
    int         mpqn = 60000000 / tempo;
    MetaMessage mm   = new MetaMessage();
    byte[]      data = Utils.getIntBytes(mpqn);
    mm.setMessage(0x51, data, data.length);
    return new MidiEvent(mm, lTick);
  }

  public void noteProcessed() {
    System.out.print(".");
  }

  public void controlEventProcessed() {
    System.out.print("*");
  }

  public void sysexEventProcessed() {
    System.out.print("X");
  }

  public void barStarted(int barid) {
    System.out.println("bar[" + barid + "]");
  }

  public void patternStarted() {
    System.out.print("[");
  }

  public void patternEnded() {
    System.out.print("]");
  }

  public void barEnded() {
  }

  public void partStarted(String partName) {
    System.out.print("    " + partName + ":");
  }

  public void partMeasureEnded() {
    System.out.print("|");
  }

  public void partEnded() {
    System.out.println();
  }

  public void aborted() {
    System.out.println("aborted.");
  }

  public void noteSetProcessed() {
    System.out.print(";");
  }
}
