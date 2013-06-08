package com.github.dakusui.symfonion.core;

import java.io.File;

import com.google.gson.JsonElement;


public class ExceptionThrower {

	public static void throwCompilationException(String msg, Throwable e) throws SymfonionException {
		throw new SymfonionException(msg, e);
	}

	public static void throwFileNotFoundException(File file, Throwable e) throws SymfonionException {
		throw new SymfonionException(String.format("%s: File not found", file, e.getMessage()));
	}

	public static void throwLoadFileException(File file, Throwable e) throws SymfonionException {
		throw new SymfonionException(String.format("%s: %s", file, e.getMessage()));
	}

	public static void throwLoadResourceException(String resourceName, Throwable e) throws SymfonionException {
		throw new SymfonionException(String.format("%s: Failed to read resource (%s)", resourceName, e.getMessage()));
	}

	public static void throwNoteMapNotFoundException(JsonElement location, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "notemap", location);
	}

	public static void throwNoteNotDefinedException(JsonElement location, String missingReference, String notemapName) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, String.format("note in %s", notemapName), location);
	}

	public static void throwGrooveNotDefinedException(JsonElement location, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "groove", location);
	}

	public static void throwPartNotFound(JsonElement location, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "part", location);
	}

	public static void throwPatternNotFound(JsonElement location, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "pattern", location);
	}
	
	public static void throwTypeMismatchException(JsonElement actualJSON, String... expectedTypes) throws SymfonionSyntaxException {
		throw new SymfonionTypeMismatchException(expectedTypes, actualJSON, actualJSON);
	}

	public static void throwIllegalFormatException(JsonElement actualJSON, String acceptableExample) throws SymfonionIllegalFormatException {
		throw new SymfonionIllegalFormatException(actualJSON, acceptableExample);
	}
	
	public static void throwRequiredElementMissingException(JsonElement actualJSON, Object relPath) throws SymfonionMissingElementException {
		throw new SymfonionMissingElementException(actualJSON, relPath);
	}
	
	public static void throwDeviceException(String msg, Throwable e) throws SymfonionException {
		throw new SymfonionException(e);
	}

	public static void throwRuntimeException(String msg, Throwable e) {
		throw new RuntimeException(msg);
	}
}