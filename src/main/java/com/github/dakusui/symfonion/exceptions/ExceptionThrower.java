package com.github.dakusui.symfonion.exceptions;

import com.github.dakusui.symfonion.utils.midi.MidiDeviceRecord;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import java.io.Closeable;
import java.io.File;
import java.util.HashMap;
import java.util.function.Predicate;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsg;
import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.ContextKey.*;
import static com.github.dakusui.valid8j.ValidationFluents.all;
import static com.github.dakusui.valid8j.ValidationFluents.that;
import static com.github.dakusui.valid8j_pcond.fluent.Statement.objectValue;
import static java.lang.String.format;

public class ExceptionThrower {
  public enum ContextKey {
    MIDI_DEVICE_INFO(MidiDevice.Info.class),
    MIDI_DEVICE_INFO_IO(MidiDeviceRecord.Io.class),

    JSON_ELEMENT_ROOT(JsonObject.class)
    ;
    private final Class<?> type;

    ContextKey(Class<?> type) {
      this.type = type;
    }
  }

  public record ContextEntry(ContextKey key, Object value) {
  }

  public static class Context implements Closeable {
    private final Context parent;
    private final HashMap<ExceptionThrower.ContextKey, Object> values = HashMap.newHashMap(100);

    private Context(Context parent) {
      this.parent = parent;
    }

    public Context() {
      this.parent = null;
    }

    public Context set(ExceptionThrower.ContextKey key, Object value) {
      assert all(
          objectValue(key).then().isNotNull().$(),
          objectValue(value).then().isNotNull().isInstanceOf(key.type).$());
      this.values.put(key, value);
      return this;
    }

    public Context parent() {
      return this.parent;
    }

    @SuppressWarnings({"unchecked"})
    <T> T get(ContextKey key) {
      assert that(objectValue(key).then().isNotNull().$());
      if (!this.values.containsKey(key)) {
        assert that(objectValue(this).invoke("parent").then().isNotNull());
        return this.parent().get(key);
      }
      return (T) this.values.get(key);
    }


    @Override
    public void close() {
      ExceptionThrower.context.set(this.parent);
    }

    Context createChild() {
      return new Context(this);
    }
  }

  private static final ThreadLocal<Context> context = new ThreadLocal<>();

  public static ContextEntry contextEntry(ContextKey key, Object value) {
    assert all(
        objectValue(key).then().isNotNull().$(),
        objectValue(value).then().isNotNull().isInstanceOf(classOfValueFor(key)).$());
    return new ContextEntry(key, value);
  }


  public static ContextEntry $(ContextKey key, Object value) {
    return contextEntry(key, value);
  }

  public static Context context(ContextEntry... entries) {
    Context ret = currentContext().createChild();
    context.set(ret);
    for (ContextEntry each : entries)
      ret.set(each.key(), each.value());
    return ret;
  }

  public static SymfonionException compilationException(String msg, Throwable e) throws SymfonionException {
    throw new SymfonionException(msg, e);
  }

  public static SymfonionException fileNotFoundException(File file, Throwable e) throws SymfonionException {
    throw new SymfonionException(format("%s: File not found (%s)", file, e.getMessage()));
  }

  public static SymfonionException loadFileException(File file, Throwable e) throws SymfonionException {
    throw new SymfonionException(format("%s: %s", file, e.getMessage()));
  }

  public static SymfonionException loadResourceException(String resourceName, Throwable e) throws SymfonionException {
    throw new SymfonionException(format("%s: Failed to read resource (%s)", resourceName, e.getMessage()));
  }

  public static SymfonionReferenceException noteMapNotFoundException(JsonElement problemCausingJsonNode, String missingReference) throws SymfonionException {
    throw new SymfonionReferenceException(missingReference, "notemap", problemCausingJsonNode, contextValueOf(JSON_ELEMENT_ROOT));
  }

  public static SymfonionReferenceException noteNotDefinedException(JsonElement problemCausingJsonNode, String missingReference, String notemapName) throws SymfonionException {
    throw new SymfonionReferenceException(missingReference, format("note in %s", notemapName), problemCausingJsonNode, contextValueOf(JSON_ELEMENT_ROOT));
  }

  public static SymfonionReferenceException grooveNotDefinedException(JsonElement problemCausingJsonNode, String missingReference) throws SymfonionException {
    throw new SymfonionReferenceException(missingReference, "groove", problemCausingJsonNode, contextValueOf(JSON_ELEMENT_ROOT));
  }

