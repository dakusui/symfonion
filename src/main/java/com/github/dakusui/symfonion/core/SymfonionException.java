package com.github.dakusui.symfonion.core;

import java.io.File;

public class SymfonionException extends Exception {

	/**
	 * A serial version uid.
	 */
	private static final long serialVersionUID = -1999577216046615241L;

	private File   sourceFile = null;

	public SymfonionException() {
		super();
	}

	public SymfonionException(String message, Throwable cause) {
		super(message, cause);
	}

	public SymfonionException(String message) {
		super(message);
	}

	public SymfonionException(Throwable cause) {
		super(cause);
	}
	
	public File getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	public static void main(String[] args) {
		System.out.println(String.format("%s%s", "hello"));
	}
}
