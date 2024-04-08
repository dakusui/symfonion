package com.github.dakusui.symfonion.compat.exceptions;

import com.github.dakusui.symfonion.compat.json.CompatJsonUtils;
import com.github.dakusui.symfonion.utils.midi.MidiDeviceRecord;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import java.io.Closeable;
import java.io.File;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsg;
import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.ContextKey.*;
import static com.github.dakusui.valid8j.ValidationFluents.all;
import static com.github.dakusui.valid8j.ValidationFluents.that;
import static com.github.dakusui.valid8j_pcond.fluent.Statement.objectValue;
import static java.lang.String.format;

public class CompatExceptionThrower {
  public enum ContextKey {
    MIDI_DEVICE_INFO(MidiDevice.Info.class),
    MIDI_DEVICE_INFO_IO(String.class),
    JSON_ELEMENT_ROOT(JsonObject.class),
    SOURCE_FILE(File.class),
    REFERENCING_JSON_NODE(JsonElement.class);
    private final Class<?> type;

    ContextKey(Class<?> type) {
      this.type = type;
    }
  }

  public record ContextEntry(ContextKey key, Object value) {
  }

  public static class Context implements Closeable {
    private final Context parent;
    private final HashMap<CompatExceptionThrower.ContextKey, Object> values = HashMap.newHashMap(100);

    private Context(Context parent) {
      this.parent = parent;
    }

    public Context() {
      this.parent = null;
    }

    public Context set(CompatExceptionThrower.ContextKey key, Object value) {
      assert all(
          objectValue(key).then().isNotNull().$(),
          objectValue(value).then().isNotNull().isInstanceOf(key.type).$());
      this.values.put(key, value);
      return this;
    }

    public Context parent() {
      return this.parent;
    }

    /**
     *
     */
    @SuppressWarnings({"unchecked"})
    <T> T get(ContextKey key) {
      assert that(objectValue(key).then().isNotNull().$());
      // In production, we do not want to produce a NullPointerException, even if the key is null.
      // Just return null in such a situation.
      if (key == null)
        return null;
      if (!this.values.containsKey(key)) {
        assert that(objectValue(this).invoke("parent").then().isNotNull());
        // In production, we do not want to produce a NullPointerException, even if a value associated with the key doesn't exist.
        // Just return null, in such a situation.
        if (this.parent() == null)
          return null;
        return this.parent().get(key);
      }
      return (T) this.values.get(key);
    }


    @Override
    public void close() {
      CompatExceptionThrower.context.set(this.parent);
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
    throw new SymfonionException(msg, e, contextValueOf(SOURCE_FILE));
  }

  public static SymfonionException fileNotFoundException(File file, Throwable e) throws SymfonionException {
    throw new SymfonionException(format("%s: File not found (%s)", file, e.getMessage()), file);
  }

  public static SymfonionException loadFileException(Throwable e) throws SymfonionException {
    throw new SymfonionException(format("%s: %s", contextValueOf(SOURCE_FILE), e.getMessage()), contextValueOf(SOURCE_FILE));
  }

  public static SymfonionException loadResourceException(String resourceName, Throwable e) throws SymfonionException {
    throw new SymfonionException(format("%s: Failed to read resource (%s)", resourceName, e.getMessage()), contextValueOf(SOURCE_FILE));
  }

  public static SymfonionReferenceException noteMapNotFoundException(JsonElement problemCausingJsonNode, String missingReference) throws SymfonionException {
    throw new SymfonionReferenceException(missingReference, "notemap", problemCausingJsonNode, contextValueOf(JSON_ELEMENT_ROOT), contextValueOf(SOURCE_FILE), contextValueOf(JSON_ELEMENT_ROOT));
  }

  public static SymfonionReferenceException noteNotDefinedException(JsonElement problemCausingJsonNode, String missingReference, String notemapName) throws SymfonionException {
    throw new SymfonionReferenceException(missingReference, format("note in %s", notemapName), problemCausingJsonNode, contextValueOf(JSON_ELEMENT_ROOT), contextValueOf(SOURCE_FILE), contextValueOf(JSON_ELEMENT_ROOT));
  }

  public static SymfonionException syntaxErrorInNotePattern(String s, int i, Matcher m) {
    return new SymfonionException("Error:" + s.substring(0, i) + "[" + s.substring(i, m.start()) + "]" + s.substring(m.start()), contextValueOf(SOURCE_FILE));
  }

