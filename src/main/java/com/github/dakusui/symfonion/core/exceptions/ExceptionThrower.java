package com.github.dakusui.symfonion.core.exceptions;

import com.google.gson.JsonElement;

import java.io.File;


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

	public static SymfonionReferenceException noteMapNotFoundException(JsonElement location, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "notemap", location);
	}

	public static SymfonionReferenceException noteNotDefinedException(JsonElement location, String missingReference, String notemapName) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, String.format("note in %s", notemapName), location);
	}

	public static SymfonionReferenceException grooveNotDefinedException(JsonElement location, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "groove", location);
	}

	public static SymfonionReferenceException partNotFound(JsonElement location, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "part", location);
	}

	public static SymfonionReferenceException patternNotFound(JsonElement location, String missingReference) throws SymfonionException {
		throw new SymfonionReferenceException(missingReference, "pattern", location);
	}
	public static SymfonionTypeMismatchException typeMismatchException(JsonElement actualJSON, String... expectedTypes) throws SymfonionSyntaxException {
		throw new SymfonionTypeMismatchException(expectedTypes, actualJSON, actualJSON);
	}

	public static SymfonionIllegalFormatException illegalFormatException(JsonElement actualJSON, String acceptableExample) throws SymfonionIllegalFormatException {
		throw new SymfonionIllegalFormatException(actualJSON, acceptableExample);
	}
	
	public static SymfonionMissingElementException requiredElementMissingException(JsonElement actualJSON, Object relPath) throws SymfonionMissingElementException {
		throw new SymfonionMissingElementException(actualJSON, relPath);
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

}