package com.github.dakusui.symfonion.core;


public class ExceptionThrower {

	public static void throwJSONException(String msg, Throwable t) throws SymfonionException {
		throw new SymfonionException(msg, t);
	}

	public static void throwDeviceNotFoundException(String devicename) throws SymfonionException {
		throw new SymfonionException("Device:<" + devicename + "> is not defined.");
	}

	public static void throwSyntaxException(String msg, Throwable t) throws SymfonionException {
		throw new SymfonionException(msg, t);
	}

	public static void throwCompilationException(String msg, Throwable e) throws SymfonionException {
		throw new SymfonionException(msg, e);
	}

	public static void throwLoadException(String msg, Throwable e) throws SymfonionException {
		throw new SymfonionException(msg, e);
	}

	public static void throwNoteMapNotFoundException(String msg, Throwable e) throws SymfonionException {
		throw new SymfonionException(msg, e);
	}

	public static void throwNoteNotDefinedException(String msg) throws SymfonionException {
		throw new SymfonionException(msg);
	}

	public static void throwInstrumentNotFound(String msg, Throwable e) throws SymfonionException {
		throw new SymfonionException(msg);
	}

	public static void throwDeviceException(String msg, Throwable e) throws SymfonionException {
		throw new SymfonionException(e);
	}

	public static void throwRuntimeException(String msg, Throwable e) {
		throw new RuntimeException(msg);
	}
}