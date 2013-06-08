package com.github.dakusui.symfonion.core;

public class SymfonionException extends Exception {

	/**
	 * A serial version uid.
	 */
	private static final long serialVersionUID = -1999577216046615241L;

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
	
	public static void main(String[] args) {
		System.out.println(String.format("%s%s", "hello"));
	}
}
