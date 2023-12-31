package com.github.dakusui.symfonion.exceptions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import java.io.File;

import static com.github.dakusui.symfonion.cli.CliUtils.composeErrMsg;
import static java.lang.String.format;

public class ExceptionThrower {

	public static SymfonionException compilationException(String msg, Throwable e) throws SymfonionException {
		throw new SymfonionException(msg, e);
	}

	public static SymfonionException fileNotFoundException(File file, Throwable e) throws SymfonionException {
		throw new SymfonionException(String.format("%s: File not found (%s)", file, e.getMessage()));
	}

	public static SymfonionException loadFileException(File file, Throwable e) throws SymfonionException {
		throw new SymfonionException(String.format("%s: %s", file, e.getMessage()));
	}

	public static SymfonionException loadResourceException(String resourceName, Throwable e) throws SymfonionException {
		throw new SymfonionException(String.format("%s: Failed to read resource (%s)", resourceName, e.getMessage()));
	}

	public static SymfonionReferenceException noteMapNotFoundException(JsonElement problemCausingJsonNode, JsonObject root, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "notemap", problemCausingJsonNode, root);
	}

	public static SymfonionReferenceException noteNotDefinedException(JsonElement problemCausingJsonNode, JsonObject root, String missingReference, String notemapName) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, String.format("note in %s", notemapName), problemCausingJsonNode, root);
	}

	public static SymfonionReferenceException grooveNotDefinedException(JsonElement problemCausingJsonNode, JsonObject root, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "groove", problemCausingJsonNode, root);
	}

	public static SymfonionReferenceException partNotFound(JsonElement problemCausingJsonNode, JsonObject root, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "part", problemCausingJsonNode, root);
	}

	public static SymfonionReferenceException patternNotFound(JsonElement problemCausingJsonNode, JsonObject root, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "pattern", problemCausingJsonNode, root);
	}
	public static SymfonionTypeMismatchException typeMismatchException(JsonElement actualJSON, JsonObject root, String... expectedTypes) throws SymfonionSyntaxException {
		throw new SymfonionTypeMismatchException(expectedTypes, actualJSON, actualJSON, root);
	}

	public static SymfonionIllegalFormatException illegalFormatException(JsonElement actualJSON, JsonObject root, String acceptableExample) throws SymfonionIllegalFormatException {
		throw new SymfonionIllegalFormatException(actualJSON, root, acceptableExample);
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

  public static CliException failedToOpenMidiIn(MidiUnavailableException ee, MidiDevice.Info inMidiDeviceInfo) {
    throw new CliException(format("(-) Failed to open MIDI-in device (%s)", inMidiDeviceInfo.getName()), ee);
  }

  public static CliException failedToOpenMidiOut(MidiUnavailableException e, MidiDevice.Info outMidiDeviceInfo) {
    throw new CliException(format("(-) Failed to open MIDI-out device (%s)", outMidiDeviceInfo.getName()), e);
  }

	public static CliException failedToAccessMidiDevice(String deviceType, MidiUnavailableException e, MidiDevice.Info[] matchedInfos) {
		throw new CliException(composeErrMsg(format("Failed to access MIDI-%s device:'%s'.", deviceType, matchedInfos[0].getName()), "O"), e);
	}
}