  public static SymfonionReferenceException grooveNotDefinedException(JsonElement problemCausingJsonNode, String missingReference) throws SymfonionException {
    throw new SymfonionReferenceException(missingReference, "groove", problemCausingJsonNode, contextValueOf(JSON_ELEMENT_ROOT), contextValueOf(SOURCE_FILE), CompatJsonUtils.asJsonElement(contextValueOf(JSON_ELEMENT_ROOT), "$grooves"));
  }

  public static SymfonionReferenceException partNotFound(JsonElement problemCausingJsonNode, String missingReference) throws SymfonionException {
    throw new SymfonionReferenceException(missingReference, "part", problemCausingJsonNode, contextValueOf(JSON_ELEMENT_ROOT), contextValueOf(SOURCE_FILE), CompatJsonUtils.asJsonElement(contextValueOf(JSON_ELEMENT_ROOT), "$parts"));
  }

  public static SymfonionReferenceException patternNotFound(String missingReference) throws SymfonionException {
    throw new SymfonionReferenceException(missingReference, "pattern", contextValueOf(REFERENCING_JSON_NODE), contextValueOf(JSON_ELEMENT_ROOT), contextValueOf(SOURCE_FILE), CompatJsonUtils.asJsonElement(contextValueOf(JSON_ELEMENT_ROOT), "$patterns"));
  }

  public static SymfonionTypeMismatchException typeMismatchException(JsonElement actualJSON, String... expectedTypes) throws SymfonionSyntaxException {
    throw new SymfonionTypeMismatchException(expectedTypes, actualJSON, actualJSON, contextValueOf(JSON_ELEMENT_ROOT), contextValueOf(SOURCE_FILE));
  }

  public static SymfonionIllegalFormatException illegalFormatException(JsonElement actualJSON, String acceptableExample) throws SymfonionIllegalFormatException {
    throw new SymfonionIllegalFormatException(actualJSON, acceptableExample, contextValueOf(JSON_ELEMENT_ROOT), contextValueOf(SOURCE_FILE));
  }

  public static SymfonionIllegalFormatException syntaxErrorWhenExpandingDotsIn(JsonArray errorContainingJsonArray) {
    return new SymfonionIllegalFormatException(
        errorContainingJsonArray,
        "In this array, a string can contain only dots. E.g. '[1, \"...\",3]'. This will be expanded and interpolation of integer values will happen.",
        contextValueOf(JSON_ELEMENT_ROOT),
        contextValueOf(SOURCE_FILE) );
  }

  public static SymfonionIllegalFormatException typeMismatchWhenExpandingDotsIn(JsonArray errorContainingJsonArray) {
    return new SymfonionIllegalFormatException(
        errorContainingJsonArray,
        "This array, only integers, nulls, and strings containing only dots (...) are allowed.",
        contextValueOf(JSON_ELEMENT_ROOT),
        contextValueOf(SOURCE_FILE) );
  }

  public static SymfonionMissingElementException requiredElementMissingException(JsonElement problemCausingJsonNode, Object relativePathFromProblemCausingJsonNode) throws SymfonionMissingElementException {
    throw new SymfonionMissingElementException(problemCausingJsonNode, relativePathFromProblemCausingJsonNode, contextValueOf(JSON_ELEMENT_ROOT), contextValueOf(SOURCE_FILE));
  }

  public static SymfonionMissingElementException requiredElementMissingException(JsonElement actualJSON, JsonObject root, Object relPath) throws SymfonionMissingElementException {
    throw new SymfonionMissingElementException(actualJSON, relPath, root, contextValueOf(SOURCE_FILE));
  }

  public static SymfonionException deviceException(String msg, Throwable e) throws SymfonionException {
    throw new SymfonionException(msg, e, contextValueOf(SOURCE_FILE));
  }

  public static RuntimeException runtimeException(String msg, Throwable e) {
    throw new RuntimeException(msg, e);
  }

  public static FractionFormatException throwFractionFormatException(String fraction) throws FractionFormatException {
    throw new FractionFormatException(fraction);
  }

  public static SymfonionInterruptedException interrupted(InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new SymfonionInterruptedException(e.getMessage(), e);
  }

  public static CliException failedToRetrieveTransmitterFromMidiIn(MidiUnavailableException e, MidiDevice.Info inMidiDeviceInfo) {
    throw new CliException(format("(-) Failed to get transmitter from MIDI-in device (%s)", inMidiDeviceInfo.getName()), e);
  }

  public static CliException failedToOpenMidiDevice(MidiUnavailableException ee) {
    throw new CliException(format("(-) Failed to open MIDI-%s device (%s)",
        CompatExceptionThrower.<MidiDevice.Info>contextValueOf(MIDI_DEVICE_INFO),
        CompatExceptionThrower.<String>contextValueOf(MIDI_DEVICE_INFO_IO).toLowerCase()), ee);
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