  public static SymfonionReferenceException partNotFound(JsonElement problemCausingJsonNode, String missingReference) throws SymfonionException {
    throw new SymfonionReferenceException(missingReference, "part", problemCausingJsonNode, contextValueOf(JSON_ELEMENT_ROOT));
  }

  public static SymfonionReferenceException patternNotFound(JsonElement problemCausingJsonNode, String missingReference) throws SymfonionException {
    throw new SymfonionReferenceException(missingReference, "pattern", problemCausingJsonNode, contextValueOf(JSON_ELEMENT_ROOT));
  }

  public static SymfonionTypeMismatchException typeMismatchException(JsonElement actualJSON, String... expectedTypes) throws SymfonionSyntaxException {
    throw new SymfonionTypeMismatchException(expectedTypes, actualJSON, actualJSON, contextValueOf(JSON_ELEMENT_ROOT));
  }

  public static SymfonionIllegalFormatException illegalFormatException(JsonElement actualJSON, String acceptableExample) throws SymfonionIllegalFormatException {
    throw new SymfonionIllegalFormatException(actualJSON, contextValueOf(JSON_ELEMENT_ROOT), acceptableExample);
  }

  public static SymfonionIllegalFormatException illegalFormatException(JsonElement actualJSON, JsonObject root, String acceptableExample) throws SymfonionIllegalFormatException {
    throw new SymfonionIllegalFormatException(actualJSON, contextValueOf(JSON_ELEMENT_ROOT), acceptableExample);
  }

  public static SymfonionMissingElementException requiredElementMissingException(JsonElement actualJSON, JsonObject root, Object relPath) throws SymfonionMissingElementException {
    throw new SymfonionMissingElementException(actualJSON, root, relPath);
  }

  public static SymfonionException deviceException(String msg, Throwable e) throws SymfonionException {
    throw new SymfonionException(msg, e);
  }

  public static RuntimeException runtimeException(String msg, Throwable e) {
    throw new RuntimeException(msg, e);
  }

  public static FractionFormatException throwFractionFormatException(String fraction) throws FractionFormatException {
    throw new FractionFormatException(fraction);
  }

  public static SymfonionRuntimeException interrupted(InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new SymfonionRuntimeException(e.getMessage(), e);
  }

  public static CliException failedToRetrieveTransmitterFromMidiIn(MidiUnavailableException e, MidiDevice.Info inMidiDeviceInfo) {
    throw new CliException(format("(-) Failed to get transmitter from MIDI-in device (%s)", inMidiDeviceInfo.getName()), e);
  }

  public static CliException failedToOpenMidiDevice(MidiUnavailableException ee) {
    throw new CliException(format("(-) Failed to open MIDI-%s device (%s)",
        ExceptionThrower.<MidiDevice.Info>contextValueOf(MIDI_DEVICE_INFO),
        ExceptionThrower.<MidiDeviceRecord.Io>contextValueOf(MIDI_DEVICE_INFO_IO).name().toLowerCase()),
        ee);
  }

  public static CliException failedToAccessMidiDevice(String deviceType, MidiUnavailableException e, MidiDevice.Info[] matchedInfos) {
    throw new CliException(composeErrMsg(format("Failed to access MIDI-%s device:'%s'.", deviceType, matchedInfos[0].getName()), "O"), e);
  }

  public static RuntimeException multipleMidiDevices(MidiDeviceRecord e1, MidiDeviceRecord e2, Predicate<MidiDeviceRecord> cond) {
    throw new CliException(format("Multiple midi devices (at least: '%s', '%s') are found for: '%s'", e1, e2, cond));
  }

  public static RuntimeException noSuchMidiDeviceWasFound(Predicate<MidiDeviceRecord> cond) {
    throw new CliException(format("No such MIDI device was found for: '%s'", cond));
  }

  public static RuntimeException failedToGetTransmitter() {
    throw new RuntimeException();
  }

  public static RuntimeException failedToSetSequence() {
    throw new RuntimeException();
  }

  private static <T> T contextValueOf(ContextKey contextKey) {
    return currentContext().get(contextKey);
  }

  private static Context currentContext() {
    if (context.get() == null)
      context.set(new Context());
    return context.get();
  }

  private static Class<?> classOfValueFor(ContextKey key) {
    class GivenKeyIsNull {
    }
    return key != null ? key.type : GivenKeyIsNull.class;
  }
}